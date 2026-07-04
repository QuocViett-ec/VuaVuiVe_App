package vn.vuavuive.customer.ui.checkout;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import vn.vuavuive.customer.R;

public class MapPickerActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "extra_address";

    private MapView map;
    private TextView tvAddressPreview;
    private MaterialButton btnConfirm;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private Geocoder geocoder;
    private String currentSelectedAddress = "";
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable geocodeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configuration for osmdroid
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map_picker);

        map = findViewById(R.id.map);
        tvAddressPreview = findViewById(R.id.tv_address_preview);
        btnConfirm = findViewById(R.id.btn_confirm_location);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        geocoder = new Geocoder(this, new Locale("vi", "VN"));

        setupMap();
        setupListeners();
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(16.0);
        // Default to Ho Chi Minh City
        GeoPoint startPoint = new GeoPoint(10.762622, 106.660172);
        mapController.setCenter(startPoint);
        
        // Initial geocode
        performGeocoding(startPoint);

        map.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                // When map is dragged, clear address and schedule geocoding
                tvAddressPreview.setText("Đang tìm địa chỉ...");
                btnConfirm.setEnabled(false);
                
                if (geocodeRunnable != null) {
                    handler.removeCallbacks(geocodeRunnable);
                }
                
                geocodeRunnable = () -> {
                    GeoPoint center = (GeoPoint) map.getMapCenter();
                    performGeocoding(center);
                };
                
                // Wait 1 second after drag stops before geocoding
                handler.postDelayed(geocodeRunnable, 1000);
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            if (currentSelectedAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng đợi tải địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent data = new Intent();
            data.putExtra(EXTRA_ADDRESS, currentSelectedAddress);
            setResult(Activity.RESULT_OK, data);
            finish();
        });
    }

    private void performGeocoding(GeoPoint point) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                String result = "";
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    // Get full address line
                    result = address.getAddressLine(0);
                    // Fallback to feature name if address line is empty
                    if (result == null || result.isEmpty()) {
                        result = address.getFeatureName();
                    }
                }
                
                final String finalResult = (result == null || result.isEmpty()) ? "Không thể lấy địa chỉ" : result;
                
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    currentSelectedAddress = finalResult.equals("Không thể lấy địa chỉ") ? "" : finalResult;
                    tvAddressPreview.setText(finalResult);
                    btnConfirm.setEnabled(!currentSelectedAddress.isEmpty());
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    currentSelectedAddress = "";
                    tvAddressPreview.setText("Lỗi mạng khi tải địa chỉ");
                    btnConfirm.setEnabled(false);
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}
