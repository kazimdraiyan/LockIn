package app.lockin.lockin.common.models;

import java.io.Serializable;

// TODO: Is this redundant?
public class MessageRealtimeEvent implements Serializable {
    private final MessageDelivery delivery;

    public MessageRealtimeEvent(MessageDelivery delivery) {
        this.delivery = delivery;
    }

    public MessageDelivery getDelivery() {
        return delivery;
    }
}
