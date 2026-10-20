package in9cow.in9cow_spring.Favorite;

import in9cow.in9cow_spring.congestion.CongestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public String addFavorite(@RequestBody FavoriteDTO favoriteDTO) {
        favoriteService.addFavorite(favoriteDTO.getUserId(), favoriteDTO.getLocationId());
        return "즐겨찾기 추가 완료";
    }

    @PostMapping("/remove")
    public String removeFavorite(@RequestBody FavoriteDTO favoriteDTO) {
        favoriteService.removeFavorite(favoriteDTO.getUserId(), favoriteDTO.getLocationId());
        return "즐겨찾기 삭제 완료";
    }

    @GetMapping("/list")
    public List<CongestionDTO> getFavorites(@RequestParam long userId) {
        return favoriteService.getFavoritesByUserId(userId);
    }

}

