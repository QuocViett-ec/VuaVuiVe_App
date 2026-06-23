package vn.vuavuive.customer.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.customer.ui.checkout.CheckoutActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.shared.data.local.CartItemEntity;
import vn.vuavuive.shared.util.CurrencyFormatter;
import java.util.List;

@AndroidEntryPoint
public class CartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private AuthViewModel authViewModel;
    private CartAdapter cartAdapter;
    private CartAdapter savedAdapter;
    private TextView tvTotal, tvEmptyCart, tvEmptyCartInline, tvItemCount, tvSubtotalAmount;
    private TextView tvSavedCount, tvSavedToggle, tvSavedEmpty;
    private View layoutCartContent;
    private View layoutEmptyCart;
    private View layoutSavedHeader;
    private Button btnCheckout;
    private RecyclerView rvSavedItems;
    private boolean savedExpanded = false;
    private List<CartItemEntity> cartItems = new java.util.ArrayList<>();
    private List<CartItemEntity> savedItems = new java.util.ArrayList<>();

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
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        tvTotal        = view.findViewById(R.id.tv_total);
        tvEmptyCart    = view.findViewById(R.id.tv_empty_cart);
        tvEmptyCartInline = view.findViewById(R.id.tv_empty_cart_inline);
        tvItemCount    = view.findViewById(R.id.tv_item_count);
        tvSubtotalAmount = view.findViewById(R.id.tv_subtotal_amount);
        layoutCartContent = view.findViewById(R.id.layout_cart_content);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        btnCheckout    = view.findViewById(R.id.btn_checkout);
        layoutSavedHeader = view.findViewById(R.id.layout_saved_header);
        tvSavedCount   = view.findViewById(R.id.tv_saved_count);
        tvSavedToggle  = view.findViewById(R.id.tv_saved_toggle);
        tvSavedEmpty   = view.findViewById(R.id.tv_saved_empty);
        rvSavedItems   = view.findViewById(R.id.rv_saved_items);

        setupCartRecyclerView(view);
        setupSavedRecyclerView();
        setupSavedHeader();
        observeCart();

        // Shop now button in empty state
        View btnShopNow = view.findViewById(R.id.btn_shop_now);
        if (btnShopNow != null) {
            btnShopNow.setOnClickListener(v -> {
                try {
                    if (getActivity() instanceof vn.vuavuive.customer.ui.MainActivity) {
                        ((vn.vuavuive.customer.ui.MainActivity) getActivity()).navigateToProducts();
                    }
                } catch (Exception ignored) {}
            });
        }

        btnCheckout.setOnClickListener(v -> {
            if (!authViewModel.isLoggedIn()) {
                Toast.makeText(getContext(), R.string.login_required_checkout, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), LoginActivity.class));
                return;
            }
            startActivity(new Intent(getContext(), CheckoutActivity.class));
        });

        setupHeaderSearch(view);
    }

    private void setupHeaderSearch(View view) {
        View etSearch = view.findViewById(R.id.header_et_search);
        if (etSearch != null) {
            etSearch.setFocusable(false);
            etSearch.setClickable(true);
            etSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), vn.vuavuive.customer.ui.search.SearchActivity.class);
                startActivity(intent);
            });
        }
        View btnMenu = view.findViewById(R.id.header_btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                try {
                    if (getActivity() instanceof vn.vuavuive.customer.ui.MainActivity) {
                        ((vn.vuavuive.customer.ui.MainActivity) getActivity()).navigateToProducts();
                    }
                } catch (Exception ignored) {}
            });
        }
    }

    private void setupCartRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_cart_items);
        cartAdapter = new CartAdapter(getContext(), cartViewModel, false);
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

    private void setupSavedRecyclerView() {
        savedAdapter = new CartAdapter(getContext(), cartViewModel, true);
        rvSavedItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSavedItems.setAdapter(savedAdapter);
    }

    private void setupSavedHeader() {
        if (layoutSavedHeader == null) return;
        layoutSavedHeader.setOnClickListener(v -> {
            savedExpanded = !savedExpanded;
            updateSavedSection();
        });
    }

    private void observeCart() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartItems = items != null ? items : new java.util.ArrayList<>();
            cartAdapter.setItems(cartItems);
            updateTotal(cartItems);
            updateEmptyState();
            updateSavedSection();
        });

        cartViewModel.getSavedItems().observe(getViewLifecycleOwner(), items -> {
            savedItems = items != null ? items : new java.util.ArrayList<>();
            savedAdapter.setItems(savedItems);
            updateEmptyState();
            updateSavedSection();
        });
    }

    private void updateTotal(List<CartItemEntity> items) {
        double total = 0;
        for (CartItemEntity item : items) total += item.getLineTotal();
        if (tvTotal != null) tvTotal.setText(CurrencyFormatter.format(total));
        if (tvSubtotalAmount != null) tvSubtotalAmount.setText(CurrencyFormatter.format(total));
        if (tvItemCount != null) {
            int count = items != null ? items.size() : 0;
            tvItemCount.setText(count + " sản phẩm");
        }
    }

    private void updateEmptyState() {
        boolean cartEmpty = cartItems == null || cartItems.isEmpty();
        boolean savedEmpty = savedItems == null || savedItems.isEmpty();
        boolean allEmpty = cartEmpty && savedEmpty;

        if (layoutEmptyCart != null) {
            layoutEmptyCart.setVisibility(allEmpty ? View.VISIBLE : View.GONE);
        }
        layoutCartContent.setVisibility(allEmpty ? View.GONE : View.VISIBLE);

        if (tvEmptyCartInline != null) {
            tvEmptyCartInline.setVisibility(!allEmpty && cartEmpty ? View.VISIBLE : View.GONE);
        }
        if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(allEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void updateSavedSection() {
        boolean hasSaved = savedItems != null && !savedItems.isEmpty();
        boolean showHeader = hasSaved || (cartItems != null && !cartItems.isEmpty());
        if (layoutSavedHeader != null) {
            layoutSavedHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }
        if (tvSavedCount != null) {
            tvSavedCount.setText("(" + (savedItems != null ? savedItems.size() : 0) + ")");
        }
        if (tvSavedToggle != null) {
            tvSavedToggle.setText(savedExpanded ? "^" : "v");
            tvSavedToggle.setVisibility(hasSaved ? View.VISIBLE : View.GONE);
        }

        if (rvSavedItems != null) {
            rvSavedItems.setVisibility(hasSaved && savedExpanded ? View.VISIBLE : View.GONE);
        }
        if (tvSavedEmpty != null) {
            tvSavedEmpty.setVisibility(!hasSaved && showHeader ? View.VISIBLE : View.GONE);
        }
    }
}
