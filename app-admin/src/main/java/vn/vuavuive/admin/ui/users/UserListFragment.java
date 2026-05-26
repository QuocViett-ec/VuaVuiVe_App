package vn.vuavuive.admin.ui.users;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.vuavuive.admin.R;
import vn.vuavuive.admin.data.repository.MockRepository;
import vn.vuavuive.admin.databinding.FragmentUserListBinding;
import vn.vuavuive.shared.data.dto.User;

public class UserListFragment extends Fragment implements UserAdapter.OnUserStatusChangeListener {

    private FragmentUserListBinding binding;
    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private String searchQuery = "";
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = MockRepository.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Block staff from accessing User Management entirely
        if ("staff".equals(currentUser.getRole())) {
            Toast.makeText(getContext(), "Nhân viên không có quyền quản lý thành viên", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }

        setupRecyclerView();
        setupFiltersAndExport();
        loadUsers();
    }

    private void setupRecyclerView() {
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(new ArrayList<>(), this);
        binding.rvUsers.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> loadUsers());
    }

    private void setupFiltersAndExport() {
        binding.etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnExportUsersCsv.setOnClickListener(v -> {
            if ("audit".equals(currentUser.getRole())) {
                Toast.makeText(getContext(), "Kiểm toán viên không có quyền xuất thành viên", Toast.LENGTH_SHORT).show();
                return;
            }
            exportUsersCsv();
        });
    }

    private void loadUsers() {
        allUsers = new ArrayList<>(MockRepository.getInstance().getUsers());
        applyFilters();
        binding.swipeRefresh.setRefreshing(false);
    }

    private void applyFilters() {
        List<User> filteredList = new ArrayList<>();
        for (User u : allUsers) {
            boolean matchesQuery = false;
            if (searchQuery.isEmpty()) {
                matchesQuery = true;
            } else {
                String q = searchQuery.toLowerCase(Locale.getDefault());
                String name = u.getName() != null ? u.getName().toLowerCase() : "";
                String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                String phone = u.getPhone() != null ? u.getPhone().toLowerCase() : "";

                if (name.contains(q) || email.contains(q) || phone.contains(q)) {
                    matchesQuery = true;
                }
            }

            if (matchesQuery) {
                filteredList.add(u);
            }
        }
        adapter.updateData(filteredList);
    }

    // Callbacks from UserAdapter
    @Override
    public void onUserStatusChanged(User user, boolean active) {
        if ("audit".equals(currentUser.getRole())) {
            Toast.makeText(getContext(), "Kiểm toán viên chỉ được quyền xem (Read-Only)", Toast.LENGTH_SHORT).show();
            // Re-load to undo the switch UI toggle
            loadUsers();
            return;
        }

        user.setActive(active);
        MockRepository.getInstance().updateUser(user);
        Toast.makeText(getContext(), "Đã cập nhật trạng thái hoạt động: " + (active ? "KÍCH HOẠT" : "KHÓA"), Toast.LENGTH_SHORT).show();
        loadUsers();
    }

    // Double functionality: click to change role (admins only)
    public void onUserClick(User user) {
        if ("audit".equals(currentUser.getRole())) {
            Toast.makeText(getContext(), "Kiểm toán viên không có quyền chỉnh sửa thành viên", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] roles = {"admin", "staff", "audit", "user"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thay đổi phân quyền thành viên");
        
        // Find current selection
        int currentSelection = 3; // default 'user'
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equalsIgnoreCase(user.getRole())) {
                currentSelection = i;
                break;
            }
        }

        builder.setSingleChoiceItems(roles, currentSelection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.setRole(roles[which]);
                MockRepository.getInstance().updateUser(user);
                Toast.makeText(getContext(), "Đã chuyển quyền thành: " + roles[which].toUpperCase(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadUsers();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void exportUsersCsv() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Mã TV,Họ Tên,Email,Số Điện Thoại,Quyền Hạn,Hoạt Động,Ngày Tạo\n");

            for (User u : allUsers) {
                boolean matchesQuery = searchQuery.isEmpty() ||
                        (u.getName() != null && u.getName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchQuery.toLowerCase()));

                if (matchesQuery) {
                    csv.append(String.format("%s,%s,%s,%s,%s,%b,%s\n",
                            u.getId(),
                            u.getName().replace(",", " -"),
                            u.getEmail(),
                            u.getPhone() != null ? u.getPhone() : "N/A",
                            u.getRole().toUpperCase(),
                            u.isActive(),
                            u.getCreatedAt() != null ? u.getCreatedAt() : "N/A"));
                }
            }

            String filename = "users_export_" + System.currentTimeMillis() + ".csv";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = getContext().getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(getContext(), "Đã xuất báo cáo " + filename + "!", Toast.LENGTH_LONG).show();
                        MockRepository.getInstance().addAuditLog("Xuất báo cáo thành viên", filename, "Tải xuống thành công " + filename);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi lưu CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
