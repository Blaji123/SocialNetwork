package com.example.guiex1.domain;

import java.time.LocalDateTime;


public class Friendship extends Entity<Tuple<Long,Long>> {
    private final LocalDateTime date;

    public Friendship() {
        date = LocalDateTime.now();
    }

    public Friendship(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
