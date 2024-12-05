package com.example.guiex1.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CommunityService {
    private FriendshipService friendshipService;
    private UserService userService;
    private HashMap<Long, List<Long>> adjList;

    public CommunityService(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    /**
     * @param v - long
     * @param visited - HashMap<Long, Boolean>
     */
    private void DFS(Long v, HashMap<Long, Boolean> visited){
        visited.put(v, true);
        if(adjList.containsKey(v)){
            adjList.get(v).stream().filter(x -> !visited.containsKey(x)).forEach(x -> DFS(x, visited));
        }
    }

    /**
     * @return int, nr of connected communities
     */
    public int connectedCommunities(){
        adjList = new HashMap<>();
        userService.getUsers().forEach(user -> {
            List<Long> friends = new ArrayList<>();
            friendshipService.getFriendships().forEach(friendship -> {
                if(friendship.getId().getE1().equals(user.getId())){
                    friends.add(friendship.getId().getE2());
                }
                if(friendship.getId().getE2().equals(user.getId())){
                    friends.add(friendship.getId().getE1());
                }
            });
            if(!friends.isEmpty()){
                this.adjList.put(user.getId(), friends);
            }
        });

        List<Long> ids = new ArrayList<>();
        userService.getUsers().forEach(user -> ids.add(user.getId()));

        AtomicInteger nrOfCommunitites = new AtomicInteger();
        HashMap<Long, Boolean> visited = new HashMap<>();
        ids.forEach(id -> {
            if(!visited.containsKey(id)){
                DFS(id, visited);
                nrOfCommunitites.getAndIncrement();
            }
        });
        return nrOfCommunitites.get();
    }

    /**
     * @return List<Long>, most social community
     */
    public List<Long> mostSocialCommunity(){
        adjList = new HashMap<>();
        AtomicReference<List<Long>> max = new AtomicReference<>(new ArrayList<>());
        userService.getUsers().forEach(user -> {
            List<Long> friends = new ArrayList<>();
            friendshipService.getFriendships().forEach(friendship -> {
                if(friendship.getId().getE1().equals(user.getId())){
                    friends.add(friendship.getId().getE2());
                }
                if(friendship.getId().getE2().equals(user.getId())){
                    friends.add(friendship.getId().getE1());
                }
            });
            if(!friends.isEmpty()){
                this.adjList.put(user.getId(), friends);
                if(max.get().size() < friends.size() + 1){
                    max.set(friends);
                    max.get().add(user.getId());
                }
            }
        });

        return max.get();
    }

}
