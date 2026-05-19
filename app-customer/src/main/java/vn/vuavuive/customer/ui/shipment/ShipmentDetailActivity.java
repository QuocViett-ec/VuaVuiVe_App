package vn.vuavuive.customer.ui.shipment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.order.OrderDetailActivity;
import vn.vuavuive.customer.ui.order.OrderItemAdapter;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.customer.viewmodel.ShipmentViewModel;
import vn.vuavuive.shared.data.dto.Shipment;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.Collections;

@AndroidEntryPoint
public class ShipmentDetailActivity extends AppCompatActivity {

    private ShipmentViewModel shipmentViewModel;
    private OrderViewModel orderViewModel;
    private ShipmentStatusAdapter statusAdapter;
    private OrderItemAdapter orderItemAdapter;

    private TextView tvTracking, tvCarrier, tvStatus;
    private TextView tvEta, tvDeliveredAt, tvFee, tvEmpty;
    private TextView tvOrderItemsEmpty;
    private View btnViewOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_detail);

        shipmentViewModel = new ViewModelProvider(this).get(ShipmentViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        initViews();
        setupRecycler();

        String shipmentId = getIntent().getStringExtra("shipment_id");
        if (shipmentId != null) {
            loadShipment(shipmentId);
        } else {
            finish();
        }
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvTracking = findViewById(R.id.tv_tracking);
        tvCarrier = findViewById(R.id.tv_carrier);
        tvStatus = findViewById(R.id.tv_status);
        tvEta = findViewById(R.id.tv_eta);
        tvDeliveredAt = findViewById(R.id.tv_delivered_at);
        tvFee = findViewById(R.id.tv_fee);
        tvEmpty = findViewById(R.id.tv_empty);
        tvOrderItemsEmpty = findViewById(R.id.tv_order_items_empty);
        btnViewOrder = findViewById(R.id.btn_view_order);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_status);
        statusAdapter = new ShipmentStatusAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(statusAdapter);
        rv.setNestedScrollingEnabled(false);

        RecyclerView rvItems = findViewById(R.id.rv_order_items);
        orderItemAdapter = new OrderItemAdapter(this, item -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", item.getProductId());
            startActivity(intent);
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(orderItemAdapter);
        rvItems.setNestedScrollingEnabled(false);
    }

    private void loadShipment(String shipmentId) {
        shipmentViewModel.getShipmentDetail(shipmentId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                bindShipment(result.data);
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindShipment(Shipment shipment) {
        tvTracking.setText(shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "--");
        tvCarrier.setText("Nha van chuyen: " + safe(shipment.getCarrier()));

        String statusLabel = ShipmentUiMapper.getStatusLabel(shipment.getCurrentStatus());
        tvStatus.setText(statusLabel);
        tvStatus.setTextColor(getResources().getColor(ShipmentUiMapper.getStatusColor(shipment.getCurrentStatus()), null));

        tvEta.setText("ETA: " + safe(shipment.getEta()));
        tvDeliveredAt.setText("Da giao: " + safe(shipment.getDeliveredAt()));
        tvFee.setText("Phi van chuyen: " + CurrencyFormatter.format(shipment.getShippingFee()));

        if (shipment.getStatusHistory() != null && !shipment.getStatusHistory().isEmpty()) {
            statusAdapter.setItems(shipment.getStatusHistory());
            tvEmpty.setVisibility(View.GONE);
        } else {
            statusAdapter.setItems(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
        }

        if (shipment.getOrderId() != null && !shipment.getOrderId().isEmpty()) {
            btnViewOrder.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderDetailActivity.class);
                intent.putExtra("order_id", shipment.getOrderId());
                startActivity(intent);
            });
            loadOrderItems(shipment.getOrderId());
        } else {
            btnViewOrder.setVisibility(View.GONE);
        }
    }

    private void loadOrderItems(String orderId) {
        orderViewModel.getOrderDetail(orderId).observe(this, result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null
                    && result.data.getItems() != null && !result.data.getItems().isEmpty()) {
                orderItemAdapter.setItems(result.data.getItems());
                tvOrderItemsEmpty.setVisibility(View.GONE);
            } else {
                orderItemAdapter.setItems(Collections.emptyList());
                tvOrderItemsEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private String safe(String value) {
        return value != null && !value.isEmpty() ? value : "--";
    }
}
