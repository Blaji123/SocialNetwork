package com.example.guiex1.domain;

import java.time.LocalDateTime;

public class FriendRequests extends Entity<Tuple<Long, Long>> {
    private LocalDateTime date = LocalDateTime.now();
    private FriendshipRequestStatus status = FriendshipRequestStatus.Pending;

    public FriendRequests() {
    }

    public FriendRequests(LocalDateTime date, FriendshipRequestStatus status) {
        this.date = date;
        this.status = status;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(FriendshipRequestStatus status) {
        this.status = status;
    }

    public FriendshipRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
