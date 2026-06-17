package com.example.in9cow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendFragment extends Fragment {

    private ChipGroup chipGroup;
    private Spinner sortSpinner;
    private RecyclerView recyclerView;
    private RecommendAdapter adapter;
    private ApiService apiService;

    private String selectedCategory = null;
    private String selectedSort = "붐빔";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        chipGroup = view.findViewById(R.id.chipGroupThemes);
        sortSpinner = view.findViewById(R.id.sortSpinner);
        recyclerView = view.findViewById(R.id.recommendRecyclerView);

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        adapter = new RecommendAdapter(requireContext(), apiService, false);
        recyclerView.setAdapter(adapter);

        // Spinner 설정
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"붐비는 순", "한적한 순"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = (String) parent.getItemAtPosition(position);
                fetchRecommendations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = null;
            } else {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    String chipText = chip.getText().toString();
                    selectedCategory = "전체".equals(chipText) ? null : chipText;
                } else {
                    selectedCategory = null;
                }
            }
            fetchRecommendations();
        });

        fetchRecommendations();
        loadFavoriteSet(); // 하트 상태 초기화

        return view;
    }

    private void fetchRecommendations() {
        Call<List<CongestionPoint>> call = apiService.getRecommendations(selectedCategory, selectedSort);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<CongestionPoint>> call, Response<List<CongestionPoint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<CongestionPoint>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadFavoriteSet() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        if (userId != -1) {
            apiService.getFavoriteList(userId).enqueue(new Callback<List<CongestionPoint>>() {
                @Override
                public void onResponse(Call<List<CongestionPoint>> call, Response<List<CongestionPoint>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Set<String> favoriteNames = new HashSet<>();
                        for (CongestionPoint p : response.body()) {
                            favoriteNames.add(p.name);
                        }
                        adapter.setFavoriteSet(favoriteNames); // 하트 상태 반영
                    }
                }

                @Override
                public void onFailure(Call<List<CongestionPoint>> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }
}
