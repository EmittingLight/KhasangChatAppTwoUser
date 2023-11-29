package yaga.ex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private String username;
    private List<String> privateMessages;

    public User(String username) {
        this.username = username;
        this.privateMessages = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public List<String> getPrivateMessages() {
        return privateMessages;
    }

    public void addPrivateMessage(String message) {
        privateMessages.add(message);
    }

    @Override
    public String toString() {
        return username; // Используем имя пользователя как текстовое представление
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
