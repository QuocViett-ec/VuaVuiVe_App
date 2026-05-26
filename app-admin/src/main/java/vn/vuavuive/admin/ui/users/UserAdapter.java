package vn.vuavuive.admin.ui.users;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.vuavuive.admin.databinding.ItemUserBinding;
import vn.vuavuive.shared.data.dto.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private final OnUserStatusChangeListener listener;

    public interface OnUserStatusChangeListener {
        void onUserStatusChanged(User user, boolean active);
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users, OnUserStatusChangeListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void updateData(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.tvUserName.setText(user.getName());
            binding.tvUserEmail.setText(user.getEmail());
            binding.tvUserPhone.setText("SĐT: " + (user.getPhone() != null ? user.getPhone() : "N/A"));

            // Calculate elegant initials
            binding.tvUserInitials.setText(getInitials(user.getName()));

            // Setup Role Badge with harmonious colors
            setupRoleBadge(user.getRole());

            // Bind Switch state without triggering listener initially
            binding.switchUserActive.setOnCheckedChangeListener(null);
            binding.switchUserActive.setChecked(user.isActive());
            
            // Register state change listener
            binding.switchUserActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onUserStatusChanged(user, isChecked);
                }
            });

            // Card view click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }

        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty()) return "US";
            String[] words = name.trim().split("\\s+");
            if (words.length == 1) {
                return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
            } else {
                String first = words[0].substring(0, 1);
                String last = words[words.length - 1].substring(0, 1);
                return (first + last).toUpperCase();
            }
        }

        private void setupRoleBadge(String role) {
            int bgTint, textColor;
            String roleText = role != null ? role.toUpperCase() : "USER";

            switch (roleText) {
                case "ADMIN":
                    bgTint = Color.parseColor("#33F44336"); // Crimson Red translucent
                    textColor = Color.parseColor("#F44336");
                    break;
                case "STAFF":
                    bgTint = Color.parseColor("#3300BCD4"); // Cyan translucent
                    textColor = Color.parseColor("#00BCD4");
                    break;
                case "AUDIT":
                    bgTint = Color.parseColor("#339C27B0"); // Purple translucent
                    textColor = Color.parseColor("#9C27B0");
                    break;
                default:
                    bgTint = Color.parseColor("#332196F3"); // Blue translucent for regular users
                    textColor = Color.parseColor("#2196F3");
                    roleText = "CUSTOMER";
                    break;
            }

            binding.tvUserRole.setText(roleText);
            binding.tvUserRole.setTextColor(textColor);
            binding.tvUserRole.setBackgroundTintList(ColorStateList.valueOf(bgTint));
        }
    }
}
