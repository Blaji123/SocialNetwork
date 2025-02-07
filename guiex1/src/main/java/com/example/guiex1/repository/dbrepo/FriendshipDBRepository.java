package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Friendship;
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
    private Validator<Friendship> validator;

    public FriendshipDBRepository(Validator<Friendship> validator) {
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
            throw new RuntimeException(e);
        }
        this.validator = validator;
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        Friendship friendship = null;

        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships WHERE \"id1\" = ? AND \"id2\" = ?")
            ){
            statement.setLong(1, id.getE1());
            statement.setLong(2, id.getE2());
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                LocalDateTime friendsFrom = resultSet.getTimestamp("friends_from").toLocalDateTime();
                friendship = new Friendship(friendsFrom);
                friendship.setId(id);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(friendship);
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
                LocalDateTime friendsFrom = resultSet.getTimestamp("friends_from").toLocalDateTime();
                Friendship friendship = new Friendship(friendsFrom);
                friendship.setId(new Tuple<>(id1, id2));
                friendships.add(friendship);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
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
            PreparedStatement statement = connection.prepareStatement("INSERT INTO friendships(id1, id2, friends_from) VALUES (?, ?, ?)")
            ){
            statement.setLong(1, entity.getId().getE1());
            statement.setLong(2, entity.getId().getE2());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        return Optional.of(entity);
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("DELETE FROM friendships WHERE \"id1\" = ? AND  \"id2\" = ?")
            ){
            statement.setLong(1,id.getE1());
            statement.setLong(2, id.getE2());
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        Friendship friendshipToDelete = null;
        for(Friendship friendship : findAll()){
            if(Objects.equals(friendship.getId(), id)){
                friendshipToDelete = friendship;
            }
        }

        return Optional.ofNullable(friendshipToDelete);
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        return Optional.empty();
    }
    @Override
    public Optional<Friendship> findByEmail(String email) {
        return Optional.empty();
    }
}
