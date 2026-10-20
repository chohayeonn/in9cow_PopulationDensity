package in9cow.in9cow_spring.Prediction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 장소 이름으로부터 location_id 조회
     */
    public Integer getLocationId(String locationName) throws EmptyResultDataAccessException {
        String sql = "SELECT location_id FROM Locations WHERE location_name = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{locationName}, Integer.class);
    }

    /**
     * 예측 혼잡도 조회
     */
    public Integer getPrediction(int locationId, String day, String hour) throws EmptyResultDataAccessException {
        String sql = "SELECT ai_congestion FROM prediction WHERE location_id = ? AND day = ? AND hour = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{locationId, day, hour}, Integer.class);
    }

    public Map<String, Integer> getWeeklyPrediction(int locationId, int hour) {
        String sql = "SELECT day, ai_congestion FROM prediction WHERE location_id = ? AND hour = ?";
        return jdbcTemplate.query(sql, new Object[]{locationId, hour}, rs -> {
            Map<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("day"), rs.getInt("ai_congestion"));
            }
            return result;
        });
    }


}
