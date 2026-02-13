package com.example.in9cow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
public class SignupActivity extends AppCompatActivity {

    EditText entSignupName, entSignupEmail, entSignupPassword, entSignupConfirmPassword;
    Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        entSignupName = findViewById(R.id.entSignupName);
        entSignupEmail = findViewById(R.id.entSignupEmail);
        entSignupPassword = findViewById(R.id.entSignupPassword);
        entSignupConfirmPassword = findViewById(R.id.entSignupConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnDoSignup);

        btnCreateAccount.setOnClickListener(v -> {
            String name = entSignupName.getText().toString().trim();
            String email = entSignupEmail.getText().toString().trim();
            String password = entSignupPassword.getText().toString().trim();
            String confirmPassword = entSignupConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignupActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashedPassword = sha256(password);

            // EC2 퍼블릭 IP로 변경
            String url = "http://3.24.137.66:8080/api/user/register";

            // JSON 바디 생성
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("username", name);
                jsonBody.put("email", email);
                jsonBody.put("password", hashedPassword);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "JSON 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            String msg = response.getString("message"); // ✅ JSON 응답 파싱
                            Toast.makeText(SignupActivity.this, msg, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SignupActivity.this, "응답 파싱 오류", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace(); // ✅ 여기선 'error'가 맞음
                        Toast.makeText(SignupActivity.this, "회원가입 실패: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    return headers;
                }
            };

            Volley.newRequestQueue(SignupActivity.this).add(request);
        });
    }
    // SHA-256 해싱 함수
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
