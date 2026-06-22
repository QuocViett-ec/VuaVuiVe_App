package vn.vuavuive.customer.ui.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.util.CurrencyFormatter;
import vn.vuavuive.shared.util.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String ARG_PRODUCT_ID = "product_id";
    private static final String ARG_PRODUCT_NAME = "product_name";
    private static final String ARG_PRODUCT_IMAGE = "product_image";
    private static final String ARG_PRODUCT_PRICE = "product_price";
    private static final String ARG_PRODUCT_UNIT = "product_unit";

    public static ReviewBottomSheetDialogFragment newInstance(
            String orderId, String productId, String name, String image, double price, String unit) {
        ReviewBottomSheetDialogFragment fragment = new ReviewBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putString(ARG_PRODUCT_ID, productId);
        args.putString(ARG_PRODUCT_NAME, name);
        args.putString(ARG_PRODUCT_IMAGE, image);
        args.putDouble(ARG_PRODUCT_PRICE, price);
        args.putString(ARG_PRODUCT_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    private OrderViewModel orderViewModel;
    private ProductViewModel productViewModel;

    private ImageView ivProduct;
    private TextView tvName, tvPrice;
    private RatingBar ratingBar;
    private TextInputEditText etComment;
    private MaterialButton btnCancel, btnSubmit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        ivProduct = view.findViewById(R.id.iv_product);
        tvName = view.findViewById(R.id.tv_product_name);
        tvPrice = view.findViewById(R.id.tv_price);
        ratingBar = view.findViewById(R.id.rating_bar);
        etComment = view.findViewById(R.id.et_comment);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSubmit = view.findViewById(R.id.btn_submit);

        bindArgs();
        loadExistingReview();

        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void bindArgs() {
        Bundle args = getArguments();
        if (args == null) return;
        String name = args.getString(ARG_PRODUCT_NAME, "");
        String image = args.getString(ARG_PRODUCT_IMAGE, "");
        double price = args.getDouble(ARG_PRODUCT_PRICE, 0);
        String unit = args.getString(ARG_PRODUCT_UNIT, null);

        tvName.setText(name);
        String priceText = CurrencyFormatter.format(price) + (unit != null && !unit.isEmpty() ? "/" + unit : "");
        tvPrice.setText(priceText);

        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.ic_image)
                .centerCrop()
                .into(ivProduct);
    }

    private void loadExistingReview() {
        Bundle args = getArguments();
        if (args == null) return;
        String orderId = args.getString(ARG_ORDER_ID, "");
        String productId = args.getString(ARG_PRODUCT_ID, "");
        if (orderId == null || orderId.isEmpty()) {
            SessionManager sessionManager = new SessionManager(requireContext());
            String currentUserId = sessionManager.getUserId();
            if (currentUserId != null && !currentUserId.isEmpty()) {
                productViewModel.getProductReviews(productId).observe(getViewLifecycleOwner(), result -> {
                    if (result != null && result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                        for (vn.vuavuive.shared.data.dto.Review r : result.data) {
                            if (currentUserId.equals(r.getUserId())) {
                                ratingBar.setRating(r.getRating());
                                etComment.setText(r.getComment() != null ? r.getComment() : "");
                                break;
                            }
                        }
                    }
                });
            }
            return;
        }

        orderViewModel.getMyReview(orderId).observe(getViewLifecycleOwner(), result -> {
            if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                if (productId.equals(result.data.getProductId())) {
                    ratingBar.setRating(result.data.getRating());
                    etComment.setText(result.data.getComment() != null ? result.data.getComment() : "");
                }
            }
        });
    }

    private void submitReview() {
        Bundle args = getArguments();
        if (args == null) return;
        String orderId = args.getString(ARG_ORDER_ID, "");
        String productId = args.getString(ARG_PRODUCT_ID, "");
        int rating = Math.round(ratingBar.getRating());
        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

        if (rating <= 0) {
            Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orderId == null || orderId.isEmpty()) {
            btnSubmit.setEnabled(false);
            productViewModel.submitProductReview(productId, rating, comment).observe(getViewLifecycleOwner(), result -> {
                btnSubmit.setEnabled(true);
                if (result.status == AuthRepository.Result.Status.SUCCESS) {
                    Toast.makeText(getContext(), "Gửi đánh giá thành công", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof ProductDetailActivity) {
                        ((ProductDetailActivity) getActivity()).refreshReviews();
                    }
                    dismiss();
                } else if (result.status == AuthRepository.Result.Status.ERROR) {
                    Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        List<Map<String, Object>> reviews = new ArrayList<>();
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", productId);
        payload.put("rating", rating);
        payload.put("comment", comment);
        reviews.add(payload);

        btnSubmit.setEnabled(false);
        orderViewModel.submitReview(orderId, reviews).observe(getViewLifecycleOwner(), result -> {
            btnSubmit.setEnabled(true);
            if (result.status == AuthRepository.Result.Status.SUCCESS) {
                Toast.makeText(getContext(), "Gửi đánh giá thành công", Toast.LENGTH_SHORT).show();
                dismiss();
            } else if (result.status == AuthRepository.Result.Status.ERROR) {
                Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
