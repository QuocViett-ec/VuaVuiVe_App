package vn.vuavuive.customer.ui.recipe;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import vn.vuavuive.customer.R;

/** Wrapper Activity để host RecipeListFragment khi navigate từ AccountFragment */
@AndroidEntryPoint
public class RecipeListFragmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list_host);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RecipeListFragment())
                    .commit();
        }
    }
}
