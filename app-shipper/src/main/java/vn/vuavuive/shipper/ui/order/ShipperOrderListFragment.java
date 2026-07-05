package vn.vuavuive.shipper.ui.order;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.util.Constants;

/**
 * ShipperOrderListFragment — Hiển thị danh sách đơn hàng từ Firebase RTDB.
 *
 * @param isHistory true  → tab Lịch sử (DELIVERED, FAILED, RETURNED)
 *                  false → tab Cần giao (CONFIRMED, IN_TRANSIT)
 */
@AndroidEntryPoint
public class ShipperOrderListFragment extends Fragment {

    private static final String ARG_IS_HISTORY = "is_history";

    @Inject FirebaseShipperRepository repository;

    private boolean isHistory;
    private ShipperOrderAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView tvError;
    private View layoutEmpty;

    // Search and Filter views
    private EditText etSearch;
    private ChipGroup chipGroupFilter;

    // Live search & filter states
    private List<Order> masterList = new ArrayList<>();
    private String searchQuery = "";
    private String statusFilter = "ALL";

    // Giữ tham chiếu LiveData để remove observer khi Fragment destroy
    private androidx.lifecycle.LiveData<FirebaseShipperRepository.Result<List<Order>>> ordersLiveData;

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

        swipeRefresh   = view.findViewById(R.id.swipe_refresh);
        recyclerOrders = view.findViewById(R.id.recycler_orders);
        progressBar    = view.findViewById(R.id.progress_bar);
        tvError        = view.findViewById(R.id.tv_error);
        layoutEmpty    = view.findViewById(R.id.layout_empty);
        etSearch       = view.findViewById(R.id.et_search);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);

        swipeRefresh.setColorSchemeResources(R.color.primary);

        adapter = new ShipperOrderAdapter(requireContext(), isHistory);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {
            // Firebase real-time listener tự cập nhật, chỉ cần reset UI
            swipeRefresh.setRefreshing(false);
        });

        setupFilterChips();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        subscribeOrders();
    }

    private void setupFilterChips() {
        chipGroupFilter.removeAllViews();
        addChip("Tất cả", "ALL", true);
        if (!isHistory) {
            addChip("Chờ lấy hàng", "PENDING", false);
            addChip("Đang giao", "IN_TRANSIT", false);
        } else {
            addChip("Thành công", "SUCCESS", false);
            addChip("Thất bại", "FAILED", false);
        }
    }

    private void addChip(String label, String code, boolean checked) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setTag(code);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setClickable(true);
        chip.setCheckedIconVisible(false);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                statusFilter = code;
                applyFilterAndSearch();
            }
        });
        chipGroupFilter.addView(chip);
    }

    @Override
    public void onDestroyView() {
        if (ordersLiveData != null) {
            ordersLiveData.removeObservers(getViewLifecycleOwner());
            ordersLiveData = null;
        }
        masterList.clear();
        super.onDestroyView();
    }

    /**
     * Subscribe vào Firebase real-time orders.
     * Mỗi lần có dữ liệu mới, lưu vào masterList và áp dụng bộ lọc.
     */
    private void subscribeOrders() {
        showLoading(true);
        tvError.setVisibility(View.GONE);

        ordersLiveData = repository.getMyOrders();
        ordersLiveData.observe(getViewLifecycleOwner(), result -> {
            showLoading(false);

            if (result == null) return;

            switch (result.status) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    masterList = result.data != null ? result.data : new ArrayList<>();
                    applyFilterAndSearch();
                    break;
                case ERROR:
                    showError(result.message != null ? result.message : "Không tải được dữ liệu");
                    break;
            }
        });
    }

    private void applyFilterAndSearch() {
        List<Order> tabOrders = filterByTab(masterList);
        List<Order> filtered = new ArrayList<>();

        for (Order order : tabOrders) {
            // 1. Lọc theo trạng thái chip
            boolean matchesStatus = false;
            String status = order.getStatus() == null ? "" : order.getStatus().toUpperCase();

            if ("ALL".equals(statusFilter)) {
                matchesStatus = true;
            } else if ("PENDING".equals(statusFilter)) {
                matchesStatus = Constants.isOrderConfirmed(status);
            } else if ("IN_TRANSIT".equals(statusFilter)) {
                matchesStatus = Constants.isOrderShipping(status);
            } else if ("SUCCESS".equals(statusFilter)) {
                matchesStatus = Constants.isOrderDelivered(status);
            } else if ("FAILED".equals(statusFilter)) {
                matchesStatus = Constants.isOrderCancelled(status) || Constants.isOrderReturn(status);
            }

            if (!matchesStatus) continue;

            // 2. Lọc theo nội dung tìm kiếm
            if (!searchQuery.isEmpty()) {
                String query = searchQuery.toLowerCase();
                String id = order.getId() != null ? order.getId().toLowerCase() : "";
                String name = order.getRecipientName() != null ? order.getRecipientName().toLowerCase() : "";
                String phone = order.getRecipientPhone() != null ? order.getRecipientPhone().toLowerCase() : "";
                String address = order.getRecipientAddress() != null ? order.getRecipientAddress().toLowerCase() : "";

                boolean matchesSearch = id.contains(query) || name.contains(query) || phone.contains(query) || address.contains(query);
                if (!matchesSearch) continue;
            }

            filtered.add(order);
        }

        adapter.submitList(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerOrders.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    /**
     * Lọc danh sách đơn theo tab:
     * - Active:  CONFIRMED, IN_TRANSIT
     * - History: DELIVERED, FAILED, RETURNED
     */
    private List<Order> filterByTab(List<Order> orders) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            String status = order.getStatus() == null ? "" : order.getStatus().toUpperCase();
            if (!isHistory) {
                if (Constants.isOrderConfirmed(status) || Constants.isOrderShipping(status)) {
                    result.add(order);
                }
            } else {
                if (Constants.isOrderDelivered(status)
                        || Constants.isOrderCancelled(status)
                        || Constants.isOrderReturn(status)) {
                    result.add(order);
                }
            }
        }
        return result;
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
