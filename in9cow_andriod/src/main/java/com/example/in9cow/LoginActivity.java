package com.example.in9cow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText entLoginEmail, entLoginPassword;
    Button btnDoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        entLoginEmail = findViewById(R.id.entLoginEmail);
        entLoginPassword = findViewById(R.id.entLoginPassword);
        btnDoLogin = findViewById(R.id.btnDoLogin);

        btnDoLogin.setOnClickListener(v -> {
            String email = entLoginEmail.getText().toString().trim();
            String password = entLoginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // SHA-256 해시
            String hashedPassword = sha256(password);

            // EC2 퍼블릭 IP로 변경
            String url = "http://3.24.137.66:8080/api/user/login";

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("email", email);
                jsonBody.put("password", hashedPassword);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "JSON 생성 오류", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        String msg = response.optString("message");
                        if (msg.equals("로그인 성공")) {

                            String username = response.optString("username");
                            String userEmail = response.optString("email");
                            long userId = response.optLong("user_id", -1);

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("username", username);
                            editor.putString("email", email);
                            editor.putBoolean("isLoggedIn", true);
                            editor.putLong("user_id", userId);
                            editor.apply();

                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "서버 오류 또는 로그인 실패", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);

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
