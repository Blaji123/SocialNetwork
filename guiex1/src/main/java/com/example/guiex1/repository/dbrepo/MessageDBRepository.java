package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.MessageType;
import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.Validator;
import com.example.guiex1.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository implements Repository<Long, Message> {

    private String url;
    private String username;
    private String password;
    private Validator validator;
    private Repository<Long, User> userRepository;

    public MessageDBRepository(Validator validator, Repository<Long, User> userRepository) {
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
        this.userRepository = userRepository;
    }

    public Optional<Message> findOneNoReply(Long id){
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM MESSAGES where id = ?")){
            statement.setLong(1,  id);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()){
                Long from_id = resultSet.getLong("from_id");
                Long to_id = resultSet.getLong("to_id");
                String message = resultSet.getString("message");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                MessageType type = MessageType.valueOf(resultSet.getString("type"));
                Message msg = new Message(userRepository.findOne(from_id).get(), Collections.singletonList(userRepository.findOne(to_id).get()), message, date, type);
                msg.setId(id);
                return Optional.of(msg);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> findOne(Long aLong) {
        Message msg = null;
        if(findOneNoReply(aLong).isPresent()){
            msg = findOneNoReply(aLong).get();
        }else return Optional.empty();

        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE id = ?")){
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();
            Long reply_id = resultSet.getLong("result_id");
            if(!resultSet.next()){
                Message replyMessage;
                if(findOneNoReply(reply_id).isPresent()){
                    replyMessage = findOneNoReply(reply_id).get();
                }else return Optional.empty();

                msg.setReply(replyMessage);
                return Optional.of(msg);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages")){
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                Long id = resultSet.getLong("id");
                Long from_id = resultSet.getLong("from_id");
                Long to_id = resultSet.getLong("to_id");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                String message = resultSet.getString("message");
                Long reply_id = resultSet.getLong("reply_id");
                User from = userRepository.findOne(from_id).get();
                List<User> to = Collections.singletonList(userRepository.findOne(to_id).get());
                MessageType type = MessageType.valueOf(resultSet.getString("type"));
                Message msg = new Message(from, to, message, date, type);
                msg.setId(id);
                messages.add(msg);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return messages;
    }

    @Override
    public Optional<Message> save(Message entity) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO messages(from_id, to_id, date, message, reply_id, type) VALUES (?,?,?,?,?,?)")){
            statement.setLong(1, entity.getFrom().getId());
            statement.setLong(2, entity.getTo().get(0).getId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getTime()));
            statement.setString(4, entity.getMessage());
            if(entity.getReply() == null){
                statement.setNull(5, Types.NULL);
            }else statement.setLong(5, entity.getReply().getId());
            statement.setString(6, entity.getType().toString());
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("DELETE FROM messages WHERE id = ?")){
            statement.setLong(1, aLong);
            statement.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("UPDATE messages SET from_id = ?, to_id = ?, date = ?, message = ?, reply_id = ?, type = ? WHERE id = ?")){
            statement.setLong(1, entity.getFrom().getId());
            statement.setLong(2, entity.getTo().get(0).getId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getTime()));
            statement.setString(4, entity.getMessage());
            statement.setLong(5, entity.getReply().getId());
            statement.setString(6, entity.getType().toString());
            statement.setLong(7, entity.getId());
            statement.executeUpdate();
            return Optional.of(entity);
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
