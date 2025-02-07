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

import java.util.*;
import java.util.stream.Collectors;

public class MessageService implements Observable<UserEntityChangeEvent> {
    private final Repository<Long, Message> messageRepository;
    private final Repository<Long, User> userRepository;
    private final List<Observer<UserEntityChangeEvent>> observers = new ArrayList<>();

    public MessageService(Repository<Long, Message> messageRepository, Repository<Long, User> userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public void addMessage(Long id_from, Long id_to, String message) {
        try{
            User from = userRepository.findOne(id_from).orElseThrow(() -> new ValidationException("User not found"));
            User to = userRepository.findOne(id_to).orElseThrow(() -> new ValidationException("User not found"));

            Message msg = new Message(from, Collections.singletonList(to), message, MessageType.Message);
            messageRepository.save(msg);

            List<Message> messagesBetweenUsers = getMessages(id_to, id_from);
            if(messagesBetweenUsers.size() > 1){
                Message oldReplyMessage = messagesBetweenUsers.get(messagesBetweenUsers.size() - 2);
                Message newReplyMessage = messagesBetweenUsers.get(messagesBetweenUsers.size() - 1);
                oldReplyMessage.setReply(newReplyMessage);
                messageRepository.update(oldReplyMessage);
            }

            UserEntityChangeEvent event = new UserEntityChangeEvent(ChangeEventType.UPDATE, from, to);
            notifyObservers(event);
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public ArrayList<Message> getMessages(Long id_from, Long id_to){
        Collection<Message> messages = (Collection<Message>) messageRepository.findAll();
        return  messages.stream()
                .filter(msg -> (msg.getType().equals(MessageType.Message) && ((msg.getFrom().getId().equals(id_from)) && msg.getTo().get(0).getId().equals(id_to)) || (msg.getFrom().getId().equals(id_to) && msg.getTo().get(0).getId().equals(id_from))))
                .sorted(Comparator.comparing(Message::getTime))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Message> getNotifications(Long id_from){
        Collection<Message> messages = (Collection<Message>) messageRepository.findAll();
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
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UserEntityChangeEvent t) {
        observers.forEach(observer -> observer.update(t));
    }
}
