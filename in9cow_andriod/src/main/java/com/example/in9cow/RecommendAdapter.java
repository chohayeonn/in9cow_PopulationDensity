package com.example.in9cow;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {

    private final Context context;
    private final ApiService apiService;
    private List<CongestionPoint> items = new ArrayList<>();
    private Set<String> favoriteSet = new HashSet<>();
    private boolean isFavoriteMode;

    public void setItems(List<CongestionPoint> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CongestionPoint item = items.get(position);
        holder.nameText.setText(item.name);
        if (isFavoriteMode) {
            // 즐겨찾기 탭: 혼잡도 숨기고, 카테고리 표시
            holder.descriptionText.setVisibility(View.GONE);
            holder.categoryText.setVisibility(View.VISIBLE);
            holder.categoryText.setText(item.category);  // 카테고리 표시
        } else {
            // 추천 탭: 혼잡도 표시, 카테고리 숨기기
            holder.descriptionText.setVisibility(View.VISIBLE);
            holder.descriptionText.setText("현재 이 장소의 혼잡도는 \"" + getCongestionText(item.congestion) + "\"입니다.");
            holder.categoryText.setVisibility(View.GONE);
        }

        boolean isFavorite = favoriteSet.contains(item.name);
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        holder.btnFavorite.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            CongestionPoint currentItem = items.get(currentPosition);
            long userId = getLoggedInUserId();
            int locationId = currentItem.locationId;

            if (userId == -1) {
                Toast.makeText(context, "로그인 후 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (favoriteSet.contains(currentItem.name)) {
                favoriteSet.remove(currentItem.name);
                if (isFavoriteMode) {
                    items.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                } else {
                    notifyItemChanged(currentPosition);
                }

                // 2. 서버에서 즐겨찾기 제거 요청
                apiService.removeFavorite(new FavoriteRequest(userId, locationId)).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("Favorite", "즐겨찾기 제거 성공");
                        } else {
                            Log.e("Favorite", "즐겨찾기 제거 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Favorite", "즐겨찾기 제거 오류", t);
                    }
                });

            } else {
                favoriteSet.add(currentItem.name);
                notifyItemChanged(currentPosition);

                // 서버에 즐겨찾기 추가 요청
                apiService.addFavorite(new FavoriteRequest(userId, locationId)).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("Favorite", "즐겨찾기 추가 성공");
                        } else {
                            Log.e("Favorite", "즐겨찾기 추가 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("Favorite", "즐겨찾기 추가 오류", t);
                    }
                });
            }
        });
    }

    public RecommendAdapter(Context context, ApiService apiService, boolean isFavoriteMode) {
        this.context = context;
        this.apiService = apiService;
        this.isFavoriteMode = isFavoriteMode;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setFavoriteSet(Set<String> favoriteSet) {
        this.favoriteSet = favoriteSet;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView descriptionText;
        ImageButton btnFavorite;
        TextView categoryText;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textTitle);
            descriptionText = itemView.findViewById(R.id.textDescription);
            categoryText = itemView.findViewById(R.id.textCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
    private long getLoggedInUserId() {
        if (context == null) {
            Log.e("Favorite", "context가 null입니다");
            return -1;
        }
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", -1);
    }
    private int getBindingAdapterPositionSafe(ViewHolder holder) {
        int position = holder.getAdapterPosition();
        return position == RecyclerView.NO_POSITION ? -1 : position;
    }
    private String getCongestionText(int level) {
        switch (level) {
            case 1:
                return "여유 🔵";
            case 2:
                return "보통 🟡";
            case 3:
                return "붐빔 🟠";
            case 4:
                return "혼잡 🔴";
            default:
                return "정보 없음";
        }
    }
}
