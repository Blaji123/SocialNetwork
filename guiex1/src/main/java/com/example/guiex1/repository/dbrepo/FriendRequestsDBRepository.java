package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.FriendRequests;
import com.example.guiex1.domain.FriendshipRequestStatus;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.validators.Validator;
import com.example.guiex1.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class FriendRequestsDBRepository implements Repository<Tuple<Long, Long>, FriendRequests> {
    private String url;
    private String username;
    private String password;
    private Validator<FriendRequests> validator;

    public FriendRequestsDBRepository(Validator<FriendRequests> validator) {
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
    public Optional<FriendRequests> findOne(Tuple<Long, Long> id) {
        FriendRequests friendRequest = null;

        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friend_requests WHERE \"id1\" = ? AND \"id2\" = ?")
        ){
            statement.setLong(1, id.getE1());
            statement.setLong(2, id.getE2());
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                FriendshipRequestStatus status = FriendshipRequestStatus.valueOf(resultSet.getString("status"));
                friendRequest = new FriendRequests(date, status);
                friendRequest.setId(id);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(friendRequest);
    }

    @Override
    public Iterable<FriendRequests> findAll() {
        Set<FriendRequests> friendRequests = new HashSet<>();

        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friend_requests");
            ResultSet resultSet = statement.executeQuery()){

            while (resultSet.next()){
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                FriendshipRequestStatus status = FriendshipRequestStatus.valueOf(resultSet.getString("status"));
                FriendRequests friendRequest = new FriendRequests(date, status);
                friendRequest.setId(new Tuple<>(id1, id2));
                friendRequests.add(friendRequest);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        return friendRequests;
    }

    @Override
    public Optional<FriendRequests> save(FriendRequests entity) {
        if(entity == null){
            throw new IllegalArgumentException("Entity cannot be null");
        }
        validator.validate(entity);
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO friend_requests(id1, id2, date, status) VALUES (?, ?, ?, ?)")
        ){
            statement.setLong(1, entity.getId().getE1());
            statement.setLong(2, entity.getId().getE2());
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(entity.getDate()));
            statement.setString(4, entity.getStatus().toString());
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

        return Optional.of(entity);
    }

    @Override
    public Optional<FriendRequests> delete(Tuple<Long, Long> id) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("DELETE FROM friend_requests WHERE \"id1\" = ? AND  \"id2\" = ?")
        ){
            statement.setLong(1,id.getE1());
            statement.setLong(2, id.getE2());
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        FriendRequests friendshipToDelete = null;
        for(FriendRequests friendRequests : findAll()){
            if(Objects.equals(friendRequests.getId(), id)){
                friendshipToDelete = friendRequests;
            }
        }

        return Optional.ofNullable(friendshipToDelete);
    }

    @Override
    public Optional<FriendRequests> update(FriendRequests entity) {
        if(entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(entity);

        String sql = "update friend_requests set date = ?, status = ? where id1 = ? and  id2 = ? ";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1,java.sql.Timestamp.valueOf(entity.getDate()));
            ps.setString(2, entity.getStatus().toString());
            ps.setLong(3, entity.getId().getE1());
            ps.setLong(4, entity.getId().getE2());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<FriendRequests> findByEmail(String email) {
        return Optional.empty();
    }
}
