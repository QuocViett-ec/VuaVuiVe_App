package vn.vuavuive.customer.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.customer.ui.chat.ChatActivity;
import vn.vuavuive.customer.ui.order.OrderListFragment;
import vn.vuavuive.customer.ui.recipe.RecipeDetailActivity;
import vn.vuavuive.customer.ui.recipe.RecipeListFragment;
import vn.vuavuive.customer.ui.recipe.RecipeListFragmentActivity;
import vn.vuavuive.customer.ui.shipment.ShipmentListActivity;
import vn.vuavuive.customer.ui.review.MyReviewsActivity;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.shared.data.dto.User;

@AndroidEntryPoint
public class AccountFragment extends Fragment {

    private AuthViewModel authViewModel;
    private ImageView ivAvatar;
    private TextView tvName, tvPhone, tvEmail;
    private TextView tvLoginHint;
    private MaterialButton btnLogout;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        ivAvatar  = view.findViewById(R.id.iv_avatar);
        tvName    = view.findViewById(R.id.tv_name);
        tvPhone   = view.findViewById(R.id.tv_phone);
        tvEmail   = view.findViewById(R.id.tv_email);
        tvLoginHint = view.findViewById(R.id.tv_login_hint);
        btnLogout = view.findViewById(R.id.btn_logout);

        setupMenuItems(view);
        observeUser();
        setupLogout();

        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                if (!authViewModel.isLoggedIn()) {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                }
            });
        }
        if (tvLoginHint != null) {
            tvLoginHint.setOnClickListener(v -> {
                if (!authViewModel.isLoggedIn()) {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                }
            });
        }
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

    private void setupMenuItems(View view) {
        // Edit profile
        View rowEditProfile = view.findViewById(R.id.row_edit_profile);
        if (rowEditProfile != null) {
            rowEditProfile.setOnClickListener(v ->
                    navigateIfLoggedIn(() ->
                            startActivity(new Intent(requireContext(), EditProfileActivity.class))));
        }

        // Change password
        View rowChangePass = view.findViewById(R.id.row_change_password);
        if (rowChangePass != null) {
            rowChangePass.setOnClickListener(v ->
                    navigateIfLoggedIn(() ->
                            startActivity(new Intent(requireContext(), ChangePasswordActivity.class))));
        }

        // My orders
        View rowOrders = view.findViewById(R.id.row_my_orders);
        if (rowOrders != null) {
            rowOrders.setOnClickListener(v -> {
                navigateIfLoggedIn(() -> {
                    // Navigate to Orders tab via BottomNav (index 2)
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.nav_host_fragment, new OrderListFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                });
            });
        }

        // Recipes
        View rowRecipes = view.findViewById(R.id.row_recipes);
        if (rowRecipes != null) {
            rowRecipes.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), RecipeListFragmentActivity.class)));
        }

        // Shipments
        View rowShipments = view.findViewById(R.id.row_shipments);
        if (rowShipments != null) {
            rowShipments.setOnClickListener(v ->
            navigateIfLoggedIn(() ->
                startActivity(new Intent(requireContext(), ShipmentListActivity.class))));
        }

        // My reviews
        View rowMyReviews = view.findViewById(R.id.row_my_reviews);
        if (rowMyReviews != null) {
            rowMyReviews.setOnClickListener(v ->
            navigateIfLoggedIn(() ->
                startActivity(new Intent(requireContext(), MyReviewsActivity.class))));
        }

        // Chat support
        View rowChat = view.findViewById(R.id.row_chat);
        if (rowChat != null) {
            rowChat.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), ChatActivity.class)));
        }
    }

    private void observeUser() {
        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                bindUser(user);
            } else {
                bindGuest();
            }
        });
    }

    private void bindUser(User user) {
        tvName.setText(user.getName() != null ? user.getName() : "—");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "—");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");

        if (tvLoginHint != null) tvLoginHint.setVisibility(View.GONE);
        if (btnLogout != null) btnLogout.setVisibility(View.VISIBLE);

        // Load avatar if available
        if (user.getAvatar() != null && !user.getAvatar().isEmpty() && ivAvatar != null) {
            Glide.with(this)
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void bindGuest() {
        tvName.setText(getString(R.string.account_guest_name));
        tvPhone.setText(getString(R.string.account_guest_phone));
        tvEmail.setText(getString(R.string.account_guest_email));
        if (ivAvatar != null) {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
        if (tvLoginHint != null) tvLoginHint.setVisibility(View.VISIBLE);
        if (btnLogout != null) btnLogout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authViewModel != null && authViewModel.isLoggedIn()) {
            authViewModel.checkSession().observe(getViewLifecycleOwner(), result -> {
                if (result.status == vn.vuavuive.customer.data.repository.AuthRepository.Result.Status.SUCCESS && result.data != null) {
                    authViewModel.setCurrentUser(result.data);
                }
            });
        }
    }

    private void navigateIfLoggedIn(Runnable action) {
        if (!authViewModel.isLoggedIn()) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }
        action.run();
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            authViewModel.logout().observe(getViewLifecycleOwner(), result -> {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        });
    }
}
