package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.Comment;
import app.lockin.lockin.common.models.Post;
import app.lockin.lockin.common.requests.CreateCommentRequest;
import app.lockin.lockin.common.requests.CreatePostRequest;
import app.lockin.lockin.common.requests.DeletePostRequest;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import app.lockin.lockin.server.services.PostService;

import java.io.IOException;
import java.util.ArrayList;

public class PostHandler {
    private final PostService postService = new PostService();

    public Response handleCreatePost(CreatePostRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before posting", null);
        }

        try {
            Post post = postService.createPost(
                    request.authenticatedSession.getUsername(),
                    request.getTextContent(),
                    request.getAttachment()
            );
            return new Response(ResponseStatus.SUCCESS, "Post created successfully", post);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    public Response handleCreateComment(CreateCommentRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before commenting", null);
        }

        try {
            Comment comment = postService.createComment(
                    request.authenticatedSession.getUsername(),
                    request.getPostId(),
                    request.getTextContent(),
                    request.getAttachment()
            );
            return new Response(ResponseStatus.SUCCESS, "Comment added successfully", comment);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    public Response handleFetchPosts(FetchRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before loading posts", null);
        }

        try {
            ArrayList<Post> posts = postService.loadPosts();
            return new Response(ResponseStatus.SUCCESS, "Posts loaded successfully", posts);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, "An unknown error occurred", null);
        }
    }

    public Response handleDeletePost(DeletePostRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before deleting posts", null);
        }

        try {
            postService.deletePost(request.authenticatedSession.getUsername(), request.getPostId());
            return new Response(ResponseStatus.SUCCESS, "Post deleted successfully", null);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    public ArrayList<Post> loadPostsByAuthor(String username) throws IOException {
        return postService.loadPostsByAuthor(username);
    }
}
