package app.lockin.lockin.server.handlers;

import app.lockin.lockin.common.models.Chat;
import app.lockin.lockin.common.models.ConversationData;
import app.lockin.lockin.common.models.MessageDelivery;
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

    public MessageCommandResult handleCreateMessage(CreateMessageRequest request) {
        if (request.authenticatedSession == null) {
            return MessageCommandResult.error("Please log in before sending messages");
        }

        try {
            MessageService.MessageCreationResult result = messageService.createMessage(
                    request.authenticatedSession.getUsername(),
                    request.getRecipientUsername(),
                    request.getText(),
                    request.getAttachment(),
                    request.getReplyOf()
            );
            return MessageCommandResult.success(result);
        } catch (IOException e) {
            return MessageCommandResult.error(e.getMessage());
        }
    }

    // TODO: Find alternative of this nested class
    public static class MessageCommandResult {
        private final Response response;
        private final String senderUsername;
        private final String recipientUsername;
        private final MessageDelivery senderDelivery;
        private final MessageDelivery recipientDelivery;

        private MessageCommandResult(
                Response response,
                String senderUsername,
                String recipientUsername,
                MessageDelivery senderDelivery,
                MessageDelivery recipientDelivery
        ) {
            this.response = response;
            this.senderUsername = senderUsername;
            this.recipientUsername = recipientUsername;
            this.senderDelivery = senderDelivery;
            this.recipientDelivery = recipientDelivery;
        }

        public static MessageCommandResult success(MessageService.MessageCreationResult result) {
            MessageDelivery senderDelivery = result.getSenderDelivery();
            return new MessageCommandResult(
                    new Response(ResponseStatus.SUCCESS, "Message sent successfully", senderDelivery),
                    result.getSenderUsername(),
                    result.getRecipientUsername(),
                    senderDelivery,
                    result.getRecipientDelivery()
            );
        }

        public static MessageCommandResult error(String message) {
            return new MessageCommandResult(
                    new Response(ResponseStatus.ERROR, message, null),
                    null,
                    null,
                    null,
                    null
            );
        }

        public Response getResponse() {
            return response;
        }

        public String getSenderUsername() {
            return senderUsername;
        }

        public String getRecipientUsername() {
            return recipientUsername;
        }

        public MessageDelivery getSenderDelivery() {
            return senderDelivery;
        }

        public MessageDelivery getRecipientDelivery() {
            return recipientDelivery;
        }
    }
}
