package com.capgemini.test.code.service;

import com.capgemini.test.code.clients.CheckDniResponse;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.CreateUserResponse;
import com.capgemini.test.code.dto.UserResponse;
import com.capgemini.test.code.exception.ResourceNotFoundException;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.model.Room;
import com.capgemini.test.code.model.User;
import com.capgemini.test.code.repository.RoomRepository;
import com.capgemini.test.code.repository.UserRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DniClient dniClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_OK() {
        CreateUserRequest request = new CreateUserRequest(
                "pablo",
                "pablo@email.com",
                "677998899",
                "admin",
                "23454234W"
        );

        Room room = new Room();
        room.setId(1L);
        room.setName("Sala 1");

        when(userRepository.findByEmail("pablo@email.com")).thenReturn(Optional.empty());
        when(dniClient.check(any())).thenReturn(ResponseEntity.ok(new CheckDniResponse()));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(notificationClient.sendEmail(any())).thenReturn(ResponseEntity.ok("valid email"));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        CreateUserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("pablo", savedUser.getName());
        assertEquals("pablo@email.com", savedUser.getEmail());
        assertEquals("677998899", savedUser.getPhone());
        assertEquals("23454234W", savedUser.getDni());
        assertEquals("admin", savedUser.getRole());
        assertEquals(1L, savedUser.getRoom().getId());

        verify(notificationClient, times(1)).sendEmail(any());
        verify(notificationClient, never()).sendSms(any());
    }

    @Test
    void createUser_validationException_whenNameIsTooLong() {
        CreateUserRequest request = new CreateUserRequest(
                "pablito",
                "pablo@email.com",
                "677998899",
                "admin",
                "23454234W"
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request)
        );

        assertEquals("error validation <userName>", exception.getMessage());

        verify(userRepository, never()).findByEmail(any());
        verify(dniClient, never()).check(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_validationException_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "pablo",
                "pablo@email.com",
                "677998899",
                "admin",
                "23454234W"
        );

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("pablo@email.com");

        when(userRepository.findByEmail("pablo@email.com")).thenReturn(Optional.of(existingUser));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request)
        );

        assertEquals("error validation <email>", exception.getMessage());

        verify(dniClient, never()).check(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwValidationException_whenDniIsInvalid() {
        CreateUserRequest request = new CreateUserRequest(
                "pablo",
                "pablo@email.com",
                "677998899",
                "admin",
                "99999999w"
        );

        when(userRepository.findByEmail("pablo@email.com")).thenReturn(Optional.empty());
        doThrow(mock(FeignException.Conflict.class)).when(dniClient).check(any());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request)
        );

        assertEquals("error validation <dni>", exception.getMessage());

        verify(userRepository, never()).save(any());
        verify(notificationClient, never()).sendEmail(any());
        verify(notificationClient, never()).sendSms(any());
    }

    @Test
    void createUser_sendSms_whenRoleIsSuperadmin() {
        CreateUserRequest request = new CreateUserRequest(
                "laura",
                "laura@email.com",
                "677998800",
                "superadmin",
                "23454235W"
        );

        Room room = new Room();
        room.setId(1L);
        room.setName("Sala 1");

        when(userRepository.findByEmail("laura@email.com")).thenReturn(Optional.empty());
        when(dniClient.check(any())).thenReturn(ResponseEntity.ok(new CheckDniResponse()));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(notificationClient.sendSms(any())).thenReturn(ResponseEntity.ok("valid sms"));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(20L);
            return user;
        });

        CreateUserResponse response = userService.createUser(request);

        assertEquals(20L, response.getId());
        verify(notificationClient, times(1)).sendSms(any());
        verify(notificationClient, never()).sendEmail(any());
    }

    @Test
    void getUserById_OK() {
        Room room = new Room();
        room.setId(1L);
        room.setName("Sala 1");

        User user = new User();
        user.setId(7L);
        user.setName("pablo");
        user.setEmail("pablo@email.com");
        user.setPhone("677998899");
        user.setDni("23454234W");
        user.setRole("admin");
        user.setRoom(room);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(7L);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals("pablo", response.getName());
        assertEquals("pablo@email.com", response.getEmail());
        assertEquals("677998899", response.getPhone());
        assertEquals("23454234W", response.getDni());
        assertEquals("admin", response.getRol());
        assertEquals(1L, response.getRoomId());
    }

    @Test
    void getUserById_NotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(99L)
        );

        assertEquals("user not found", exception.getMessage());
    }
}