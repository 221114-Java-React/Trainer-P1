package com.revature.yolp.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.yolp.dtos.requests.NewLoginRequest;
import com.revature.yolp.dtos.responses.Principal;
import com.revature.yolp.models.User;
import com.revature.yolp.services.TokenService;
import com.revature.yolp.services.UserService;
import com.revature.yolp.utils.custom_exceptions.InvalidAuthException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/* purpose of this class is to authenticate the user */
public class AuthHandler {
    /* dependency injections */
    private final UserService userService;
    private final TokenService tokenService;
    private final ObjectMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public AuthHandler(UserService userService, TokenService tokenService, ObjectMapper mapper) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.mapper = mapper;
    }

    public void authenticateUser(Context ctx) throws IOException {
        NewLoginRequest req = mapper.readValue(ctx.req.getInputStream(), NewLoginRequest.class);

        try {
            logger.info("Attempting to login...");

            Principal principal = userService.login(req);

            /* generate token from principal obj */
            String token = tokenService.generateToken(principal);

            /* set the header with the auth token */
            ctx.res.setHeader("authorization", token);

            /* return the principal obj as json */
            ctx.json(principal);

            ctx.status(202); // ACCEPTED

            logger.info("Login successful...");
        } catch (InvalidAuthException e) {
            ctx.status(401);
            ctx.json(e);
        }
    }
}
