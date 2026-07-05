package vn.vuavuive.admin.ui.orders;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.tabs.TabLayout;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentAdminOrderListBinding;
import vn.vuavuive.shared.data.api.AdminOrderApi;
import vn.vuavuive.shared.data.api.OrderStatusApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.Constants;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class AdminOrderListFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    @Inject AdminOrderApi adminOrderApi;
    @Inject OrderStatusApi orderStatusApi;
    @Inject SessionManager sessionManager;

    private FragmentAdminOrderListBinding binding;
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private String currentStatusFilter = "all";
    private String currentSearchQuery = "";
    private User currentUser;
    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrderListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = sessionManager.getUser();
        if (currentUser == null) return;
        if (currentUser.getRole() != null) currentUser.setRole(currentUser.getRole().toLowerCase(Locale.getDefault()));

        setupTabs();
        setupRecyclerView();
        setupSearchAndActions();
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tất cả").setTag("all"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chờ duyệt").setTag("pending"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã xác nhận").setTag("confirmed"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đang giao").setTag("in_transit"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã giao").setTag("delivered"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã hủy").setTag("cancelled"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Trả hàng").setTag("returns"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Object tag = tab.getTag();
                currentStatusFilter = tag instanceof String ? (String) tag : "all";
                applyFilters();
                // Disable bulk mode if status changed to prevent state leakage
                if (adapter.isMultiSelectMode()) {
                    adapter.setMultiSelectMode(false);
                    binding.layoutBulkActions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(new ArrayList<>(), this);
        binding.rvOrders.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadOrders();
        });
    }

    private void setupSearchAndActions() {
        // Real-time Search
        binding.etSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Export CSV
        binding.btnExportCsv.setOnClickListener(v -> {
            if ("audit".equals(currentUser.getRole())) {
                Toast.makeText(getContext(), "Kiểm toán viên không có quyền xuất dữ liệu hóa đơn", Toast.LENGTH_SHORT).show();
                return;
            }
            exportFilteredOrdersCsv();
        });

        // Bulk Actions Trigger
        binding.btnBulkStatus.setOnClickListener(v -> {
            showBulkUpdateDialog();
        });
    }

    private void loadOrders() {
        if (!isUiReady()) return;
        binding.swipeRefresh.setRefreshing(true);
        // Call the real API
        adminOrderApi.getOrders("all", 0, 100, null, null).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (!isUiReady()) return;
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    allOrders = new ArrayList<>(response.body().getData());
                    applyFilters();
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách đơn hàng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                if (!isUiReady()) return;
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (!isUiReady() || adapter == null) return;
        List<Order> filteredList = new ArrayList<>();
        for (Order o : allOrders) {
            String status = o.getStatus() == null ? "" : o.getStatus().toLowerCase(Locale.getDefault());
            // Status check
            boolean matchesStatus = false;
            if ("all".equals(currentStatusFilter)) {
                matchesStatus = true;
            } else if ("returns".equals(currentStatusFilter)) {
                matchesStatus = Constants.isOrderReturn(status) || o.getReturnRequest() != null;
            } else if ("in_transit".equals(currentStatusFilter)) {
                matchesStatus = Constants.isOrderShipping(status);
            } else if ("pending".equals(currentStatusFilter)) {
                matchesStatus = Constants.isOrderPending(status);
            } else if ("confirmed".equals(currentStatusFilter)) {
                matchesStatus = Constants.isOrderConfirmed(status);
            } else if ("cancelled".equals(currentStatusFilter)) {
                matchesStatus = Constants.isOrderCancelled(status);
            } else {
                matchesStatus = currentStatusFilter.equals(status);
            }

            // Search query check (Order ID or Customer name or phone)
            boolean matchesQuery = false;
            if (currentSearchQuery.isEmpty()) {
                matchesQuery = true;
            } else {
                String q = currentSearchQuery.toLowerCase(Locale.getDefault());
                String rawId = o.getOrderId() != null ? o.getOrderId() : o.getId();
                String id = rawId != null ? rawId.toLowerCase(Locale.getDefault()) : "";
                String name = o.getRecipientName() != null ? o.getRecipientName().toLowerCase(Locale.getDefault()) : "";
                String phone = o.getRecipientPhone() != null ? o.getRecipientPhone() : "";

                if (id.contains(q) || name.contains(q) || phone.contains(q)) {
                    matchesQuery = true;
                }
            }

            if (matchesStatus && matchesQuery) {
                filteredList.add(o);
            }
        }
        adapter.updateData(filteredList);
    }

    private boolean isUiReady() {
        return isAdded() && binding != null;
    }

    // Callbacks from Adapter
    @Override
    public void onOrderClick(Order order) {
        // Let Adapter handle starting detail activity directly, or launch it manually:
        android.content.Intent intent = new android.content.Intent(getContext(), AdminOrderDetailActivity.class);
        intent.putExtra("ORDER_ID", order.getId());
        startActivity(intent);
    }

    @Override
    public void onOrderSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            binding.layoutBulkActions.setVisibility(View.VISIBLE);
            binding.tvBulkCount.setText("Đã chọn: " + selectedCount + " đơn hàng");
        } else {
            binding.layoutBulkActions.setVisibility(View.GONE);
            adapter.setMultiSelectMode(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // ponytail: reuse the existing mapper; read directly if this extra fetch becomes measurable.
                loadOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isUiReady()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        ordersRef.addValueEventListener(ordersListener);
    }

    @Override
    public void onStop() {
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
            ordersListener = null;
            ordersRef = null;
        }
        super.onStop();
    }

    private void showBulkUpdateDialog() {
        if ("audit".equals(currentUser.getRole())) {
            Toast.makeText(getContext(), "Kiểm toán viên không có quyền cập nhật trạng thái đơn", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] options = {"Xác nhận đơn", "Hủy đơn"};
        final String[] statusCodes = {"confirmed", "cancelled"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn trạng thái cập nhật hàng loạt");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String targetStatus = statusCodes[which];
                List<String> selectedIds = new ArrayList<>(adapter.getSelectedOrderIds());
                if (selectedIds.isEmpty()) return;
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failCount = new AtomicInteger(0);
                int total = selectedIds.size();
                
                for (String id : selectedIds) {
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("status", targetStatus);
                    body.put("updatedBy", currentUser.getName() != null ? currentUser.getName() : "Admin");
                    orderStatusApi.updateStatus(id, body).enqueue(new Callback<ApiResponse<Order>>() {
                        @Override public void onResponse(@NonNull Call<ApiResponse<Order>> call,
                                                         @NonNull Response<ApiResponse<Order>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                successCount.incrementAndGet();
                            } else {
                                failCount.incrementAndGet();
                            }
                            finishBulkUpdate(successCount, failCount, total);
                        }
                        @Override public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                            failCount.incrementAndGet();
                            finishBulkUpdate(successCount, failCount, total);
                        }
                    });
                }

                // Reset Selection
                adapter.setMultiSelectMode(false);
                binding.layoutBulkActions.setVisibility(View.GONE);
            }
        });
        builder.show();
    }

    private void finishBulkUpdate(AtomicInteger successCount, AtomicInteger failCount, int total) {
        if (successCount.get() + failCount.get() != total || !isUiReady()) return;
        requireActivity().runOnUiThread(() -> {
            if (!isUiReady()) return;
            Toast.makeText(getContext(),
                    "Cập nhật: " + successCount.get() + " thành công, " + failCount.get() + " thất bại",
                    Toast.LENGTH_LONG).show();
            loadOrders();
        });
    }

    private void exportFilteredOrdersCsv() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Mã Đơn,Khách Hàng,Số Điện Thoại,Địa Chỉ,Tổng Tiền,Trạng Thái,Ngày Tạo\n");
            
            // Build CSV based on currently filtered items
            List<Order> currentItems = adapter != null ? adapter.getCurrentItems() : new ArrayList<>();
            for (Order o : currentItems) {
                if (o != null) {
                    String name = o.getRecipientName() != null ? o.getRecipientName() : "N/A";
                    String phone = o.getRecipientPhone() != null ? o.getRecipientPhone() : "N/A";
                    String address = o.getRecipientAddress() != null ? o.getRecipientAddress().replace(",", " -") : "N/A";
                    csv.append(String.format("%s,%s,%s,%s,%.0f,%s,%s\n",
                            o.getOrderId() != null ? o.getOrderId() : o.getId(),
                            name, phone, address, o.getFinalAmount(), o.getStatus(), o.getCreatedAt()));
                }
            }

            String filename = "orders_filtered_" + System.currentTimeMillis() + ".csv";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Context context = getContext();
            if (context == null) return;
            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(getContext(), "Đã lưu " + filename + " vào Downloads folder!", Toast.LENGTH_LONG).show();
                        MockRepository.getInstance().addAuditLog("Xuất báo cáo đơn hàng", filename, "Xuất thành công báo cáo đơn hàng CSV");
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi lưu CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
