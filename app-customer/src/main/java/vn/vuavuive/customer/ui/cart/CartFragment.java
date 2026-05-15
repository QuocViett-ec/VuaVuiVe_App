package vn.vuavuive.customer.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.checkout.CheckoutActivity;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.List;

@AndroidEntryPoint
public class CartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;
    private TextView tvTotal, tvEmptyCart;
    private LinearLayout layoutCartContent;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        tvTotal        = view.findViewById(R.id.tv_total);
        tvEmptyCart    = view.findViewById(R.id.tv_empty_cart);
        layoutCartContent = view.findViewById(R.id.layout_cart_content);
        btnCheckout    = view.findViewById(R.id.btn_checkout);

        setupCartRecyclerView(view);
        observeCart();

        btnCheckout.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CheckoutActivity.class));
        });
    }

    private void setupCartRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_cart_items);
        cartAdapter = new CartAdapter(getContext(), cartViewModel);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(cartAdapter);

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                CartItemEntity item = cartAdapter.getItemAt(position);
                if (item != null) {
                    cartViewModel.removeItem(item.getProductId());
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rv);
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                showEmptyState(true);
            } else {
                showEmptyState(false);
                cartAdapter.setItems(items);
                updateTotal(items);
            }
        });
    }

    private void updateTotal(List<CartItemEntity> items) {
        double total = 0;
        for (CartItemEntity item : items) total += item.getLineTotal();
        tvTotal.setText(CurrencyFormatter.format(total));
    }

    private void showEmptyState(boolean empty) {
        tvEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
        layoutCartContent.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
