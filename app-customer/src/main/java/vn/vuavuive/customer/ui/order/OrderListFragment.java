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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.util.Constants;

public class OrderListFragment extends Fragment {

    private static final String TAB_ALL = "all";
    private static final String TAB_PENDING = "pending_group";
    private static final String TAB_CONFIRMED = "confirmed_group";
    private static final String TAB_SHIPPING = "shipping_group";
    private static final String TAB_DELIVERED = "delivered";
    private static final String TAB_CANCELLED = "cancelled";
    private static final String TAB_RETURNS = "returns";

    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private String currentTab = TAB_ALL;
    private ProgressBar progressBar;
    private List<Order> allOrders = new ArrayList<>();
    private LiveData<AuthRepository.Result<List<Order>>> ordersLiveData;

    @Nullable
    @Override
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
                } catch (Exception ignored) {
                }
            });
        }
    }

    private void setupTabs(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        String[] tabs = {"Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đang giao", "Đã giao", "Đã hủy", "Trả hàng"};
        String[] keys = {TAB_ALL, TAB_PENDING, TAB_CONFIRMED, TAB_SHIPPING, TAB_DELIVERED, TAB_CANCELLED, TAB_RETURNS};

        for (String tab : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = keys[tab.getPosition()];
                showOrders();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
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
        swipe.setOnRefreshListener(() -> swipe.setRefreshing(false));
    }

    private void loadOrders() {
        if (progressBar == null || !isAdded() || getView() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        ordersLiveData = orderViewModel.getOrders(null, 1);
        ordersLiveData.observe(getViewLifecycleOwner(), result -> {
            progressBar.setVisibility(View.GONE);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                allOrders = result.data;
                showOrders();
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                orderAdapter.setOrders(Collections.emptyList());
                updateEmptyState(true);
            }
        });
    }

    private void showOrders() {
        if (orderAdapter == null) return;
        List<Order> filtered = filterOrders(allOrders);
        orderAdapter.setOrders(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private List<Order> filterOrders(List<Order> orders) {
        if (TAB_ALL.equals(currentTab)) return orders;

        List<Order> filtered = new ArrayList<>();
        for (Order order : orders) {
            if (matchesCurrentTab(order)) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    private boolean matchesCurrentTab(Order order) {
        String status = order.getStatus() == null
                ? ""
                : order.getStatus().toLowerCase(Locale.ROOT);

        switch (currentTab) {
            case TAB_PENDING:
                return Constants.isOrderPending(status);
            case TAB_CONFIRMED:
                return Constants.isOrderConfirmed(status);
            case TAB_SHIPPING:
                return Constants.isOrderShipping(status);
            case TAB_DELIVERED:
                return Constants.isOrderDelivered(status);
            case TAB_CANCELLED:
                return Constants.isOrderCancelled(status);
            case TAB_RETURNS:
                return order.getReturnRequest() != null || Constants.isOrderReturn(status);
            default:
                return true;
        }
    }

    private void updateEmptyState(boolean empty) {
        View root = getView();
        if (root == null) return;

        View layoutEmpty = root.findViewById(R.id.layout_empty);
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            return;
        }

        TextView tvEmpty = root.findViewById(R.id.tv_empty);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        if (ordersLiveData != null) {
            ordersLiveData.removeObservers(getViewLifecycleOwner());
            ordersLiveData = null;
        }
        allOrders = new ArrayList<>();
        super.onDestroyView();
    }
}
