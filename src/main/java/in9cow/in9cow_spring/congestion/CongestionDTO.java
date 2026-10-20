package in9cow.in9cow_spring.congestion;

import java.sql.Timestamp;

public class CongestionDTO {
    private int locationId;
    private String name;
    private double lat;
    private double lon;
    private int congestion;
    private String category;
    private String address;  // ✅ 추가

    public CongestionDTO(int locationId, String name, double lat, double lon,
                         int congestion, String category, String address) {
        this.locationId = locationId;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.congestion = congestion;
        this.category = category;
        this.address = address; // ✅ 추가
    }

    public int getLocationId() { return locationId; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public int getCongestion() { return congestion; }
    public String getCategory() { return category; }
    public String getAddress() { return address; } // ✅ 추가
}
