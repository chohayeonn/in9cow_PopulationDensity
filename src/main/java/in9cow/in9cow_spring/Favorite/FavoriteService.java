package in9cow.in9cow_spring.Favorite;

import in9cow.in9cow_spring.congestion.CongestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addFavorite(long userId, int locationId) {
        String sql = "INSERT INTO Favorite (user_id, location_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE user_id = user_id";
        jdbcTemplate.update(sql, userId, locationId);
    }

    public void removeFavorite(long userId, int locationId) {
        String sql = "DELETE FROM Favorite WHERE user_id = ? AND location_id = ?";
        jdbcTemplate.update(sql, userId, locationId);
    }

    public List<CongestionDTO> getFavoritesByUserId(long userId) {
        String sql = """
        SELECT l.location_id, l.location_name, l.lat, l.lon, l.category, l.address,
               COALESCE(p.congestion, -1) AS congestion
        FROM Favorite f
        JOIN Locations l ON f.location_id = l.location_id
        LEFT JOIN PopulationFlow p ON l.location_id = p.location_id
        AND p.timestamp = (SELECT MAX(timestamp) FROM PopulationFlow)
        WHERE f.user_id = ?
    """;

        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) ->
                new CongestionDTO(
                        rs.getInt("location_id"),
                        rs.getString("location_name"),
                        rs.getDouble("lat"),
                        rs.getDouble("lon"),
                        rs.getInt("congestion"),
                        rs.getString("category"),
                        rs.getString("address") // ✅ 여기 추가
                )
        );
    }
}

