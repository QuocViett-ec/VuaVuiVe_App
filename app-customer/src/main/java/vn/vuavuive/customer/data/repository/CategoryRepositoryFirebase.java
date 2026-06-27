package vn.vuavuive.customer.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CategoryRepositoryFirebase {

    private final DatabaseReference dbRef;
    private List<CategoryResponse> cachedCategories = null;

    @Inject
    public CategoryRepositoryFirebase() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public LiveData<AuthRepository.Result<List<CategoryResponse>>> getCategories() {
        android.util.Log.d("CategoryRepositoryFirebase", "getCategories: Called. Cache is " + (cachedCategories != null ? "HIT" : "MISS"));
        MutableLiveData<AuthRepository.Result<List<CategoryResponse>>> result = new MutableLiveData<>();

        if (cachedCategories != null) {
            result.postValue(AuthRepository.Result.success(cachedCategories));
            return result;
        }

        result.postValue(AuthRepository.Result.loading());
        dbRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("CategoryRepositoryFirebase", "onDataChange: Fetched " + snapshot.getChildrenCount() + " categories from Firebase.");
                List<CategoryResponse> categories = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    CategoryResponse cat = new CategoryResponse();
                    cat.setId(s.child("id").getValue(String.class));
                    cat.setName(s.child("name").getValue(String.class));
                    cat.setSlug(s.child("slug").getValue(String.class));
                    cat.setImageUrl(s.child("image_url").getValue(String.class));
                    cat.setParentId(s.child("parent_id").getValue(String.class));
                    cat.setParentName(s.child("parent_name").getValue(String.class));
                    
                    Boolean active = s.child("is_active").getValue(Boolean.class);
                    cat.setActive(active != null ? active : true);

                    if (cat.isActive()) {
                        categories.add(cat);
                    }
                }
                android.util.Log.d("CategoryRepositoryFirebase", "onDataChange: Mapped " + categories.size() + " active categories.");
                cachedCategories = categories;
                result.postValue(AuthRepository.Result.success(categories));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("CategoryRepositoryFirebase", "onCancelled: Failed to fetch categories: " + error.getMessage());
                result.postValue(AuthRepository.Result.error("Lỗi tải danh mục: " + error.getMessage()));
            }
        });

        return result;
    }
}
