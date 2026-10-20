package in9cow.in9cow_spring.Prediction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<?> getPrediction(@RequestBody PredictionDTO request) {
        try {
            // 1. 장소명으로 location_id 조회
            Integer locationId = predictionService.getLocationId(request.getLocation());

            if (locationId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "장소를 찾을 수 없습니다."));
            }

            // 2. 예측 혼잡도 조회
            Integer congestion = predictionService.getPrediction(
                    locationId, request.getDay(), request.getHour()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("location", request.getLocation());
            result.put("location_id", locationId);
            result.put("day", request.getDay());
            result.put("hour", request.getHour());
            result.put("ai_congestion", congestion);

            return ResponseEntity.ok(result);

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "예측값이 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/predict/weekly")
    public ResponseEntity<?> getWeeklyPrediction(
            @RequestParam int location_id,
            @RequestParam int hour) {
        try {
            Map<String, Integer> weekly = predictionService.getWeeklyPrediction(location_id, hour);
            return ResponseEntity.ok(weekly);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
