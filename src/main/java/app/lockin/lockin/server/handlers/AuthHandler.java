package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.ProfilePageData;
import app.lockin.lockin.common.models.Session;
import app.lockin.lockin.common.models.UserProfile;
import app.lockin.lockin.common.requests.*;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import app.lockin.lockin.server.services.AuthService;

import java.io.IOException;
import java.util.ArrayList;

// Handles auth related requests
public class AuthHandler {
    AuthService authService = new AuthService();

    // Example usage:
    public Response handleLogin(LoginRequest request) {
        System.out.println("Login request from " + request.getUsername());
        try {
            Session session = authService.login(request.getUsername(), request.getPassword());
            if (session != null) {
                return new Response(ResponseStatus.SUCCESS, "Login successful", session);
            } else {
                return new Response(ResponseStatus.ERROR, "Invalid credentials", null);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while trying to handle a login request");
            return new Response(ResponseStatus.ERROR, "An unknown error occurred", null);
        }
    }

    public Response handleLoginUsingToken(LoginUsingTokenRequest request) {
        try {
            System.out.println("Login using token request: " + request.getToken());
            String username = authService.usernameFromToken(request.getToken());
            if (username != null) {
                return new Response(ResponseStatus.SUCCESS, "Login using token successful", new Session(request.getToken(), username));
            } else {
                return new Response(ResponseStatus.ERROR, "Invalid token", null);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while trying to handle a login using token request");
            return new Response(ResponseStatus.ERROR, "An unknown error occurred", null);
        }
    }

    public Response handleLogout(LogoutRequest request) {
        try {
            authService.removeSession(request.authenticatedSession);
            return new Response(ResponseStatus.SUCCESS, "Logout successful", null);
        } catch (IOException e) {
            System.out.println("An error occurred while trying to handle a logout request");
            return new Response(ResponseStatus.ERROR, "An unknown error occurred", null);
        }
    }

    public Response handleSignUp(SignUpRequest request) {
        System.out.println("Sign up request from " + request.getUsername());
        try {
            Session session = authService.createUser(request.getUsername(), request.getPassword()); // If no exceptions occur, token is expected to be non-null.
            if (session != null) {
                return new Response(ResponseStatus.SUCCESS, "Sign up successful", session);
            } else {
                System.out.println("If no exceptions are thrown during sign up, token should be non-null; but null is returned.");
                return new Response(ResponseStatus.ERROR, "An unknown error occurred. Please try again later.", null);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while trying to create the user");
            return new Response(ResponseStatus.ERROR, "An unknown error occurred. Please try again later.", null);
        }
    }

    public Response handleFetchChats(FetchRequest request) {
        System.out.println("Fetch chat list request from " + request.authenticatedSession);
        try {
            ArrayList<Chat> chats = authService.loadChats(request.authenticatedSession.getUsername());
            return new Response(ResponseStatus.SUCCESS, "Chat list successfully fetched", chats);
        } catch (IOException e) {
            System.out.println("An error occurred while trying to load chat list");
            return new Response(ResponseStatus.ERROR, "An unknown error occurred. Please try again later.", null);
        }
    }

    public Response handleFetchProfile(FetchRequest request, PostHandler postHandler) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before loading profile", null);
        }
        try {
            String username = request.authenticatedSession.getUsername();
            UserProfile profile = authService.loadProfile(username);
            ProfilePageData pageData = new ProfilePageData(profile, postHandler.loadPostsByAuthor(username));
            return new Response(ResponseStatus.SUCCESS, "Profile loaded successfully", pageData);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    public Response handleUpdateProfile(UpdateProfileRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before updating profile", null);
        }
        try {
            UserProfile profile = authService.updateProfile(
                    request.authenticatedSession.getUsername(),
                    request.getDescription(),
                    request.getProfilePicture()
            );
            return new Response(ResponseStatus.SUCCESS, "Profile updated successfully", profile);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }
}
