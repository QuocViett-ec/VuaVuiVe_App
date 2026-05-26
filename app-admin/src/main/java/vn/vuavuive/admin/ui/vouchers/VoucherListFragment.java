package vn.vuavuive.admin.ui.vouchers;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentVoucherListBinding;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.Voucher;

public class VoucherListFragment extends Fragment implements VoucherAdapter.OnVoucherClickListener {

    private FragmentVoucherListBinding binding;
    private VoucherAdapter adapter;
    private List<Voucher> allVouchers = new ArrayList<>();
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVoucherListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = MockRepository.getInstance().getCurrentUser();
        if (currentUser == null) return;

        setupRecyclerView();
        setupFab();
        loadVouchers();
    }

    private void setupRecyclerView() {
        binding.rvVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VoucherAdapter(new ArrayList<>(), this);
        binding.rvVouchers.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> loadVouchers());
    }

    private void setupFab() {
        // Only admin can add/edit vouchers. Staff & Audit can only view them.
        if (!"admin".equals(currentUser.getRole())) {
            binding.fabAddVoucher.setVisibility(View.GONE);
        }

        binding.fabAddVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), VoucherEditActivity.class);
            startActivity(intent);
        });
    }

    private void loadVouchers() {
        allVouchers = new ArrayList<>(MockRepository.getInstance().getVouchers());
        adapter.updateData(allVouchers);
        binding.swipeRefresh.setRefreshing(false);
    }

    // Callback from VoucherAdapter
    @Override
    public void onVoucherClick(Voucher voucher) {
        if (!"admin".equals(currentUser.getRole())) {
            Toast.makeText(getContext(), "Bạn chỉ được quyền xem chi tiết khuyến mãi (Read-Only)", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getContext(), VoucherEditActivity.class);
        intent.putExtra("VOUCHER_CODE", voucher.getCode());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVouchers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
