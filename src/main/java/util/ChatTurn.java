package util;

import java.io.Serializable;

public class ChatTurn implements Serializable {

    private final String role;
    private final String text;

    public ChatTurn(String role, String text) {
        this.role = role;
        this.text = text;
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }
}
