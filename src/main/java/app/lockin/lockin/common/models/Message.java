package app.lockin.lockin.common.models;

import java.io.Serializable;

public abstract class Message implements Serializable {
    MessageType type;

    abstract MessageType getType();
}
