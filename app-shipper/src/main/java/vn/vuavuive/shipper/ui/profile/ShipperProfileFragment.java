package vn.vuavuive.shipper.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import javax.inject.Inject;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;
import vn.vuavuive.shipper.ui.auth.ShipperLoginActivity;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.util.SessionManager;

@AndroidEntryPoint
public class ShipperProfileFragment extends Fragment {

    @Inject SessionManager sessionManager;
    @Inject FirebaseShipperRepository repository;

    private TextView tvName, tvPhone, tvEmail, tvSuccessRate;
    private LinearProgressIndicator progressSuccessRate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shipper_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tv_profile_name);
        tvPhone = view.findViewById(R.id.tv_profile_phone);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvSuccessRate = view.findViewById(R.id.tv_success_rate);
        progressSuccessRate = view.findViewById(R.id.progress_success_rate);

        setupProfile();
        setupLogout(view);
        observePerformance();
    }

    private void setupProfile() {
        User user = sessionManager.getUser();
        if (user != null) {
            tvName.setText(user.getName() != null ? user.getName() : "Tài Xế");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
            tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Chưa cập nhật");
        }
    }

    private void setupLogout(View view) {
        view.findViewById(R.id.btn_profile_logout).setOnClickListener(v -> {
            repository.logout();
            goToLogin();
        });
    }

    private void observePerformance() {
        repository.getMyOrders().observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.status != FirebaseShipperRepository.Result.Status.SUCCESS) {
                return;
            }

            List<Order> orders = result.data;
            if (orders == null) return;

            int success = 0;
            int failed = 0;

            for (Order order : orders) {
                String status = order.getStatus();
                if (status == null) continue;
                status = status.toUpperCase();

                if ("DELIVERED".equals(status)) {
                    success++;
                } else if ("FAILED".equals(status) || "RETURNED".equals(status)) {
                    failed++;
                }
            }

            int total = success + failed;
            int rate = 100;
            if (total > 0) {
                rate = (success * 100) / total;
            }

            tvSuccessRate.setText(rate + "%");
            progressSuccessRate.setProgress(rate);
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(requireActivity(), ShipperLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
