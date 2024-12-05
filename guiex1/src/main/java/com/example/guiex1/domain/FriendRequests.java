package com.example.guiex1.domain;

import java.time.LocalDateTime;

public class FriendRequests extends Entity<Tuple<Long, Long>> {
    private LocalDateTime date = LocalDateTime.now();
    private FriendshipStatus status = FriendshipStatus.Pending;

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public LocalDateTime getDate() {
        return date;
    }


    public FriendRequests() {
    }

    public FriendRequests(LocalDateTime date, FriendshipStatus status) {
        this.date = date;
        this.status = status;
    }

}
