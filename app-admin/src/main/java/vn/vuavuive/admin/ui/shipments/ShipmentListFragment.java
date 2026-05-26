package vn.vuavuive.admin.ui.shipments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentShipmentListBinding;
import vn.vuavuive.shared.data.dto.Shipment;

public class ShipmentListFragment extends Fragment implements ShipmentAdapter.OnShipmentClickListener {

    private FragmentShipmentListBinding binding;
    private ShipmentAdapter adapter;
    private List<Shipment> allShipments = new ArrayList<>();
    
    private String carrierFilter = "all";
    private String statusFilter = "all";

    private static final String[] CARRIER_NAMES = {
            "Tất cả đơn vị", "Nội bộ Vựa Vui Vẻ", "Giao Hàng Nhanh (GHN)"
    };
    private static final String[] CARRIER_KEYS = {
            "all", "internal", "external"
    };

    private static final String[] STATUS_NAMES = {
            "Tất cả trạng thái", "Chờ lấy hàng (Pending)", "Đang giao hàng (Shipping)", "Đã giao (Delivered)", "Thất bại/Hủy"
    };
    private static final String[] STATUS_KEYS = {
            "all", "pending", "shipping", "delivered", "failed"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShipmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinners();
        setupRecyclerView();
        loadShipments();
    }

    private void setupSpinners() {
        // Carrier Spinner
        ArrayAdapter<String> carrierAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, CARRIER_NAMES);
        carrierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterCarrier.setAdapter(carrierAdapter);
        binding.spinnerFilterCarrier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                carrierFilter = CARRIER_KEYS[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Status Spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, STATUS_NAMES);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilterShipmentStatus.setAdapter(statusAdapter);
        binding.spinnerFilterShipmentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statusFilter = STATUS_KEYS[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        binding.rvShipments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShipmentAdapter(new ArrayList<>(), this);
        binding.rvShipments.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> loadShipments());
    }

    private void loadShipments() {
        allShipments = new ArrayList<>(MockRepository.getInstance().getShipments());
        applyFilters();
        binding.swipeRefresh.setRefreshing(false);
    }

    private void applyFilters() {
        List<Shipment> filtered = new ArrayList<>();

        for (Shipment s : allShipments) {
            // Carrier filter
            boolean carrierMatches = "all".equals(carrierFilter) || carrierFilter.equalsIgnoreCase(s.getCarrier());

            // Status filter
            boolean statusMatches = false;
            if ("all".equals(statusFilter)) {
                statusMatches = true;
            } else if ("pending".equals(statusFilter)) {
                statusMatches = "pending".equalsIgnoreCase(s.getCurrentStatus());
            } else if ("shipping".equals(statusFilter)) {
                statusMatches = "shipping".equalsIgnoreCase(s.getCurrentStatus()) || "shipped".equalsIgnoreCase(s.getCurrentStatus());
            } else if ("delivered".equals(statusFilter)) {
                statusMatches = "delivered".equalsIgnoreCase(s.getCurrentStatus());
            } else if ("failed".equals(statusFilter)) {
                statusMatches = "failed".equalsIgnoreCase(s.getCurrentStatus()) || "cancelled".equalsIgnoreCase(s.getCurrentStatus());
            }

            if (carrierMatches && statusMatches) {
                filtered.add(s);
            }
        }
        adapter.updateData(filtered);
    }

    // Callback from ShipmentAdapter
    @Override
    public void onShipmentClick(Shipment shipment) {
        Intent intent = new Intent(getContext(), ShipmentDetailActivity.class);
        intent.putExtra("SHIPMENT_ID", shipment.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadShipments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
