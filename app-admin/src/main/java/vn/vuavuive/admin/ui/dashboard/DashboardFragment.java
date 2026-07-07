package vn.vuavuive.admin.ui.dashboard;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import vn.vuavuive.shared.data.api.DashboardApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.DashboardStats;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.CurrencyFormatter;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";

    @Inject AdminOrderApi adminOrderApi;
    @Inject AdminProductApi adminProductApi;
    @Inject DashboardApi dashboardApi;
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
        loadFirebaseStats();
        binding.tvStatPending.setText(stats.getPendingCount() + " đơn");

        // Fetch real pending orders
        adminOrderApi.getOrders("pending", 0, 3, null, null).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (!isUiReady()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    orderAdapter.updateData(response.body().getData());
                }
                binding.swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                Log.e(TAG, "load pending orders failed", t);
                showLoadError();
            }
        });

        // Fetch real low stock products
        adminProductApi.getAllProducts(1, 100, "", "all").enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (!isUiReady()) return;
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
                binding.swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e(TAG, "load low stock products failed", t);
                showLoadError();
            }
        });
    }

    private void loadFirebaseStats() {
        dashboardApi.getStats().enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<DashboardStats>> call, @NonNull Response<ApiResponse<DashboardStats>> response) {
                if (!isUiReady()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindStats(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<DashboardStats>> call, @NonNull Throwable t) {
                Log.e(TAG, "load dashboard stats failed", t);
            }
        });
    }

    private void bindStats(DashboardStats stats) {
        binding.tvStatOrders.setText(stats.getTotalOrders() + " đơn");
        binding.tvStatRevenue.setText(CurrencyFormatter.formatVnd(stats.getTotalRevenue()));
        binding.tvStatUsers.setText(stats.getTotalUsers() + " users");
        binding.tvStatPending.setText(stats.getPendingCount() + " đơn");
    }

    private boolean isUiReady() {
        return isAdded() && binding != null;
    }

    private void showLoadError() {
        if (!isUiReady()) return;
        binding.swipeRefresh.setRefreshing(false);
        Toast.makeText(getContext(), "Khong tai duoc du lieu moi nhat", Toast.LENGTH_SHORT).show();
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
                if (currentUser == null) return;
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
                if (currentUser == null) return;
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
        if (!isUiReady() || currentUser == null) return;
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
                    List<Order> list = MockRepository.getInstance().getOrders();
                    StringBuilder csv = new StringBuilder("Mã Đơn,Khách Hàng,Tổng Tiền,Trạng Thái,Ngày Tạo\n");
                    for (Order o : list) {
                        String name = o.getRecipientName() != null ? o.getRecipientName() : "Không tên";
                        name = name.replace(",", " ");
                        String date = o.getCreatedAt() != null && o.getCreatedAt().length() >= 10 ? o.getCreatedAt().substring(0, 10) : "";
                        csv.append(String.format(java.util.Locale.US, "%s,%s,%.0f,%s,%s\n",
                                o.getOrderId() != null ? o.getOrderId() : o.getId(), name, o.getFinalAmount(), o.getStatus() != null ? o.getStatus() : "", date));
                    }
                    exportCsv("orders_export.csv", csv.toString());
                } else if (which == 1) { // Products
                    if ("audit".equals(currentUser.getRole())) {
                        Toast.makeText(getContext(), "Kiểm toán viên không có quyền xuất sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<Product> list = MockRepository.getInstance().getProducts();
                    StringBuilder csv = new StringBuilder("Mã Sản Phẩm,Tên Sản Phẩm,Giá Bán,Tồn Kho,Danh Mục\n");
                    for (Product p : list) {
                        String name = p.getName() != null ? p.getName().replace(",", " ") : "Sản phẩm";
                        String cat = p.getCategory() != null ? p.getCategory().replace(",", " ") : "";
                        csv.append(String.format(java.util.Locale.US, "%s,%s,%.0f,%d,%s\n",
                                p.getId(), name, p.getPrice(), p.getStock(), cat));
                    }
                    exportCsv("products_export.csv", csv.toString());
                } else if (which == 2) { // Users
                    if ("staff".equals(currentUser.getRole())) {
                        Toast.makeText(getContext(), "Nhân viên không có quyền xuất thành viên", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<User> list = MockRepository.getInstance().getUsers();
                    StringBuilder csv = new StringBuilder("ID,Tên,Email,Số Điện Thoại,Quyền,Hoạt Động\n");
                    for (User u : list) {
                        String name = u.getName() != null ? u.getName().replace(",", " ") : "Thành viên";
                        String email = u.getEmail() != null ? u.getEmail() : "";
                        String phone = u.getPhone() != null ? u.getPhone() : "";
                        csv.append(String.format(java.util.Locale.US, "%s,%s,%s,%s,%s,%b\n",
                                u.getId(), name, email, phone, u.getRole() != null ? u.getRole() : "", u.isActive()));
                    }
                    exportCsv("users_export.csv", csv.toString());
                }
            }
        });
        builder.show();
    }

    private void exportCsv(String filename, String content) {
        if (!isUiReady()) return;
        Context context = getContext();
        if (context == null) return;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(content.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(context, "Xuất báo cáo " + filename + " thành công! (đã đồng bộ vào folder exports/)", Toast.LENGTH_LONG).show();
                        MockRepository.getInstance().addAuditLog("Xuất báo cáo CSV", filename, "Xuất thành công tập tin " + filename);
                    }
                }
            }
            // Send to host exports/ directory via backend API
            sendExportToBackend(filename, content);
        } catch (Exception e) {
            Toast.makeText(context, "Lỗi khi lưu báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendExportToBackend(String filename, String content) {
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                org.json.JSONObject json = new org.json.JSONObject();
                json.put("filename", filename);
                json.put("content", content);

                okhttp3.RequestBody body = okhttp3.RequestBody.create(
                        json.toString(),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")
                );

                String baseUrl = vn.vuavuive.admin.BuildConfig.BASE_URL;
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(baseUrl + "/api/export")
                        .post(body)
                        .build();

                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        android.util.Log.i(TAG, "Gửi báo cáo lên backend thành công: " + filename);
                    } else {
                        android.util.Log.e(TAG, "Gửi báo cáo lên backend thất bại: " + response.code());
                    }
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Lỗi gửi báo cáo lên backend", e);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
