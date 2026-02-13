package com.example.in9cow;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteFragment extends Fragment {

    private RecyclerView favoriteRecyclerView;
    private RecommendAdapter adapter; // 재활용
    private ApiService apiService = RetrofitClient.getApiService();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        long userId = prefs.getLong("user_id", -1);

        LinearLayout loginLayout = view.findViewById(R.id.layout_login_required);
        favoriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecommendAdapter(requireContext(), apiService, true);
        favoriteRecyclerView.setAdapter(adapter);

        if (!isLoggedIn) {
            loginLayout.setVisibility(View.VISIBLE);
            favoriteRecyclerView.setVisibility(View.GONE);
            Button btnGoLogin = view.findViewById(R.id.btnGoLogin);
            btnGoLogin.setOnClickListener(v -> {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_fragment, new MypageFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            });
        } else {
            loginLayout.setVisibility(View.GONE);
            favoriteRecyclerView.setVisibility(View.VISIBLE);
            fetchFavorites(userId);
        }

        return view;
    }

    private void fetchFavorites(long userId) {
        apiService.getFavoriteList(userId).enqueue(new Callback<List<CongestionPoint>>() {
            @Override
            public void onResponse(Call<List<CongestionPoint>> call, Response<List<CongestionPoint>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CongestionPoint> favorites = response.body();
                    adapter.setItems(favorites); // 카드 리스트 표시

                    // 👉 하트 상태 유지용 Set 도 같이 설정
                    Set<String> favoriteNames = new HashSet<>();
                    for (CongestionPoint p : favorites) {
                        favoriteNames.add(p.name);
                    }
                    adapter.setFavoriteSet(favoriteNames); // 하트 표시 반영
                }
            }

            @Override
            public void onFailure(Call<List<CongestionPoint>> call, Throwable t) {
                Log.e("Favorite", "즐겨찾기 로딩 실패", t);
            }
        });
    }

}