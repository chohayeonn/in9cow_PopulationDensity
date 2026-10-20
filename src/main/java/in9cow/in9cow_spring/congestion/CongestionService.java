package in9cow.in9cow_spring.congestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CongestionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. 공통 SQL 생성 메서드
    private String buildBaseQuery(String category) {
        StringBuilder sql = new StringBuilder("""
    SELECT l.location_id, l.location_name, l.lat, l.lon, l.category, l.address, p.congestion
    FROM Locations l
    JOIN PopulationFlow p ON l.location_id = p.location_id
    WHERE p.timestamp BETWEEN
        DATE_FORMAT((SELECT MAX(timestamp) FROM PopulationFlow), '%Y-%m-%d %H:00:00')
        AND
        DATE_FORMAT((SELECT MAX(timestamp) FROM PopulationFlow), '%Y-%m-%d %H:59:59')
""");


        if (category != null && !category.isEmpty()) {
            sql.append(" AND l.category = ? ");
        }

        return sql.toString();
    }

    // 2. 혼잡도 정렬 없이 기본 조회 (Controller용)
    public List<CongestionDTO> getCurrentHourCongestion() {
        String sql = buildBaseQuery(null) + " ORDER BY l.location_name ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CongestionDTO(
                rs.getInt("location_id"),
                rs.getString("location_name"),
                rs.getDouble("lat"),
                rs.getDouble("lon"),
                rs.getInt("congestion"),
                rs.getString("category"),
                rs.getString("address") //
        ));
    }

    // 3. 추천 필터 + 정렬 포함
    public List<CongestionDTO> getRecommendations(String category, String sort) {
        String sql = buildBaseQuery(category);

        sql += " ORDER BY p.congestion ";
        sql += ("한적한 순".equals(sort)) ? "ASC, l.location_name AS" +
                "C" : "DESC, l.location_name ASC";

        if (category != null && !category.isEmpty()) {
            return jdbcTemplate.query(sql, new Object[]{category}, (rs, rowNum) -> new CongestionDTO(
                    rs.getInt("location_id"),
                    rs.getString("location_name"),
                    rs.getDouble("lat"),
                    rs.getDouble("lon"),
                    rs.getInt("congestion"),
                    rs.getString("category"),
                    rs.getString("address") // ✅ 여기 추가
            ));
        } else {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new CongestionDTO(
                    rs.getInt("location_id"),
                    rs.getString("location_name"),
                    rs.getDouble("lat"),
                    rs.getDouble("lon"),
                    rs.getInt("congestion"),
                    rs.getString("category"),
                    rs.getString("address") // ✅ 여기 추가
            ));
        }
    }
}

