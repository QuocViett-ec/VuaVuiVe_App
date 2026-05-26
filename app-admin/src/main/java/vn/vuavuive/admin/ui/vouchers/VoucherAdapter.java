package vn.vuavuive.admin.ui.vouchers;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.vuavuive.admin.databinding.ItemVoucherBinding;
import vn.vuavuive.shared.data.dto.Voucher;
import vn.vuavuive.shared.util.CurrencyFormatter;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> vouchers;
    private final OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
    }

    public VoucherAdapter(List<Voucher> vouchers, OnVoucherClickListener listener) {
        this.vouchers = vouchers;
        this.listener = listener;
    }

    public void updateData(List<Voucher> newVouchers) {
        this.vouchers = newVouchers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVoucherBinding binding = ItemVoucherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VoucherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        holder.bind(vouchers.get(position));
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private final ItemVoucherBinding binding;

        public VoucherViewHolder(ItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Voucher voucher) {
            binding.tvVoucherCode.setText(voucher.getCode());
            binding.tvVoucherNote.setText(voucher.getNote() != null ? voucher.getNote() : "Không có mô tả");

            // Format discount value based on type
            String discountText = "";
            if (voucher.isShipVoucher()) {
                discountText = "Miễn phí vận chuyển (" + CurrencyFormatter.formatVnd(voucher.getValue()) + ")";
            } else if (voucher.isPercentVoucher()) {
                discountText = "Giảm " + (int) voucher.getValue() + "%" + 
                        (voucher.getCap() > 0 ? " (Tối đa " + CurrencyFormatter.formatVnd(voucher.getCap()) + ")" : "");
            } else {
                discountText = "Giảm trực tiếp " + CurrencyFormatter.formatVnd(voucher.getValue());
            }
            binding.tvVoucherValue.setText(discountText);

            // Date formats
            String start = voucher.getStartsAt() != null ? voucher.getStartsAt().split("T")[0] : "";
            String expire = voucher.getExpiresAt() != null ? voucher.getExpiresAt().split("T")[0] : "";
            binding.tvVoucherExpiry.setText("Hạn dùng: " + start + " đến " + expire);

            // Status indicator
            if (voucher.isActive()) {
                binding.tvVoucherStatus.setText("ACTIVE");
                binding.tvVoucherStatus.setTextColor(Color.parseColor("#4CAF50"));
                binding.tvVoucherStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#334CAF50")));
            } else {
                binding.tvVoucherStatus.setText("EXPIRED");
                binding.tvVoucherStatus.setTextColor(Color.parseColor("#F44336"));
                binding.tvVoucherStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#33F44336")));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherClick(voucher);
                } else {
                    Intent intent = new Intent(itemView.getContext(), VoucherEditActivity.class);
                    intent.putExtra("VOUCHER_CODE", voucher.getCode());
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
