package com.revature.yolp.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.yolp.dtos.requests.NewUserRequest;
import com.revature.yolp.dtos.responses.Principal;
import com.revature.yolp.models.Role;
import com.revature.yolp.models.User;
import com.revature.yolp.services.TokenService;
import com.revature.yolp.services.UserService;
import com.revature.yolp.utils.custom_exceptions.InvalidAuthException;
import com.revature.yolp.utils.custom_exceptions.InvalidUserException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/* purpose of this UserHandler class is to handle http verbs and endpoints */
/* hierarchy dependency injection -> userhandler -> userservice -> userdao */
public class UserHandler {
    private final UserService userService;
    private final TokenService tokenService;
    private final ObjectMapper mapper;
    private final static Logger logger = LoggerFactory.getLogger(User.class);

    public UserHandler(UserService userService, TokenService tokenService, ObjectMapper mapper) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.mapper = mapper;
    }

    public void signup(Context ctx) throws IOException {
        NewUserRequest req = mapper.readValue(ctx.req.getInputStream(), NewUserRequest.class);
        try {
            logger.info("Attempting too signup...");

            User createdUser = null;

            if (userService.isValidUsername(req.getUsername())) {
                if (userService.isUniqueUsername(req.getUsername())) {
                    if (userService.isValidPassword(req.getPassword1())) {
                        if (userService.isSamePassword(req.getPassword1(), req.getPassword2())) {
                            createdUser = userService.signup(req);
                        }
                    }
                }
            }

            ctx.status(201); // CREATED
            ctx.json(Objects.requireNonNull(createdUser));
            logger.info("Signup attempt successful...");
        } catch (InvalidUserException e) {
            ctx.status(403); // FORBIDDEN
            ctx.json(e);
            logger.info("Signup attempt unsuccessful...");
        }
    }

    public void getAllUsers(Context ctx) {
        try {
            String token = ctx.req.getHeader("authorization");
            if (token == null || token.isEmpty()) throw new InvalidAuthException("You are not signed in");

            Principal principal = tokenService.extractRequesterDetails(token);
            if (principal == null) throw new InvalidAuthException("Invalid token");
            if (!principal.getRole().equals(Role.ADMIN))
                throw new InvalidAuthException("You are not authorized to do this");

            List<User> users = userService.getAllUsers();
            ctx.json(users);
        } catch (InvalidAuthException e) {
            ctx.status(401);
            ctx.json(e);
        }
    }

    public void getAllUsersByUsername(Context ctx) {
        try {
            String token = ctx.req.getHeader("authorization");
            if (token == null || token.isEmpty()) throw new InvalidAuthException("You are not signed in");

            Principal principal = tokenService.extractRequesterDetails(token);
            if (principal == null) throw new InvalidAuthException("Invalid token");
            if (!principal.getRole().equals(Role.ADMIN))
                throw new InvalidAuthException("You are not authorized to do this");

            String username = ctx.req.getParameter("username");
            List<User> users = userService.getAllUsersByUsername(username);
            ctx.json(users);
        } catch (InvalidAuthException e) {
            ctx.status(401);
            ctx.json(e);
        }
    }
}
