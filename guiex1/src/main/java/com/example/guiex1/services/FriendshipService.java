package com.example.guiex1.services;

import com.example.guiex1.domain.*;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.repository.dbrepo.FriendRequestsDBRepository;
import com.example.guiex1.repository.dbrepo.FriendshipDBRepository;
import com.example.guiex1.repository.dbrepo.UserDbRepository;
import com.example.guiex1.repository.paging.Page;
import com.example.guiex1.repository.paging.Pageable;
import com.example.guiex1.repository.paging.PagingRepository;
import com.example.guiex1.utils.observer.Observer;
import com.example.guiex1.utils.observer.Observer2;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FriendshipService {
    private final PagingRepository<Tuple<Long, Long>, Friendship> friendshipPagingRepo;
    private final Repository<Tuple<Long, Long>, Friendship> repositoryFriendship;
    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository;
    private final Repository<Long, Message> messageRepo;
    private final List<Observer2> observers = new ArrayList<>();

    public FriendshipService(PagingRepository<Tuple<Long, Long>, Friendship> friendshipPagingRepo, Repository<Tuple<Long, Long>, Friendship>  repositoryFriendship, Repository<Long, User>  userRepository, Repository<Tuple<Long, Long>, FriendRequests> friendRequestsRepository, Repository<Long, Message> messageRepo) {
        this.friendshipPagingRepo = friendshipPagingRepo;
        this.repositoryFriendship = repositoryFriendship;
        this.userRepository = userRepository;
        this.friendRequestsRepository = friendRequestsRepository;
        this.messageRepo = messageRepo;
    }

    /**
     * @return list of friendships
     */
    public Iterable<Friendship> getFriendships(){
        return repositoryFriendship.findAll();
    }

    /**
     * @param userID1 - long
     * @param userID2 - long
     * @throws ValidationException if friendship already exists
     */
    public void addFriendship(Long userID1, Long userID2) {
        User user1 = userRepository.findOne(userID1).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(userID2).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElseThrow(() -> new ValidationException("Friendship request not found"));
        friendRequest.setStatus(FriendshipStatus.Accepted);
        friendRequestsRepository.update(friendRequest);

        Friendship friendship = new Friendship();
        friendship.setId(new Tuple<>(user1.getId(), user2.getId()));

        if(repositoryFriendship.findOne(new Tuple<>(user2.getId(), user1.getId())).isPresent()) {
            throw new ValidationException("Friendship already exists");
        }

        if(repositoryFriendship.findOne(new Tuple<>(user1.getId(), user2.getId())).isPresent()) {
            throw new ValidationException("Friendship already exists");
        }

        repositoryFriendship.save(friendship);
        notifyObservers();
    }

    /**
     * @param userID1 - long
     * @param userID2 - long
     * @throws ValidationException if the friendship doesnt exist
     */
    public void removeFriendship(Long userID1, Long userID2) {
        User user1 = userRepository.findOne(userID1).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(userID2).orElseThrow(() -> new ValidationException("User not found"));

        if(friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).isPresent()){
            FriendRequests friendRequests = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElse(null);
            friendRequests.setStatus(FriendshipStatus.Deleted);
            friendRequestsRepository.update(friendRequests);
        }else if(friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).isPresent()){
            FriendRequests friendRequests = friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).orElse(null);
            friendRequests.setStatus(FriendshipStatus.Deleted);
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
            if(friendRequest.getStatus()!=FriendshipStatus.Rejected && friendRequest.getStatus()!=FriendshipStatus.Deleted) {
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

    public void sendFriendInvite(Long id, Long id1) {
        User user1 = userRepository.findOne(id).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(id1).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = new FriendRequests();
        friendRequest.setId(new Tuple<>(user1.getId(), user2.getId()));

        Message message = new Message(user2, Collections.singletonList(user2), user1.getFirstName() + " " + user1.getLastName() + " sent you a friend request", MessageType.Notification);
        messageRepo.save(message);

        if(friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).isPresent()) {
            FriendRequests friendRequest12 = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElse(null);
            if(friendRequest12.getStatus() == FriendshipStatus.Deleted || friendRequest12.getStatus() == FriendshipStatus.Rejected) {
                friendRequest12.setStatus(FriendshipStatus.Pending);
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
            if(friendRequest12.getStatus() == FriendshipStatus.Deleted || friendRequest12.getStatus() == FriendshipStatus.Rejected) {
                friendRequest12.setStatus(FriendshipStatus.Pending);
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
        friendRequest.setStatus(FriendshipStatus.Rejected);

        friendRequestsRepository.update(friendRequest);
        notifyObservers();
    }

    public List<User> getReceivedInvites(User currentUser) {
        List<User> users = new ArrayList<>();
        friendRequestsRepository.findAll().forEach(friendRequest -> {
            if(friendRequest.getStatus()!=FriendshipStatus.Rejected && friendRequest.getStatus()!=FriendshipStatus.Accepted) {
                if (friendRequest.getId().getE2().equals(currentUser.getId())) {
                    User user = userRepository.findOne(friendRequest.getId().getE1()).orElseThrow(() -> new ValidationException("User not found"));
                    users.add(user);
                }
            }
        });
        return users;
    }

    public List<User> getSentInvites(User currentUser) {
        List<User> users = new ArrayList<>();
        friendRequestsRepository.findAll().forEach(friendRequest -> {
                if (friendRequest.getId().getE1().equals(currentUser.getId())) {
                    User user = userRepository.findOne(friendRequest.getId().getE2()).orElseThrow(() -> new ValidationException("User not found"));
                    users.add(user);
                }
        });
        return users;
    }

    public LocalDateTime getInviteDate(Long id, Long id1) {
        User user1 = userRepository.findOne(id).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(id1).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = friendRequestsRepository.findOne(new Tuple<>(user2.getId(), user1.getId())).orElseThrow(() -> new ValidationException("friendship doesnt exist"));
        return friendRequest.getDate();
    }

    public FriendshipStatus getInviteStatus(Long id, Long id1) {
        User user1 = userRepository.findOne(id).orElseThrow(() -> new ValidationException("User not found"));
        User user2 = userRepository.findOne(id1).orElseThrow(() -> new ValidationException("User not found"));

        FriendRequests friendRequest = friendRequestsRepository.findOne(new Tuple<>(user1.getId(), user2.getId())).orElseThrow(() -> new ValidationException("friendship doesnt exist"));
        return friendRequest.getStatus();
    }

    public Page<User> getPaginatedFriendsForUser(User currentUser, int pageNumber, int pageSize) {
        Pageable pageable = new Pageable(pageNumber, pageSize);
        Page<Friendship> friendships = friendshipPagingRepo.findAllPaged(currentUser.getId(), pageable);
        List<Friendship> friendshipsList = new ArrayList<>();
        friendships.getElementsOnPage().forEach(friendshipsList::add);

        // Map friendships to corresponding User objects
        List<User> users = friendshipsList.stream()
                .map(friendship -> {
                    Long friendId = friendship.getId().getE1().equals(currentUser.getId())
                            ? friendship.getId().getE2()
                            : friendship.getId().getE1();
                    return userRepository.findOne(friendId);
                })
                .flatMap(Optional::stream)// Exclude null results if any
                .collect(Collectors.toList());

        // Return a new Page object with the users and total element count
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
