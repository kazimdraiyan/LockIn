package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.ConversationData;
import app.lockin.lockin.common.requests.CreateMessageRequest;
import app.lockin.lockin.common.requests.FetchMessagesRequest;
import app.lockin.lockin.common.requests.FetchRequest;
import app.lockin.lockin.common.response.Response;
import app.lockin.lockin.common.response.ResponseStatus;
import app.lockin.lockin.server.services.MessageService;

import java.io.IOException;
import java.util.ArrayList;

public class MessageHandler {
    private final MessageService messageService = new MessageService();

    public Response handleFetchChats(FetchRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before opening chats", null);
        }

        try {
            ArrayList<Chat> chats = messageService.loadChats(request.authenticatedSession.getUsername());
            return new Response(ResponseStatus.SUCCESS, "Chats loaded successfully", chats);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    public Response handleFetchMessages(FetchMessagesRequest request) {
        if (request.authenticatedSession == null) {
            return new Response(ResponseStatus.ERROR, "Please log in before opening messages", null);
        }

        try {
            ConversationData conversationData = messageService.loadConversation(
                    request.authenticatedSession.getUsername(),
                    request.getOtherUsername()
            );
            return new Response(ResponseStatus.SUCCESS, "Messages loaded successfully", conversationData);
        } catch (IOException e) {
            return new Response(ResponseStatus.ERROR, e.getMessage(), null);
        }
    }

    public MessageService.MessageCreationResult handleCreateMessage(CreateMessageRequest request) throws IOException {
        if (request.authenticatedSession == null) {
            throw new IOException("Please log in before sending messages");
        }

        return messageService.createMessage(
                request.authenticatedSession.getUsername(),
                request.getRecipientUsername(),
                request.getText(),
                request.getAttachment(),
                request.getReplyOf()
        );
    }
}
