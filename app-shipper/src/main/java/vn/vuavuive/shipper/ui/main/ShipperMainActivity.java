package vn.vuavuive.shipper.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.ui.auth.ShipperLoginActivity;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.ShipperProfile;
import vn.vuavuive.shared.util.SessionManager;
import javax.inject.Inject;

/**
 * ShipperMainActivity — Màn hình chính cho vai trò SHIPPER.
 * Tự động điều hướng đến đây khi User đăng nhập với role SHIPPER.
 *
 * Tính năng:
 * - Header: Tên tài xế + Toggle trạng thái Online/Offline
 * - Tab 1: "Đơn cần giao" — Danh sách đơn hàng đang được gán (PREPARING, IN_TRANSIT)
 * - Tab 2: "Lịch sử"     — Danh sách đơn đã hoàn thành (DELIVERED, FAILED, RETURNED)
 */
@AndroidEntryPoint
public class ShipperMainActivity extends AppCompatActivity {

    @Inject SessionManager sessionManager;
    @Inject ShipperOrderApi shipperOrderApi;

    private static final String[] TAB_TITLES = {"Can giao", "Lich su"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!sessionManager.isLoggedIn() || !sessionManager.isShipper() || !sessionManager.hasValidAccessToken()) {
            goToLogin();
            return;
        }
        setContentView(R.layout.activity_shipper_main);

        setupHeader();
        setupTabs();
        setupLogout();
        setupOnlineToggle();
    }

    private void setupHeader() {
        TextView tvName = findViewById(R.id.tv_shipper_name);
        vn.vuavuive.shared.data.dto.User user = sessionManager.getUser();
        if (user != null && user.getName() != null) {
            tvName.setText(user.getName());
        }
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        ShipperPagerAdapter adapter = new ShipperPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();
    }

    private void setupLogout() {
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            sessionManager.clearSession();
            goToLogin();
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, ShipperLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupOnlineToggle() {
        SwitchMaterial switchOnline = findViewById(R.id.switch_online);
        TextView tvStatusLabel = findViewById(R.id.tv_status_label);
        vn.vuavuive.shared.data.dto.User user = sessionManager.getUser();

        switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "AVAILABLE" : "OFFLINE";
            tvStatusLabel.setText(isChecked ? "Online" : "Offline");
            tvStatusLabel.setTextColor(isChecked
                    ? getColor(R.color.status_delivered)
                    : getColor(R.color.text_hint));

            if (user == null) return;
            shipperOrderApi.getMyProfile().enqueue(new Callback<ApiResponse<ShipperProfile>>() {
                @Override
                public void onResponse(Call<ApiResponse<ShipperProfile>> call, Response<ApiResponse<ShipperProfile>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().getId() != null) {
                        shipperOrderApi.updateShipperStatus(response.body().getData().getId(), newStatus).enqueue(new Callback<ApiResponse<Void>>() {
                            @Override public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {}
                            @Override public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
                        });
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ShipperProfile>> call, Throwable t) {}
            });
        });
    }
}
