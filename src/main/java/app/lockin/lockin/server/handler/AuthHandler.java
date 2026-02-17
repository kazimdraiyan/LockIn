package app.lockin.lockin.server.handler;

import app.lockin.lockin.server.request.LoginRequest;
import app.lockin.lockin.server.request.LogoutRequest;
import app.lockin.lockin.server.request.Request;
import app.lockin.lockin.server.request.SignUpRequest;
import app.lockin.lockin.server.response.Response;

// Handles auth related requests
public class AuthHandler {
    public AuthHandler() {}

    // Example usage:
    public Response handleLogin(LoginRequest request) {
        return null;
    }

    public Response handleLogout(LogoutRequest request) {
        return null;
    }

    public Response handleSignUp(SignUpRequest request) {
        System.out.println("Sign up request from " + request.getName() + " with " + request.getEmail());
        return null;
    }
}
