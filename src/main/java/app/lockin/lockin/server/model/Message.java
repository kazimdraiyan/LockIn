package app.lockin.lockin.server.model;

import java.io.Serializable;

public abstract class Message implements Serializable {
    MessageType type;

    abstract MessageType getType();
}
