package com.demo.be.service;

import com.demo.be.dto.auth.ChangePasswordRequest;
import com.demo.be.dto.auth.LoginRequest;
import com.demo.be.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
