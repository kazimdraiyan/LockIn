package app.lockin.lockin.server.model;

import java.io.Serializable;

public class Session implements Serializable {
    private String username;
    private String token;

    public Session(String token, String username) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
