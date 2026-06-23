package vn.vuavuive.customer.ui.shipment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.ShipmentViewModel;

@AndroidEntryPoint
public class ShipmentListActivity extends AppCompatActivity {

    private ShipmentViewModel shipmentViewModel;
    private ShipmentAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_list);

        shipmentViewModel = new ViewModelProvider(this).get(ShipmentViewModel.class);

        initViews();
        setupRecycler();
        loadShipments();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_shipments);
        adapter = new ShipmentAdapter(this, shipment -> {
            Intent intent = new Intent(this, ShipmentDetailActivity.class);
            intent.putExtra("shipment_id", shipment.getId());
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private void loadShipments() {
        progressBar.setVisibility(View.VISIBLE);
        shipmentViewModel.getMyShipments().observe(this, result -> {
            progressBar.setVisibility(View.GONE);
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                adapter.setItems(result.data);
                layoutEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
