package app.lockin.lockin.server.handler;

import app.lockin.lockin.server.request.LoginRequest;
import app.lockin.lockin.server.request.LogoutRequest;
import app.lockin.lockin.server.request.SignUpRequest;
import app.lockin.lockin.server.response.Response;
import app.lockin.lockin.server.service.AuthService;

import java.io.IOException;

// Handles auth related requests
public class AuthHandler {
    AuthService authService = new AuthService();

    public AuthHandler() {
    }

    // Example usage:
    public Response handleLogin(LoginRequest request) {
        return null;
    }

    public Response handleLogout(LogoutRequest request) {
        return null;
    }

    public Response handleSignUp(SignUpRequest request) {
        System.out.println("Sign up request from " + request.getUsername());
        try {
            authService.createUser(request.getUsername(), request.getPassword());
        } catch (IOException e) {
            System.out.println("An error occurred while trying to create the user");
        }
        return null;
    }
}
