package com.example.guiex1.services;

import com.example.guiex1.domain.Message;
import com.example.guiex1.domain.MessageType;
import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.ValidationException;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.utils.events.ChangeEventType;
import com.example.guiex1.utils.events.UserEntityChangeEvent;
import com.example.guiex1.utils.observer.Observable;
import com.example.guiex1.utils.observer.Observer;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UserService implements Observable<UserEntityChangeEvent> {
    private final Repository<Long, User> repo;
    private final Repository<Long, Message> messageRepo;
    private List<Observer<UserEntityChangeEvent>> observers=new ArrayList<>();
    private static final String DEFAULT_PHOTO_PATH = "/home/blaji/Downloads/lab6-exemplu-GUI1/guiex1/src/main/resources/com/example/guiex1/images/default_anonymous_photo.jpg";

    public UserService(Repository<Long, User> repo, Repository<Long, Message> messageRepo) {
        this.repo = repo;
        this.messageRepo = messageRepo;
    }

    private byte[] loadDefaultPhoto(){
        try{
            return Files.readAllBytes(Paths.get(DEFAULT_PHOTO_PATH));
        }catch (IOException e){
            throw new RuntimeException("Default photo could not be uploaded", e);
        }
    }

    public User addUser(User user) {
        if(repo.findByEmail(user.getEmail()).isPresent()){
            throw new ValidationException("Email already in use");
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        user.setPhoto(loadDefaultPhoto());

        if(repo.save(user).isEmpty()){
            UserEntityChangeEvent event = new UserEntityChangeEvent(ChangeEventType.ADD, user);
            notifyObservers(event);
            return null;
        }
        return user;
    }

    public User deleteUser(Long id){
        Optional<User> user=repo.delete(id);
        if (user.isPresent()) {
            notifyObservers(new UserEntityChangeEvent(ChangeEventType.DELETE, user.get()));
            return user.get();
        }
        return null;
    }

    public Iterable<User> getUsers(){
        return repo.findAll();
    }

    public boolean addMessage(Long id_from, Long id_to, String message) {
        try{
            User from = repo.findOne(id_from).get();
            User to = repo.findOne(id_to).get();

            Message msg = new Message(from, Collections.singletonList(to), message, MessageType.Message);
            messageRepo.save(msg);

            List<Message> messagesBetweenUsers = getMessages(id_to, id_from);
            if(messagesBetweenUsers.size() > 1){
                Message oldReplyMessage = messagesBetweenUsers.get(messagesBetweenUsers.size() - 2);
                Message newReplyMessage = messagesBetweenUsers.get(messagesBetweenUsers.size() - 1);
                oldReplyMessage.setReply(newReplyMessage);
                messageRepo.update(oldReplyMessage);
            }

            UserEntityChangeEvent event = new UserEntityChangeEvent(ChangeEventType.UPDATE, from, to);
            notifyObservers(event);
            return true;
        }catch (ValidationException ve){
            System.out.println(ve.getMessage());
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return false;
    }

    public ArrayList<Message> getMessages(Long id_from, Long id_to){
        User user1 = repo.findOne(id_from).get();
        User user2 = repo.findOne(id_to).get();

        Collection<Message> messages = (Collection<Message>) messageRepo.findAll();
        return  messages.stream()
                .filter(msg -> (msg.getType().equals(MessageType.Message) && ((msg.getFrom().getId().equals(id_from)) && msg.getTo().get(0).getId().equals(id_to)) || (msg.getFrom().getId().equals(id_to) && msg.getTo().get(0).getId().equals(id_from))))
                .sorted(Comparator.comparing(Message::getTime))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Message> getNotifications(Long id_from){
        User user1 = repo.findOne(id_from).get();

        Collection<Message> messages = (Collection<Message>) messageRepo.findAll();
        return messages.stream()
                .filter(msg -> (msg.getType().equals(MessageType.Notification) && (msg.getFrom().getId().equals(id_from))))
                .sorted(Comparator.comparing(Message::getTime))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void addObserver(Observer<UserEntityChangeEvent> e) {
        observers.add(e);

    }

    @Override
    public void removeObserver(Observer<UserEntityChangeEvent> e) {
        //observers.remove(e);
    }

    @Override
    public void notifyObservers(UserEntityChangeEvent t) {

        observers.stream().forEach(x->x.update(t));
    }

    public User updateUser(User u) {
        Optional<User> oldUser=repo.findOne(u.getId());
        if(oldUser.isPresent()) {
            Optional<User> newUser=repo.update(u);
            if (newUser.isEmpty())
                notifyObservers(new UserEntityChangeEvent(ChangeEventType.UPDATE, u, oldUser.get()));
            return newUser.orElse(null);
        }
        return oldUser.orElse(null);
    }


    public User authenticateUser(String email, String password) {
        User user = repo.findByEmail(email).orElse(null);

        if(user == null || !BCrypt.checkpw(password, user.getPassword())){
            return null;
        }

        return user;
    }
}
