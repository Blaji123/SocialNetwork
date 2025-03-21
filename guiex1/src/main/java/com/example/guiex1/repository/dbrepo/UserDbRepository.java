package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.Validator;
import com.example.guiex1.repository.Repository;


import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDbRepository implements Repository<Long, User> {
    protected final String url;
    protected final String username;
    protected final String password;
    protected final Validator<User> validator;

    public UserDbRepository(String url, String username, String password, Validator<User> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<User> findOne(Long id) {
        User user;

        try(Connection connection = DriverManager.getConnection(url, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery(String.format("select * from users U where U.id = '%d'", id))) {
            if(resultSet.next()){
                user = createUserFromResultSet(resultSet);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        User user = null;
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?")
        ){
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                user = createUserFromResultSet(resultSet);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(user);
    }

    private User createUserFromResultSet(ResultSet resultSet) {
        try {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            Long id = resultSet.getLong("id");
            String email = resultSet.getString("email");
            String password = resultSet.getString("password");
            byte[] photo = resultSet.getBytes("photo");

            User user = new User(firstName, lastName, email, password, photo);
            user.setId(id);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                byte[] photo = resultSet.getBytes("photo");

                User utilizator = new User(firstName, lastName, email, password, photo);
                utilizator.setId(id);
                users.add(utilizator);
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> save(User entity) {
        String sql = "insert into users (first_name, last_name, email, password, photo) values (?, ?, ?, ?, ?)";
        validator.validate(entity);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.setString(3, entity.getEmail());
            ps.setString(4, entity.getPassword());
            ps.setBytes(5, entity.getPhoto());

            ps.executeUpdate();
        } catch (SQLException e) {
            return Optional.ofNullable(entity);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> delete(Long id) {
        String sql = "delete from users where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Optional<User> user = findOne(id);
            if(user.isPresent()) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User user) {
        if(user == null)
            throw new IllegalArgumentException("entity must be not null!");
        validator.validate(user);
        String sql = "update users set first_name = ?, last_name = ?, email = ?, photo = ? where id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1,user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setBytes(4, user.getPhoto());
            ps.setLong(5, user.getId());
            if( ps.executeUpdate() > 0 )
                return Optional.empty();
            return Optional.of(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
