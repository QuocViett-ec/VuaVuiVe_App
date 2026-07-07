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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.vuavuive.customer.R;

public class MapPickerActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS = "extra_address";

    private MapView map;
    private TextView tvAddressPreview;
    private MaterialButton btnConfirm;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private EditText etSearchAddress;
    private ImageView btnSearchAddress;

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
        etSearchAddress = findViewById(R.id.et_search_address);
        btnSearchAddress = findViewById(R.id.btn_search_address);

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

        if (btnSearchAddress != null) {
            btnSearchAddress.setOnClickListener(v -> {
                if (etSearchAddress != null) {
                    performSearch(etSearchAddress.getText().toString());
                }
            });
        }

        if (etSearchAddress != null) {
            etSearchAddress.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(etSearchAddress.getText().toString());
                    return true;
                }
                return false;
            });
        }
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
                    if (isFinishing() || isDestroyed()) return;
                    progressBar.setVisibility(View.GONE);
                    currentSelectedAddress = finalResult.equals("Không thể lấy địa chỉ") ? "" : finalResult;
                    tvAddressPreview.setText(finalResult);
                    btnConfirm.setEnabled(!currentSelectedAddress.isEmpty());
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressBar.setVisibility(View.GONE);
                    currentSelectedAddress = "";
                    tvAddressPreview.setText("Lỗi mạng khi tải địa chỉ");
                    btnConfirm.setEnabled(false);
                });
            }
        }).start();
    }

    private void performSearch(final String query) {
        if (query == null || query.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ cần tìm", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        // Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        new Thread(() -> {
            GeoPoint foundPoint = null;
            // 1. Try standard Geocoder first
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    foundPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. Try Nominatim fallback if Geocoder fails
            if (foundPoint == null) {
                foundPoint = searchNominatim(query);
            }

            final GeoPoint finalPoint = foundPoint;
            handler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setVisibility(View.GONE);
                if (finalPoint != null) {
                    IMapController mapController = map.getController();
                    mapController.setZoom(17.0);
                    mapController.animateTo(finalPoint);
                    performGeocoding(finalPoint);
                } else {
                    Toast.makeText(MapPickerActivity.this, "Không tìm thấy địa chỉ này", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private GeoPoint searchNominatim(String query) {
        HttpURLConnection conn = null;
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json&limit=1&accept-language=vi");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", getPackageName());

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String json = response.toString();
                Pattern latPattern = Pattern.compile("\"lat\":\"([^\"]+)\"");
                Pattern lonPattern = Pattern.compile("\"lon\":\"([^\"]+)\"");
                Matcher latMatcher = latPattern.matcher(json);
                Matcher lonMatcher = lonPattern.matcher(json);

                if (latMatcher.find() && lonMatcher.find()) {
                    double lat = Double.parseDouble(latMatcher.group(1));
                    double lon = Double.parseDouble(lonMatcher.group(1));
                    return new GeoPoint(lat, lon);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (geocodeRunnable != null) handler.removeCallbacks(geocodeRunnable);
        if (map != null) map.onPause();
    }
}
