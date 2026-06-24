package vn.vuavuive.shipper.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import vn.vuavuive.shipper.ui.order.ShipperOrderListFragment;
import vn.vuavuive.shipper.ui.profile.ShipperProfileFragment;
import vn.vuavuive.shipper.ui.stats.ShipperStatsFragment;

/**
 * ShipperPagerAdapter — Cung cấp 4 Fragment cho tabs của ShipperMainActivity:
 *   0: ShipperOrderListFragment(assigned)  — Đơn đang được giao
 *   1: ShipperOrderListFragment(history)   — Lịch sử giao hàng
 *   2: ShipperStatsFragment               — Thống kê doanh thu
 *   3: ShipperProfileFragment             — Thông tin cá nhân tài xế
 */
public class ShipperPagerAdapter extends FragmentStateAdapter {

    public ShipperPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ShipperOrderListFragment.newInstance(false); // active orders
            case 1:
                return ShipperOrderListFragment.newInstance(true);  // history orders
            case 2:
                return new ShipperStatsFragment();                  // statistics tab
            case 3:
            default:
                return new ShipperProfileFragment();                // profile tab
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
