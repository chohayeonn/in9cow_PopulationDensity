package in9cow.in9cow_spring.Favorite;

public class FavoriteDTO {
    private long userId;
    private int locationId;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

}
