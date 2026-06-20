package vn.vuavuive.shipper.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import vn.vuavuive.shipper.ui.order.ShipperOrderListFragment;

/**
 * ShipperPagerAdapter — Cung cấp 2 Fragment cho tabs của ShipperMainActivity:
 *   0: ShipperOrderListFragment(assigned)  — Đơn đang được giao
 *   1: ShipperOrderListFragment(history)   — Lịch sử giao hàng
 */
public class ShipperPagerAdapter extends FragmentStateAdapter {

    public ShipperPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ShipperOrderListFragment.newInstance(false); // assigned/active orders
        } else {
            return ShipperOrderListFragment.newInstance(true);  // history
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
