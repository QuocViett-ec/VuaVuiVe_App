package vn.vuavuive.admin.ui.dashboard;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentDashboardBinding;
import vn.vuavuive.admin.ui.audit.AuditLogFragment;
import vn.vuavuive.admin.ui.main.MainActivity;
import vn.vuavuive.admin.ui.orders.AdminOrderListFragment;
import vn.vuavuive.admin.ui.orders.OrderAdapter;
import vn.vuavuive.admin.ui.products.AdminProductListFragment;
import vn.vuavuive.admin.ui.products.ProductAdapter;
import vn.vuavuive.admin.ui.shipments.ShipmentListFragment;
import vn.vuavuive.admin.ui.users.UserListFragment;
import vn.vuavuive.shared.data.api.AdminOrderApi;
import vn.vuavuive.shared.data.api.AdminProductApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.DashboardStats;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.CurrencyFormatter;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
    @Inject AdminOrderApi adminOrderApi;
    @Inject AdminProductApi adminProductApi;
    @Inject SessionManager sessionManager;

    private FragmentDashboardBinding binding;
    private User currentUser;
    private OrderAdapter orderAdapter;
    private ProductAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = sessionManager.getUser();
        if (currentUser == null) return;

        binding.tvWelcome.setText("Chào buổi chiều, " + currentUser.getName() + " 👋");

        setupRecyclerViews();
        loadDashboardData();
        setupListeners();
    }

    private void setupRecyclerViews() {
        binding.rvPendingOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLowStock.setLayoutManager(new LinearLayoutManager(getContext()));

        orderAdapter = new OrderAdapter(new ArrayList<>(), null); // no checkbox actions in dashboard
        productAdapter = new ProductAdapter(new ArrayList<>(), null); // no CRUD actions in dashboard

        binding.rvPendingOrders.setAdapter(orderAdapter);
        binding.rvLowStock.setAdapter(productAdapter);
    }

    private void loadDashboardData() {
        MockRepository repo = MockRepository.getInstance();
        DashboardStats stats = repo.getDashboardStats();

        // Bind main metrics (Keep Mock for stats because no Backend API for dashboard stats yet)
        binding.tvStatOrders.setText(stats.getTotalOrders() + " đơn");
        binding.tvStatRevenue.setText(CurrencyFormatter.formatVnd(stats.getTotalRevenue()));
        binding.tvStatUsers.setText(stats.getTotalUsers() + " users");
        binding.tvStatPending.setText(stats.getPendingCount() + " đơn");

        // Fetch real pending orders
        adminOrderApi.getOrders("pending", 0, 3, null, null).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    orderAdapter.updateData(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {}
        });

        // Fetch real low stock products
        adminProductApi.getAllProducts(1, 100, "", "all").enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> lowStock = new ArrayList<>();
                    for (Product p : response.body().getData()) {
                        if (p.getStock() <= 10 && p.isActive()) {
                            lowStock.add(p);
                            if (lowStock.size() >= 3) break;
                        }
                    }
                    productAdapter.updateData(lowStock);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {}
        });

        binding.swipeRefresh.setRefreshing(false);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDashboardData();
            }
        });

        // See all triggers
        binding.btnSeeAllOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToMenu(R.id.nav_orders);
                }
            }
        });

        binding.btnSeeAllProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToMenu(R.id.nav_products);
                }
            }
        });

        // Shortcut buttons clicks
        binding.btnShortcutUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("staff".equals(currentUser.getRole())) {
                    Toast.makeText(getContext(), "Nhân viên không có quyền quản lý thành viên", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(new UserListFragment());
                }
            }
        });

        binding.btnShortcutShipments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(new ShipmentListFragment());
                }
            }
        });

        binding.btnShortcutAudit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("staff".equals(currentUser.getRole())) {
                    Toast.makeText(getContext(), "Nhân viên không có quyền xem nhật ký hoạt động", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(new AuditLogFragment());
                }
            }
        });

        binding.btnShortcutReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExportDialog();
            }
        });
    }

    private void showExportDialog() {
        final String[] options = {"Báo cáo Đơn hàng", "Báo cáo Sản phẩm", "Báo cáo Thành viên"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xuất báo cáo CSV hệ thống");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { // Orders
                    if ("audit".equals(currentUser.getRole())) {
                        Toast.makeText(getContext(), "Kiểm toán viên không có quyền xuất hóa đơn", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    exportCsv("orders_export.csv", "Mã Đơn,Khách Hàng,Tổng Tiền,Trạng Thái,Ngày Tạo\n" +
                            "ORD-9843A,Phạm Minh Huy,220000,pending,2026-05-22\n" +
                            "ORD-1092F,Phạm Minh Huy,104000,confirmed,2026-05-21\n" +
                            "ORD-5743D,Đặng Minh Anh,143000,delivered,2026-05-20");
                } else if (which == 1) { // Products
                    if ("audit".equals(currentUser.getRole())) {
                        Toast.makeText(getContext(), "Kiểm toán viên không có quyền xuất sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    exportCsv("products_export.csv", "Mã Sản Phẩm,Tên Sản Phẩm,Giá Bán,Tồn Kho,Danh Mục\n" +
                            "prod-1,Cà chua bi hữu cơ Đà Lạt,35000,4,veg\n" +
                            "prod-2,Thịt ba rọi heo thảo mộc,145000,8,meat\n" +
                            "prod-3,Táo Envy Mỹ nhập khẩu,89000,45,fruit");
                } else if (which == 2) { // Users
                    if ("staff".equals(currentUser.getRole())) {
                        Toast.makeText(getContext(), "Nhân viên không có quyền xuất thành viên", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    exportCsv("users_export.csv", "ID,Tên,Email,Số Điện Thoại,Quyền,Hoạt Động\n" +
                            "usr-1,Lê Hoàng Admin,admin@vuavuive.vn,0901234567,admin,true\n" +
                            "usr-2,Trần Thị Nhân Viên,staff@vuavuive.vn,0912345678,staff,true");
                }
            }
        });
        builder.show();
    }

    private void exportCsv(String filename, String content) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = getContext().getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(content.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(getContext(), "Xuất báo cáo " + filename + " vào Downloads folder thành công!", Toast.LENGTH_LONG).show();
                        MockRepository.getInstance().addAuditLog("Xuất báo cáo CSV", filename, "Xuất thành công tập tin " + filename);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi lưu báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
