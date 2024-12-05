package com.example.guiex1.domain;

import java.time.LocalDateTime;


public class Friendship extends Entity<Tuple<Long,Long>> {

    private LocalDateTime date = LocalDateTime.now();

    public Friendship(LocalDateTime date) {
        this.date = date;
    }

    public Friendship() {
    }

    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return date;
    }
}
