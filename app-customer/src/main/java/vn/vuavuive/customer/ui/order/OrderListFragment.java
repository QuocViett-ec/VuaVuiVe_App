package vn.vuavuive.customer.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.OrderViewModel;

@AndroidEntryPoint
public class OrderListFragment extends Fragment {

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private String currentStatus = null; // null = all
    private ProgressBar progressBar;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        progressBar = view.findViewById(R.id.progress_bar);

        setupTabs(view);
        setupRecyclerView(view);
        setupSwipeRefresh(view);
        loadOrders();
        setupHeaderSearch(view);
    }

    private void setupHeaderSearch(View view) {
        View etSearch = view.findViewById(R.id.header_et_search);
        if (etSearch != null) {
            etSearch.setFocusable(false);
            etSearch.setClickable(true);
            etSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), vn.vuavuive.customer.ui.search.SearchActivity.class);
                startActivity(intent);
            });
        }
        View btnMenu = view.findViewById(R.id.header_btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                try {
                    if (getActivity() instanceof vn.vuavuive.customer.ui.MainActivity) {
                        ((vn.vuavuive.customer.ui.MainActivity) getActivity()).navigateToProducts();
                    }
                } catch (Exception ignored) {}
            });
        }
    }

    private void setupTabs(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        String[] tabs = {"Tất cả", "Chờ xác nhận", "Đang giao", "Đã giao", "Đã hủy"};
        String[] statuses = {null, "pending", "shipping", "delivered", "cancelled"};

        for (String tab : tabs) tabLayout.addTab(tabLayout.newTab().setText(tab));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatus = statuses[tab.getPosition()];
                loadOrders();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_orders);
        orderAdapter = new OrderAdapter(getContext(), order -> {
            Intent intent = new Intent(getContext(), OrderDetailActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(orderAdapter);
    }

    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipe = view.findViewById(R.id.swipe_refresh);
        swipe.setColorSchemeResources(R.color.primary);
        swipe.setOnRefreshListener(() -> {
            loadOrders();
            swipe.setRefreshing(false);
        });
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        orderViewModel.getOrders(currentStatus, 1).observe(getViewLifecycleOwner(), result -> {
            progressBar.setVisibility(View.GONE);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                orderAdapter.setOrders(result.data);
                // Show/hide empty state wrapper
                View layoutEmpty = requireView().findViewById(R.id.layout_empty);
                if (layoutEmpty != null) {
                    layoutEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    // Fallback: tv_empty directly
                    TextView tvEmpty = requireView().findViewById(R.id.tv_empty);
                    if (tvEmpty != null) tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }
}
