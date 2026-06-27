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
import javax.inject.Inject;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;
import vn.vuavuive.shipper.ui.auth.ShipperLoginActivity;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.SessionManager;

/**
 * ShipperMainActivity — Màn hình chính cho vai trò SHIPPER (Firebase-based).
 *
 * Tính năng:
 * - Header: Tên tài xế + Toggle trạng thái Online/Offline → ghi vào /users/{uid}/onlineStatus
 * - Tab 1: "Đơn cần giao" — Danh sách đơn hàng đang active
 * - Tab 2: "Lịch sử"     — Danh sách đơn đã hoàn thành
 */
@AndroidEntryPoint
public class ShipperMainActivity extends AppCompatActivity {

    @Inject SessionManager sessionManager;
    @Inject FirebaseShipperRepository repository;

    private static final String[] TAB_TITLES = {"Cần giao", "Lịch sử", "Thống kê", "Cá nhân"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra session Firebase: currentUser != null + role SHIPPER
        if (!repository.isLoggedIn()) {
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
        User user = sessionManager.getUser();
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
            repository.logout(); // Firebase signOut + clearSession
            goToLogin();
        });
    }

    /**
     * Toggle Online/Offline → cập nhật /users/{uid}/onlineStatus trong Firebase RTDB.
     * Không cần gọi API getMyProfile() nữa vì uid lấy trực tiếp từ FirebaseAuth.
     */
    private void setupOnlineToggle() {
        SwitchMaterial switchOnline = findViewById(R.id.switch_online);
        TextView tvStatusLabel = findViewById(R.id.tv_status_label);

        switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvStatusLabel.setText(isChecked ? "Online" : "Offline");
            tvStatusLabel.setTextColor(isChecked
                    ? getColor(R.color.status_delivered)
                    : getColor(R.color.text_hint));

            // Ghi trạng thái vào Firebase
            repository.updateOnlineStatus(isChecked);
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, ShipperLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
