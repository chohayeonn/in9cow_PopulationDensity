package com.example.in9cow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

public class MypageFragment extends Fragment {

    private LinearLayout loggedInLayout;
    private TextView tvUserName, tvUserEmail;
    private TextView tvLoginNotice;
    private Button btnLogin, btnSignup, btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        loggedInLayout = view.findViewById(R.id.loggedInLayout);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvLoginNotice = view.findViewById(R.id.tvLoginNotice);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignup = view.findViewById(R.id.btnSignup);
        btnLogout = view.findViewById(R.id.btnLogout);

        // 로그인 정보 확인
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String email = prefs.getString("email", null);


        if (username != null && email != null) {
            // 로그인된 사용자
            tvUserName.setText(username);
            tvUserEmail.setText(email);
            loggedInLayout.setVisibility(View.VISIBLE);
            tvLoginNotice.setVisibility(View.GONE);
            btnLogin.setVisibility(View.GONE);
            btnSignup.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            // 로그인 안된 상태
            loggedInLayout.setVisibility(View.GONE);
            tvLoginNotice.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnSignup.setVisibility(View.VISIBLE);
        }

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        btnSignup.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), SignupActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();  //
            editor.clear(); // 로그인 정보 삭제
            editor.apply();

            // 프래그먼트 새로고침
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_fragment, new MypageFragment()) // 반드시 fragment_container ID가 있어야 함
                    .commit();
        });

        return view;
    }
}
