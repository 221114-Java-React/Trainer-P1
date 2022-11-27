package com.revature.yolp.services;

import com.revature.yolp.daos.UserDAO;
import com.revature.yolp.dtos.requests.NewLoginRequest;
import com.revature.yolp.dtos.requests.NewUserRequest;
import com.revature.yolp.dtos.responses.Principal;
import com.revature.yolp.models.Role;
import com.revature.yolp.models.User;
import com.revature.yolp.utils.custom_exceptions.InvalidUserException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/*
   Mockito is a mocking framework. JAVA-based library that is used for effective unit testing of JAVA applications.

   Mocking is the act of removing external dependencies from a unit test in order to create a controlled environment around it. Typically, we mock all other classes that interact with the class that we want to test.
 */
public class UserServiceTest {
    /* dependency injection */
    private UserService sut; // SUT = system under test

    private final UserDAO mockUserDAO = Mockito.mock(UserDAO.class);

    /*
        Common JUnit annotations:
            - @Test (marks a method as a test case)
            - @Ignore (tells JUnit to skip this test case)
            - @Before (logic that runs once before every test case)
            - @After (logic that runs once after every test case)
            - @BeforeClass (logic that runs only once before all test cases)
            - @AfterClass (logic that runs only once after all test cases)

        Common Mockito terminologies:
            - stub: a stub is a fake class that comes with pre-programmed return values.
            - mocking: allows us to create a mock (copy) object of a class or an interface. We can then use the mock to stub return values for its methods and verify if they were called.
            - spying: a Spy is like a partial mock, which will track the interactions with the object like a mock.

        Still confused on spying vs mocking? https://stackoverflow.com/questions/15052984/what-is-the-difference-between-mocking-and-spying-when-using-mockito
     */

    @Before
    public void init() {
        sut = new UserService(mockUserDAO);
    }

    @After
    public void reset() {
        Mockito.reset(mockUserDAO);
    }


    /* -------------------------------- isValid ------------------------------------- */

    @Test
    public void test_isValidUsername_givenCorrectUsername() {
        // Arrange
        String validUsername = "bduong0929";

        // Act
        boolean condition = sut.isValidUsername(validUsername);

        // Assert
        assertTrue(condition);
    }

    @Test
    public void test_isValidUsername_givenUniqueUsername() {
        // Arrange
        UserService spiedSut = Mockito.spy(sut);
        String validUsername = "bduong0929";
        Mockito.when(spiedSut.isUniqueUsername(validUsername)).thenReturn(true);

        // Act
        boolean condition = spiedSut.isUniqueUsername(validUsername);

        // Assert
        assertTrue(condition);
    }

    @Test
    public void test_isValidPassword_givenCorrectPassword() {
        // Arrange
        String validPassword = "passw0rd";

        // Act
        boolean condition = sut.isValidPassword(validPassword);

        // Assert
        assertTrue(condition);
    }

    @Test
    public void test_isValidPassword_givenSamePassword() {
        // Arrange
        String validPassword1 = "passw0rd";
        String validPassword2 = "passw0rd";

        // Act
        boolean condition = sut.isSamePassword(validPassword1, validPassword2);

        // Assert
        assertTrue(condition);
    }

    @Test
    public void test_isValidGetAllUsers() {
        // Arrange
        UserService spiedSut = Mockito.spy(sut);
        List<User> stubbedUsers = new ArrayList<>();
        Mockito.when(spiedSut.getAllUsers()).thenReturn(stubbedUsers);

        // Act
        List<User> users = spiedSut.getAllUsers();

        // Assert
        assertNotNull(users);
        Mockito.verify(mockUserDAO, Mockito.times(1)).findAll();
    }

    @Test
    public void test_isValidSignup_givenCorrectUsernameAndPassword() {
        /* Arrange/Act/Assert is a testing pattern that organizes tests into three clear steps for easy maintenance. */

        /* Arrange (essentially initializing your variables) */
        UserService spiedSut = Mockito.spy(sut);
        NewUserRequest stubbedRequest = new NewUserRequest("bduong0929", "passw0rd", "passw0rd");
        Mockito.doReturn(true).when(spiedSut).isValidUsername(stubbedRequest.getUsername());
        Mockito.doReturn(true).when(spiedSut).isUniqueUsername(stubbedRequest.getUsername());
        Mockito.doReturn(true).when(spiedSut).isValidPassword(stubbedRequest.getPassword1());
        Mockito.doReturn(true).when(spiedSut).isSamePassword(stubbedRequest.getPassword1(), stubbedRequest.getPassword2());

        /* Act (calling the method you are testing) */
        User createdUser = spiedSut.signup(stubbedRequest);

        /* Assert (compare the actual to the expected) */
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals(Role.DEFAULT, createdUser.getRole());
        Mockito.verify(mockUserDAO, Mockito.times(1)).save(createdUser);
    }

    @Test
    public void test_isValidLogin_givenCorrectCredentials() {
        // Arrange
        UserService spiedSut = Mockito.spy(sut);
        String validUsername = "tester001";
        String validPassword = "passw0rd";
        NewLoginRequest stubbedRequest = new NewLoginRequest(validUsername, validPassword);
        User stubbedUser = new User(UUID.randomUUID().toString(), "tester001", "passw0rd", Role.DEFAULT);
        Mockito.when(spiedSut.isValidUsername(validUsername)).thenReturn(true);
        Mockito.when(spiedSut.isValidPassword(validPassword)).thenReturn(true);
        Mockito.when(mockUserDAO.getUserByUsernameAndPassword(validUsername, validPassword)).thenReturn(stubbedUser);

        // Act
        Principal principal = spiedSut.login(stubbedRequest);

        // Assert
        assertNotNull(principal);
        assertEquals(Role.DEFAULT, principal.getRole());
        assertNotNull(principal.getId());
        Mockito.verify(mockUserDAO, Mockito.times(1)).getUserByUsernameAndPassword(validUsername, validPassword);
    }

    /* -------------------------------- isInvalid ------------------------------------- */

    @Test(expected = InvalidUserException.class)
    public void test_isInvalidUsername_givenIncorrectUsername() {
        // Arrange
        String invalidUsername = "bduong";

        // Act
        boolean condition = sut.isValidUsername(invalidUsername);
    }

    @Test(expected = InvalidUserException.class)
    public void test_isInvalidUsername_givenEmptyUsername() {
        // Arrange
        String invalidUsername = "";

        // Act
        boolean condition = sut.isValidUsername(invalidUsername);
    }

    @Test(expected = InvalidUserException.class)
    public void test_isInvalidUsername_givenDuplicateUsername() {
        // Arrange
        UserService spiedSut = Mockito.spy(sut);
        String dupUsername = "bduong0929";
        List<String> stubbedUsernames = Arrays.asList("bduong0929", "tester001", "tester002");
        Mockito.when(mockUserDAO.findAllUsernames()).thenReturn(stubbedUsernames);

        // Act
        boolean condition = spiedSut.isUniqueUsername(dupUsername);
    }

    @Test(expected = InvalidUserException.class)
    public void test_isInvalidPassword_givenIncorrectPassword() {
        // Arrange
        String invalidPassword = "pass";

        // Act
        boolean condition = sut.isValidUsername(invalidPassword);
    }
}