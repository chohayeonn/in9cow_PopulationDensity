package com.example.in9cow;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class SearchFragment extends Fragment {

    private AutoCompleteTextView searchInput;
    private Spinner spinnerTime;
    private Button searchButton;
    private Button datePickerButton;
    private TextView resultText;
    private LinearLayout optionLayout;

    private String selectedDate; // yyyy-MM-dd
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // UI 연결
        searchInput = view.findViewById(R.id.search_input);
        spinnerTime = view.findViewById(R.id.spinner_time);
        searchButton = view.findViewById(R.id.search_button);
        datePickerButton = view.findViewById(R.id.date_picker_button);
        resultText = view.findViewById(R.id.search_result);
        optionLayout = view.findViewById(R.id.option_layout);

        // 장소 목록 로드 후 어댑터 연결
        List<String> locationList = loadLocationNamesFromJson(requireContext());
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                locationList
        );
        searchInput.setAdapter(locationAdapter);

        // AutoCompleteTextView 클릭 시 전체 드롭다운 표시 + 옵션 보이기
        searchInput.setOnClickListener(v -> {
            optionLayout.setVisibility(View.VISIBLE);
            searchInput.showDropDown();
        });
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                optionLayout.setVisibility(View.VISIBLE);
                searchInput.showDropDown();
            }
        });

        // 날짜 선택 기본값: 오늘
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        selectedDate = String.format("%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerButton.setText(selectedDate);

        datePickerButton.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                datePickerButton.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 시간 스피너: 09시 ~ 24시
        List<String> timeList = new ArrayList<>();
        for (int hour = 9; hour <= 24; hour++) {
            timeList.add(String.format("%02d:00", hour));
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        // 현재 시간 기반 기본 선택
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int defaultHour = Math.max(9, Math.min(hour, 24));
        spinnerTime.setSelection(defaultHour - 9);

        // 버튼 클릭: 예측 요청
        searchButton.setOnClickListener(v -> {
            String location = searchInput.getText().toString().trim();
            String time = spinnerTime.getSelectedItem().toString();
            String hourOnly = time.split(":")[0];
            String day = getDayOfWeek(selectedDate);

            if (location.isEmpty()) {
                resultText.setText("장소를 입력해주세요.");
                return;
            }

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("location", location);
                jsonBody.put("day", day);
                jsonBody.put("hour", hourOnly);

                showLoadingDialog();
                sendPredictionRequest(jsonBody, location, day, hourOnly);
            } catch (Exception e) {
                hideLoadingDialog();
                resultText.setText("요청 구성 오류: " + e.getMessage());
            }
        });

        return view;
    }

    private List<String> loadLocationNamesFromJson(android.content.Context context) {
        List<String> locationNames = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("place_coordinates.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonStr);

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject location = jsonObject.getJSONObject(key);
                String name = location.optString("name", key);
                locationNames.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locationNames;
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        ProgressBar progressBar = new ProgressBar(requireContext());
        builder.setView(progressBar);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showResultDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("확인", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendPredictionRequest(JSONObject requestJson, String location, String day, String hour) {
        String url = "http://3.24.137.66:8080/api/predict";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                response -> {
                    hideLoadingDialog();

                    try {
                        String congestion = response.getString("ai_congestion");
                        String congestionText;
                        switch (congestion) {
                            case "1": congestionText = "🔵 매우 여유"; break;
                            case "2": congestionText = "🟢 보통"; break;
                            case "3": congestionText = "🟡 약간 혼잡"; break;
                            case "4": congestionText = "🔴 매우 혼잡"; break;
                            default: congestionText = "알 수 없음"; break;
                        }

                        String message = String.format("%s\n%s %s시 예측 혼잡도:\n%s",
                                location, day, hour, congestionText);

                        resultText.setText(message);
                        showResultDialog("📍 예측 결과", message);

                    } catch (Exception e) {
                        showResultDialog("결과 오류", "결과 처리 중 오류: " + e.getMessage());
                    }
                },
                error -> {
                    hideLoadingDialog();

                    String errorMessage = "서버 통신 실패";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errorObj = new JSONObject(body);
                            errorMessage = errorObj.optString("error", errorMessage);
                        } catch (Exception e) {
                            errorMessage += ": 응답 파싱 실패";
                        }
                    }
                    resultText.setText(errorMessage);
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestJson.toString().getBytes(StandardCharsets.UTF_8);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private String getDayOfWeek(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            Date date = sdf.parse(dateString);
            SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.KOREA);
            return dayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
