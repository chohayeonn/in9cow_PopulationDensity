package com.example.in9cow;

public class FavoriteRequest {
    public long userId;
    public int locationId;

    public FavoriteRequest(long userId, int locationId) {
        this.userId = userId;
        this.locationId = locationId;
    }
}
