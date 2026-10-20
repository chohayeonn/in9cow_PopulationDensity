package in9cow.in9cow_spring.congestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CongestionController {

    @Autowired
    private CongestionService congestionService;

    @GetMapping("/congestion")
    public List<CongestionDTO> getCongestion() {
        return congestionService.getCurrentHourCongestion();
    }

    @GetMapping("/recommendation")
    public List<CongestionDTO> getRecommendation(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "붐비는 순") String sort) {
        return congestionService.getRecommendations(category, sort);
    }
}