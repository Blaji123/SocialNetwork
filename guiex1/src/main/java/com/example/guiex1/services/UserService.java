package com.example.guiex1.services;

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

public class UserService implements Observable<UserEntityChangeEvent> {
    private final Repository<Long, User> repo;
    private final List<Observer<UserEntityChangeEvent>> observers=new ArrayList<>();
    private static final String DEFAULT_PHOTO_PATH = "/home/blaji/Downloads/lab6-exemplu-GUI1/guiex1/src/main/resources/com/example/guiex1/images/default_anonymous_photo.jpg";

    public UserService(Repository<Long, User> repo) {
        this.repo = repo;
    }

    private byte[] loadDefaultPhoto(){
        try{
            return Files.readAllBytes(Paths.get(DEFAULT_PHOTO_PATH));
        }catch (IOException e){
            throw new RuntimeException("Default photo could not be uploaded", e);
        }
    }

    public void addUser(User user) {
        if(repo.findByEmail(user.getEmail()).isPresent()){
            throw new ValidationException("Email already in use");
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        user.setPhoto(loadDefaultPhoto());

        if(repo.save(user).isEmpty()){
            UserEntityChangeEvent event = new UserEntityChangeEvent(ChangeEventType.ADD, user);
            notifyObservers(event);
        }
    }

    public void deleteUser(Long id){
        Optional<User> user=repo.delete(id);
        user.ifPresent(value -> notifyObservers(new UserEntityChangeEvent(ChangeEventType.DELETE, value)));
    }

    public void updateUser(User u) {
        Optional<User> oldUser=repo.findOne(u.getId());
        if(oldUser.isPresent()) {
            Optional<User> newUser=repo.update(u);
            if (newUser.isEmpty())
                notifyObservers(new UserEntityChangeEvent(ChangeEventType.UPDATE, u, oldUser.get()));
        }
    }

    public User authenticateUser(String email, String password) {
        User user = repo.findByEmail(email).orElse(null);
        if(user == null || !BCrypt.checkpw(password, user.getPassword())){
            return null;
        }
        return user;
    }

    @Override
    public void addObserver(Observer<UserEntityChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<UserEntityChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UserEntityChangeEvent t) {
        observers.forEach(x->x.update(t));
    }
}
