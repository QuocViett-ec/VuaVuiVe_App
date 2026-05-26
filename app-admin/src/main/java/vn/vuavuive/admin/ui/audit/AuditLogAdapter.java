package vn.vuavuive.admin.ui.audit;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.vuavuive.admin.databinding.ItemAuditLogBinding;
import vn.vuavuive.admin.data.repository.MockRepository.AuditLog;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.AuditViewHolder> {

    private List<AuditLog> auditLogs;

    public AuditLogAdapter(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    public void updateData(List<AuditLog> newLogs) {
        this.auditLogs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuditLogBinding binding = ItemAuditLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AuditViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
        holder.bind(auditLogs.get(position));
    }

    @Override
    public int getItemCount() {
        return auditLogs.size();
    }

    class AuditViewHolder extends RecyclerView.ViewHolder {
        private final ItemAuditLogBinding binding;

        public AuditViewHolder(ItemAuditLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AuditLog log) {
            binding.tvAuditAction.setText(log.action);
            binding.tvAuditTime.setText(log.timestamp);
            binding.tvAuditOperator.setText("Thực hiện bởi: " + log.operatorName + " (" + log.role.toUpperCase() + ")");
            binding.tvAuditTarget.setText("Đối tượng: " + log.target);
            binding.tvAuditDetails.setText("Chi tiết: " + log.details);

            // Dynamically color action badge for maximum visual clarity
            setupActionBadgeColor(log.action);
        }

        private void setupActionBadgeColor(String action) {
            int bgTint, textColor;
            String cleanAction = action != null ? action.toLowerCase() : "";

            if (cleanAction.contains("xóa")) {
                bgTint = Color.parseColor("#33F44336"); // Red
                textColor = Color.parseColor("#F44336");
            } else if (cleanAction.contains("thêm") || cleanAction.contains("tạo")) {
                bgTint = Color.parseColor("#334CAF50"); // Green
                textColor = Color.parseColor("#4CAF50");
            } else if (cleanAction.contains("sửa") || cleanAction.contains("cập nhật")) {
                bgTint = Color.parseColor("#33FF9800"); // Orange
                textColor = Color.parseColor("#FF9800");
            } else if (cleanAction.contains("đăng nhập")) {
                bgTint = Color.parseColor("#3300BCD4"); // Cyan
                textColor = Color.parseColor("#00BCD4");
            } else if (cleanAction.contains("xuất")) {
                bgTint = Color.parseColor("#339C27B0"); // Purple
                textColor = Color.parseColor("#9C27B0");
            } else {
                bgTint = Color.parseColor("#332196F3"); // Blue
                textColor = Color.parseColor("#2196F3");
            }

            binding.tvAuditAction.setTextColor(textColor);
            binding.tvAuditAction.setBackgroundTintList(ColorStateList.valueOf(bgTint));
        }
    }
}
