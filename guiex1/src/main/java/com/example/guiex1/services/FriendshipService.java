package com.example.guiex1.services;

import com.example.guiex1.domain.*;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.repository.paging.Page;
import com.example.guiex1.repository.paging.Pageable;
import com.example.guiex1.repository.paging.PagingRepository;
import com.example.guiex1.utils.observer.Observer2;

import java.util.*;
import java.util.stream.Collectors;

public class FriendshipService {
    private final PagingRepository<Tuple<Long, Long>, Friendship> friendshipPagingRepo;
    private final Repository<Tuple<Long, Long>, Friendship> repositoryFriendship;
    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository;
    private final List<Observer2> observers = new ArrayList<>();

    public FriendshipService(PagingRepository<Tuple<Long, Long>, Friendship> friendshipPagingRepo, Repository<Tuple<Long, Long>, Friendship>  repositoryFriendship, Repository<Long, User>  userRepository, Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository) {
        this.friendshipPagingRepo = friendshipPagingRepo;
        this.repositoryFriendship = repositoryFriendship;
        this.userRepository = userRepository;
        this.friendRequestsRepository = friendRequestsRepository;
    }

    public Iterable<Friendship> getFriendships(){
        return repositoryFriendship.findAll();
    }

    public void addFriendship(Long userID1, Long userID2) {
        User user1 = userRepository.findOne(userID1).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(userID2).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElseThrow(() -> new ValidationException("Friendship request not found"));
        friendRequest.setStatus(FriendshipRequestStatus.Accepted);
        friendRequestsRepository.update(friendRequest);

        Friendship friendship = new Friendship();
        friendship.setId(new Tuple<>(user1.getId(), user2.getId()));

        if(repositoryFriendship.findOne(new Tuple<>(user2.getId(), user1.getId())).isPresent()
         || repositoryFriendship.findOne(new Tuple<>(user1.getId(), user2.getId())).isPresent()) {
            throw new ValidationException("Friendship already exists");
        }

        repositoryFriendship.save(friendship);
        notifyObservers();
    }

    public void removeFriendship(Long userID1, Long userID2) {
        User user1 = userRepository.findOne(userID1).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(userID2).orElseThrow(() -> new ValidationException("User not found"));

        if(friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).isPresent()){
            FriendRequests friendRequests = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElse(null);
            friendRequests.setStatus(FriendshipRequestStatus.Deleted);
            friendRequestsRepository.update(friendRequests);
        }else if(friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).isPresent()){
            FriendRequests friendRequests = friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).orElse(null);
            friendRequests.setStatus(FriendshipRequestStatus.Deleted);
            friendRequestsRepository.update(friendRequests);
        }else{
            throw new ValidationException("Friendship request not found");
        }

        if(repositoryFriendship.findOne(new Tuple<>(userID1, userID2)).isPresent()){
            repositoryFriendship.delete(new Tuple<>(userID1, userID2));
        }else if(repositoryFriendship.findOne(new Tuple<>(userID2, userID1)).isPresent()){
            repositoryFriendship.delete(new Tuple<>(userID2, userID1));
        }else{
            throw new ValidationException("Friendship not found");
        }
        notifyObservers();
    }

    public List<User> getFriendsForUser(User currentUser) {
        List<User> users = new ArrayList<>();
        getFriendships().forEach(friendship -> {
            if(friendship.getId().getE1().equals(currentUser.getId())) {
                User user = userRepository.findOne(friendship.getId().getE2()).orElseThrow(() -> new ValidationException("User not found"));
                users.add(user);
            }
            if(friendship.getId().getE2().equals(currentUser.getId())) {
                User user = userRepository.findOne(friendship.getId().getE1()).orElseThrow(() -> new ValidationException("User not found"));
                users.add(user);
            }
        });
        return users;
    }

    public List<User> getNotFriendsForUser(User currentUser) {
        List<User> users = getFriendsForUser(currentUser);
        List<User> notFriends = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            if(!users.contains(user) && !user.equals(currentUser)) {
                notFriends.add(user);
            }
        });
        friendRequestsRepository.findAll().forEach(friendRequest -> {
            if(friendRequest.getStatus()!= FriendshipRequestStatus.Rejected && friendRequest.getStatus()!= FriendshipRequestStatus.Deleted) {
                if (friendRequest.getId().getE1().equals(currentUser.getId())) {
                    User user = userRepository.findOne(friendRequest.getId().getE2()).orElseThrow(() -> new ValidationException("User not found"));
                    notFriends.remove(user);
                }
                if (friendRequest.getId().getE2().equals(currentUser.getId())) {
                    User user = userRepository.findOne(friendRequest.getId().getE1()).orElseThrow(() -> new ValidationException("User not found"));
                    notFriends.remove(user);
                }
            }
        });
        return notFriends;
    }

    public Page<User> getPaginatedFriendsForUser(User currentUser, int pageNumber, int pageSize) {
        Pageable pageable = new Pageable(pageNumber, pageSize);
        Page<Friendship> friendships = friendshipPagingRepo.findAllPaged(currentUser.getId(), pageable);
        List<Friendship> friendshipsList = new ArrayList<>();
        friendships.getElementsOnPage().forEach(friendshipsList::add);

        List<User> users = friendshipsList.stream()
                .map(friendship -> {
                    Long friendId = friendship.getId().getE1().equals(currentUser.getId())
                            ? friendship.getId().getE2()
                            : friendship.getId().getE1();
                    return userRepository.findOne(friendId);
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        return new Page<>(users, friendships.getTotalElementCount());
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
