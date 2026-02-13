package com.example.in9cow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;
import com.skt.tmap.overlay.TMapMarkerItem;
import com.skt.tmap.poi.TMapPOIItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private TMapView tMapView;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        FrameLayout mapContainer = view.findViewById(R.id.map_view);
        tMapView = new TMapView(requireContext());
        tMapView.setSKTMapApiKey("dpxb5t3iRs1o66ZL4jnrF53G76E2UFsL5hG9fqm3");
        mapContainer.addView(tMapView);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // 지도 준비 시 혼잡도 불러오기
        tMapView.setOnMapReadyListener(() -> {
            tMapView.setCenterPoint(36.851885, 127.151307);
            tMapView.setZoomLevel(15);
            loadCongestionData();
        });

        // 장소 검색
        EditText etSearch = view.findViewById(R.id.home_input);
        Button btnSearch = view.findViewById(R.id.home_button);

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(requireContext(), "장소를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            TMapData tMapData = new TMapData();
            tMapData.findAllPOI(keyword, poiList -> {
                if (poiList != null && !poiList.isEmpty()) {
                    String[] items = new String[poiList.size()];
                    for (int i = 0; i < poiList.size(); i++) {
                        TMapPOIItem poi = poiList.get(i);
                        items[i] = poi.getPOIName() + " - " + poi.getPOIAddress();
                    }

                    requireActivity().runOnUiThread(() -> {
                        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_places, container, false);
                        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
                        ListView listView = sheetView.findViewById(R.id.listViewPlaces);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, items);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view1, position, id) -> {
                            TMapPOIItem selectedPOI = poiList.get(position);
                            double lon = selectedPOI.getPOIPoint().getLongitude();
                            double lat = selectedPOI.getPOIPoint().getLatitude();

                            tMapView.post(() -> {
                                tMapView.setCenterPoint(lat, lon);
                                tMapView.setZoomLevel(17);
                                tMapView.removeAllTMapMarkerItem();
                                loadCongestionData();
                            });

                            dialog.dismiss();
                        });

                        dialog.setContentView(sheetView);
                        dialog.show();
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });

        return view;
    }

    // ✅ Retrofit 기반 혼잡도 로딩
    private void loadCongestionData() {
        apiService.getCongestionPoints().enqueue(new Callback<List<CongestionPoint>>() {
            @Override
            public void onResponse(Call<List<CongestionPoint>> call, Response<List<CongestionPoint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CongestionPoint> points = response.body();

                    requireActivity().runOnUiThread(() -> {
                        tMapView.removeAllTMapMarkerItem();

                        for (CongestionPoint point : points) {
                            TMapPoint tPoint = new TMapPoint(point.lat, point.lon);

                            int drawableResId;
                            switch (point.congestion) {
                                case 1:
                                    drawableResId = R.drawable.marker_green;
                                    break;
                                case 2:
                                    drawableResId = R.drawable.marker_yellow;
                                    break;
                                case 3:
                                    drawableResId = R.drawable.marker_orange;
                                    break;
                                case 4:
                                    drawableResId = R.drawable.marker_red;
                                    break;
                                default:
                                    drawableResId = R.drawable.marker_gray;
                                    break;
                            }

                            TMapMarkerItem marker = new TMapMarkerItem();
                            marker.setTMapPoint(tPoint);
                            marker.setName(point.name);
                            marker.setCalloutTitle("혼잡도: " + point.congestion);
                            marker.setCanShowCallout(true);

                            Bitmap icon = getBitmapFromVector(drawableResId);
                            if (icon != null) marker.setIcon(icon);
                            marker.setId("marker_" + point.locationId);
                            tMapView.addTMapMarkerItem(marker);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<CongestionPoint>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap getBitmapFromVector(int drawableId) {
        Drawable drawable = getResources().getDrawable(drawableId, null);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
