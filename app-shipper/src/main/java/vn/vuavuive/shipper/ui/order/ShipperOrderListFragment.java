package vn.vuavuive.shipper.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * ShipperOrderListFragment — Fragment hiển thị danh sách đơn hàng cho Shipper.
 *
 * @param isHistory true  → hiển thị đơn đã hoàn thành (DELIVERED, FAILED, RETURNED)
 *                  false → hiển thị đơn đang active (PREPARING, IN_TRANSIT)
 */
@AndroidEntryPoint
public class ShipperOrderListFragment extends Fragment {

    private static final String ARG_IS_HISTORY = "is_history";

    @Inject ShipperOrderApi shipperOrderApi;

    private boolean isHistory;
    private ShipperOrderAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView tvError;
    private View layoutEmpty;

    public static ShipperOrderListFragment newInstance(boolean isHistory) {
        ShipperOrderListFragment fragment = new ShipperOrderListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_HISTORY, isHistory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHistory = getArguments().getBoolean(ARG_IS_HISTORY, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shipper_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh    = view.findViewById(R.id.swipe_refresh);
        recyclerOrders  = view.findViewById(R.id.recycler_orders);
        progressBar     = view.findViewById(R.id.progress_bar);
        tvError         = view.findViewById(R.id.tv_error);
        layoutEmpty     = view.findViewById(R.id.layout_empty);

        swipeRefresh.setColorSchemeResources(R.color.primary);

        adapter = new ShipperOrderAdapter(requireContext(), isHistory);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadOrders);

        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        // Quyết định filter status theo tab
        // Tab Active: lấy cả PREPARING và IN_TRANSIT (không lọc status → backend trả tất cả đơn của Shipper)
        // Tab History: lọc DELIVERED
        String statusFilter = "";

        shipperOrderApi.getMyShipperOrders(statusFilter).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call,
                                   @NonNull Response<ApiResponse<List<Order>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Order> allOrders = response.body().getData();

                    // For active tab: also filter PREPARING + IN_TRANSIT from the full list
                    List<Order> filtered = new ArrayList<>();
                    if (allOrders != null) {
                        for (Order order : allOrders) {
                            String status = order.getStatus() == null ? "" : order.getStatus().toUpperCase();
                            if (!isHistory) {
                                // Show PREPARING and IN_TRANSIT in active tab
                                if ("CONFIRMED".equals(status) ||
                                    "SHIPPING".equals(status) ||
                                    "PREPARING".equals(status) ||
                                    "IN_TRANSIT".equals(status) ||
                                    "READY_FOR_PICKUP".equals(status)) {
                                    filtered.add(order);
                                }
                            } else {
                                // Show DELIVERED, FAILED, RETURNED in history tab
                                if ("DELIVERED".equals(status) ||
                                    "FAILED".equals(status) ||
                                    "RETURNED".equals(status)) {
                                    filtered.add(order);
                                }
                            }
                        }
                    }

                    adapter.submitList(filtered);
                    layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerOrders.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    showError("Không thể tải dữ liệu. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean loading) {
        if (swipeRefresh.isRefreshing()) {
            if (!loading) swipeRefresh.setRefreshing(false);
        } else {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (loading) {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerOrders.setVisibility(View.GONE);
    }
}
