package vn.vuavuive.customer.ui.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.ui.product.ProductAdapter;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;

@AndroidEntryPoint
public class SearchActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "vvv_search_history";
    private static final String KEY_HISTORY = "history";
    private static final int HISTORY_LIMIT = 10;

    private ProductViewModel productViewModel;
    private ProductAdapter adapter;

    private TextInputEditText etSearch;
    private LinearLayout llHistory;
    private TextView tvHistoryEmpty;
    private TextView tvClearHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        initViews();
        setupRecycler();
        setupSearch();
        renderHistory(loadHistory());

        String prefill = getIntent().getStringExtra("prefill_query");
        if (prefill != null && !prefill.isEmpty()) {
            etSearch.setText(prefill);
            etSearch.setSelection(prefill.length());
            performSearch(prefill, true);
        }
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.et_search);
        llHistory = findViewById(R.id.ll_history);
        tvHistoryEmpty = findViewById(R.id.tv_history_empty);
        tvClearHistory = findViewById(R.id.tv_clear_history);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        tvClearHistory.setOnClickListener(v -> {
            saveHistory(new ArrayList<>());
            renderHistory(new ArrayList<>());
        });
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rv_results);
        adapter = new ProductAdapter(this, product -> {
            android.content.Intent intent = new android.content.Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(adapter);
    }

    private void setupSearch() {
        TextInputLayout tilSearch = findViewById(R.id.til_search);
        if (tilSearch != null) {
            tilSearch.setEndIconOnClickListener(v -> performSearch(getQuery(), true));
        }
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String q = getQuery();
                performSearch(q, true);
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> performSearch(getQuery(), false);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void performSearch(String query, boolean fromAction) {
        if (query.isEmpty()) {
            adapter.setProducts(new ArrayList<>());
            tvEmpty.setVisibility(View.GONE);
            renderHistory(loadHistory());
            return;
        }

        showLoading(true);
        productViewModel.getProducts(null, query, 1, 50, null)
                .observe(this, result -> {
                    showLoading(false);
                    if (result.status == AuthRepository.Result.Status.SUCCESS && result.data != null) {
                        adapter.setProducts(result.data);
                        tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
                        if (fromAction || !result.data.isEmpty()) {
                            addToHistory(query);
                        }
                    } else if (result.status == AuthRepository.Result.Status.ERROR) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void renderHistory(List<String> items) {
        llHistory.removeAllViews();
        boolean empty = items == null || items.isEmpty();
        tvHistoryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);

        if (empty) return;

        for (String item : items) {
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 0);
            tv.setLayoutParams(lp);
            tv.setPadding(16, 10, 16, 10);
            tv.setBackgroundResource(R.drawable.bg_history_chip);
            tv.setTextColor(getResources().getColor(R.color.text_primary, null));
            tv.setText(item);
            tv.setOnClickListener(v -> {
                etSearch.setText(item);
                etSearch.setSelection(item.length());
                performSearch(item, true);
            });
            llHistory.addView(tv);
        }
    }

    private void addToHistory(String query) {
        List<String> current = loadHistory();
        current.remove(query);
        current.add(0, query);
        if (current.size() > HISTORY_LIMIT) {
            current = current.subList(0, HISTORY_LIMIT);
        }
        saveHistory(current);
        renderHistory(current);
    }

    private List<String> loadHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String raw = prefs.getString(KEY_HISTORY, "[]");
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                String item = arr.optString(i, "");
                if (!item.isEmpty()) list.add(item);
            }
        } catch (Exception e) {
            list.clear();
        }
        return list;
    }

    private void saveHistory(List<String> list) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        List<String> safe = list != null ? list : Collections.emptyList();
        for (String item : safe) arr.put(item);
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
    }

    private String getQuery() {
        return etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
