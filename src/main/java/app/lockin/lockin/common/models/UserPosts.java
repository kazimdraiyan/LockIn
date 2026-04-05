package app.lockin.lockin.common.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserPosts implements Serializable {
    private final UserProfile profile;
    private final ArrayList<Post> posts;

    public UserPosts(UserProfile profile, List<Post> posts) {
        this.profile = profile;
        this.posts = posts == null ? new ArrayList<>() : new ArrayList<>(posts);
    }

    public UserProfile getProfile() {
        return profile;
    }

    public ArrayList<Post> getPosts() {
        return new ArrayList<>(posts);
    }
}
