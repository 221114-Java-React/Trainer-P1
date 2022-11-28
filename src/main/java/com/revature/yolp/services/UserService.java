package com.revature.yolp.services;

import com.revature.yolp.daos.UserDAO;
import com.revature.yolp.dtos.requests.NewLoginRequest;
import com.revature.yolp.dtos.requests.NewUserRequest;
import com.revature.yolp.dtos.responses.Principal;
import com.revature.yolp.models.Role;
import com.revature.yolp.models.User;
import com.revature.yolp.utils.custom_exceptions.InvalidAuthException;
import com.revature.yolp.utils.custom_exceptions.InvalidUserException;

import java.util.List;
import java.util.UUID;

/* purpose of UserService is to validate and retrieve data from the DAO (DATA ACCESS OBJECT) */
/* Service class is essentially an api */
public class UserService {
    /* dependency injection = when a class is dependent on another class */
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User signup(NewUserRequest req) {
        User createdUser = new User(UUID.randomUUID().toString(), req.getUsername(), req.getPassword1(), Role.DEFAULT);
        userDAO.save(createdUser);
        return createdUser;
    }

    public Principal login(NewLoginRequest req) {
        User validUser = userDAO.getUserByUsernameAndPassword(req.getUsername(), req.getPassword());
        if (validUser == null) throw new InvalidAuthException("Invalid username or password");
        return new Principal(validUser.getId(), validUser.getUsername(), validUser.getRole());
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getAllUsersByUsername(String username) {
        return userDAO.getAllUsersByUsername(username);
    }

    public boolean isValidUsername(String username) {
        return username.matches("^(?=[a-zA-Z0-9._]{8,20}$)(?!.*[_.]{2})[^_.].*[^_.]$");
    }

    public boolean isDuplicateUsername(String username) {
        List<String> usernames = userDAO.findAllUsernames();
        return usernames.contains(username);
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
    }

    public boolean isSamePassword(String password1, String password2) {
        return password1.equals(password2);
    }
}
