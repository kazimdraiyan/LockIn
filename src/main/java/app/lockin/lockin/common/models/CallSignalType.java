package app.lockin.lockin.common.models;

import java.io.Serializable;

public enum CallSignalType implements Serializable {
    RINGING,
    ANSWERED,
    ENDED,
    INCOMING, // Callee perspective
    PENDING, // Server-side state
}
