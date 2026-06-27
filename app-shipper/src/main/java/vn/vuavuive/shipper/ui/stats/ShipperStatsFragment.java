package vn.vuavuive.shipper.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import vn.vuavuive.shipper.R;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.PaymentDetail;

@AndroidEntryPoint
public class ShipperStatsFragment extends Fragment {

    @Inject FirebaseShipperRepository repository;

    private TextView tvTotalRevenue, tvTotalOrders;
    private TextView tvCodAmount, tvOnlineAmount;
    private TextView tvSuccessCount, tvFailedCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shipper_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvTotalOrders  = view.findViewById(R.id.tv_total_orders);
        tvCodAmount    = view.findViewById(R.id.tv_cod_amount);
        tvOnlineAmount = view.findViewById(R.id.tv_online_amount);
        tvSuccessCount = view.findViewById(R.id.tv_success_count);
        tvFailedCount  = view.findViewById(R.id.tv_failed_count);

        observeStats();
    }

    private void observeStats() {
        repository.getMyOrders().observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.status != FirebaseShipperRepository.Result.Status.SUCCESS) {
                return;
            }

            List<Order> orders = result.data;
            if (orders == null) return;

            long totalRevenue = 0;
            long codRevenue = 0;
            long onlineRevenue = 0;
            int successCount = 0;
            int failedCount = 0;

            for (Order order : orders) {
                String status = order.getStatus();
                if (status == null) continue;
                status = status.toUpperCase();

                if ("DELIVERED".equals(status)) {
                    successCount++;
                    long amount = (long) order.getFinalAmount();
                    totalRevenue += amount;

                    PaymentDetail pmt = order.getPayment();
                    if (pmt != null && "momo".equalsIgnoreCase(pmt.getMethod()) && "paid".equalsIgnoreCase(pmt.getStatus())) {
                        onlineRevenue += amount;
                    } else {
                        codRevenue += amount;
                    }
                } else if ("FAILED".equals(status) || "RETURNED".equals(status)) {
                    failedCount++;
                }
            }

            // Bind values to UI
            NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

            tvTotalRevenue.setText(fmt.format(totalRevenue) + " đ");
            tvTotalOrders.setText("Đã hoàn thành " + successCount + " đơn hàng");
            tvCodAmount.setText(fmt.format(codRevenue) + " đ");
            tvOnlineAmount.setText(fmt.format(onlineRevenue) + " đ");
            tvSuccessCount.setText(successCount + " đơn");
            tvFailedCount.setText(failedCount + " đơn");
        });
    }
}
