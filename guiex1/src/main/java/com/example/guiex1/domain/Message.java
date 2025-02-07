package com.example.guiex1.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long>{
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime time;
    private Message reply;
    private MessageType type;

    public Message(User from, List<User> to, String message, LocalDateTime time, MessageType type) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.time = time;
        this.reply = null;
        this.type = type;
    }

    public Message(User from, List<User> to, String message, MessageType type) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.time = LocalDateTime.now();
        this.reply = null;
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public List<User> getTo() {
        return to;
    }

    public void setTo(List<User> to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Message getReply() {
        return reply;
    }

    public void setReply(Message reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from=" + from +
                ", to=" + to +
                ", message='" + message + '\'' +
                ", time=" + time;
    }
}
