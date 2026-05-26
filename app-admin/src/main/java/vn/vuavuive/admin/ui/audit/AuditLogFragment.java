package vn.vuavuive.admin.ui.audit;

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
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.data.repository.MockRepository.AuditLog;
import vn.vuavuive.admin.databinding.FragmentAuditLogBinding;
import vn.vuavuive.shared.data.dto.User;

public class AuditLogFragment extends Fragment {

    private FragmentAuditLogBinding binding;
    private AuditLogAdapter adapter;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAuditLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = MockRepository.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Block staff from accessing Audit Logs entirely
        if ("staff".equals(currentUser.getRole())) {
            Toast.makeText(getContext(), "Nhân viên không có quyền xem nhật ký hoạt động", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        setupRecyclerView();
        loadAuditLogs();
    }

    private void setupRecyclerView() {
        binding.rvAuditLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuditLogAdapter(new ArrayList<>());
        binding.rvAuditLogs.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> loadAuditLogs());
    }

    private void loadAuditLogs() {
        List<AuditLog> logs = MockRepository.getInstance().getAuditLogs();
        adapter.updateData(logs);
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
