package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Friendship;
import com.example.guiex1.domain.FriendshipStatus;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.validators.Validator;
import com.example.guiex1.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class FriendshipDBRepository implements Repository<Tuple<Long, Long>, Friendship> {

    protected String url;
    protected String username;
    protected String password;
    private Validator validator;

    public FriendshipDBRepository(Validator validator) {
        try(InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")){
            Properties prop = new Properties();
            if(input == null){
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            prop.load(input);
            this.url = prop.getProperty("db.url");
            this.username = prop.getProperty("db.username");
            this.password = prop.getProperty("db.password");
        }catch (IOException e){
            e.printStackTrace();
        }
        this.validator = validator;
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> userUserTuple) {
        Friendship friendship = null;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships WHERE \"id1\" = ? AND \"id2\" = ?");
            ){
            statement.setLong(1, userUserTuple.getE1());
            statement.setLong(2, userUserTuple.getE2());
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                Timestamp date = resultSet.getTimestamp("friends_from");
                LocalDateTime friendFrom = new java.sql.Timestamp(date.getTime()).toLocalDateTime();
                friendship = new Friendship(friendFrom);
                friendship.setId(userUserTuple);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.ofNullable(friendship);
    }

    @Override
    public Optional<Friendship> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> friendships = new HashSet<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships");
            ResultSet resultSet = statement.executeQuery()){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                Timestamp date = resultSet.getTimestamp("friends_from");
                LocalDateTime friendFrom = new java.sql.Timestamp(date.getTime()).toLocalDateTime();
                Friendship friendship = new Friendship(friendFrom);
                friendship.setId(new Tuple<>(id1, id2));
                friendships.add(friendship);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        if(entity == null){
            throw new IllegalArgumentException("Entity cannot be null");
        }
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO friendships(id1, id2, friends_from) VALUES (?, ?, ?)");
            ){
            statement.setLong(1, entity.getId().getE1());
            statement.setLong(2, entity.getId().getE2());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> userUserTuple) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("DELETE FROM friendships WHERE \"id1\" = ? AND  \"id2\" = ?");
            ){
            statement.setLong(1,userUserTuple.getE1());
            statement.setLong(2, userUserTuple.getE2());
            statement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
        Friendship friendshipToDelete = null;
        for(Friendship friendship : findAll()){
            if(Objects.equals(friendship.getId(), userUserTuple)){
                friendshipToDelete = friendship;
            }
        }
        return Optional.ofNullable(friendshipToDelete);
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        return Optional.empty();
    }
}
