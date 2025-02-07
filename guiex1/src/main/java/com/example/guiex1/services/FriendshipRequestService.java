package com.example.guiex1.services;

import com.example.guiex1.domain.*;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.utils.observer.Observer2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendshipRequestService{
    private final Repository<Long, User> userRepository;
    private final Repository<Long, Message> messageRepository;
    private final Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository;

    private final List<Observer2> observers = new ArrayList<>();

    public FriendshipRequestService(Repository<Long, User> userRepository, Repository<Long, Message> messageRepository, Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.friendRequestsRepository = friendRequestsRepository;
    }

    public void sendFriendInvite(Long id, Long id1) {
        User user1 = userRepository.findOne(id).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(id1).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = new FriendRequests();
        friendRequest.setId(new Tuple<>(user1.getId(), user2.getId()));

        Message message = new Message(user2, Collections.singletonList(user2), user1.getFirstName() + " " + user1.getLastName() + " sent you a friend request", MessageType.Notification);
        messageRepository.save(message);

        if(friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).isPresent()) {
            FriendRequests friendRequest12 = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElse(null);
            if(friendRequest12.getStatus() == FriendshipRequestStatus.Deleted || friendRequest12.getStatus() == FriendshipRequestStatus.Rejected) {
                friendRequest12.setStatus(FriendshipRequestStatus.Pending);
                friendRequest12.setId(new Tuple<>(user1.getId(), user2.getId()));

                FriendRequests friendRequests13 = new FriendRequests(friendRequest12.getDate(), friendRequest12.getStatus());
                friendRequests13.setId(new Tuple<>(user1.getId(), user2.getId()));

                friendRequestsRepository.delete(new Tuple<>(user2.getId(), user1.getId()));
                friendRequestsRepository.save(friendRequests13);
                notifyObservers();
                return;
            }else{
                throw new ValidationException("friend request exists");
            }
        }

        if(friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).isPresent()) {
            FriendRequests friendRequest12 = friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).orElse(null);
            if(friendRequest12.getStatus() == FriendshipRequestStatus.Deleted || friendRequest12.getStatus() == FriendshipRequestStatus.Rejected) {
                friendRequest12.setStatus(FriendshipRequestStatus.Pending);
                friendRequestsRepository.update(friendRequest12);
                notifyObservers();
                return;
            }else {
                throw new ValidationException("friend request exists");
            }
        }

        friendRequestsRepository.save(friendRequest);
        notifyObservers();
    }

    public void rejectFriendInvite(Long id, Long id1) {
        User user1 = userRepository.findOne(id).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(id1).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = new FriendRequests();
        friendRequest.setId(new Tuple<>(user2.getId(), user1.getId()));
        friendRequest.setStatus(FriendshipRequestStatus.Rejected);

        friendRequestsRepository.update(friendRequest);
        notifyObservers();
    }

    public List<User> getReceivedInvites(User currentUser) {
        List<User> users = new ArrayList<>();
        friendRequestsRepository.findAll().forEach(friendRequest -> {
            if(friendRequest.getStatus()!= FriendshipRequestStatus.Rejected && friendRequest.getStatus()!= FriendshipRequestStatus.Accepted) {
                if (friendRequest.getId().getE2().equals(currentUser.getId())) {
                    User user = userRepository.findOne(friendRequest.getId().getE1()).orElseThrow(() -> new ValidationException("User not found"));
                    users.add(user);
                }
            }
        });
        return users;
    }

    public void addObserver(Observer2 observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer2 observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        observers.forEach(Observer2::update);
    }

}
