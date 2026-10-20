package in9cow.in9cow_spring.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private DataSource dataSource;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        Map<String, String> response = new HashMap<>();

        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();

        if (username == null || email == null || password == null) {
            response.put("message", "필수 정보가 누락되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.findByEmail(email).isPresent()) {
            response.put("message", "이미 가입된 이메일입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        try {
            // 저장 처리
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword_hash(request.getPassword());

            userRepository.save(user);
            System.out.println("✅ DB 저장 성공");

            response.put("message", "회원가입 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "서버 오류");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        String email = payload.get("email");
        String password = payload.get("password");

        if (email == null || password == null) {
            response.put("message", "이메일과 비밀번호를 입력하세요.");
            return ResponseEntity.badRequest().body(response);
        }

        return userRepository.findByEmail(email).map(user -> {
            if (user.getPassword_hash().equals(password)) {
                response.put("message", "로그인 성공");
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("user_id", user.getUser_id());
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "비밀번호가 일치하지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        }).orElseGet(() -> {
            response.put("message", "존재하지 않는 이메일입니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        });
    }

}