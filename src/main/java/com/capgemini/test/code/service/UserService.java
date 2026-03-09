package com.capgemini.test.code.service;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.EmailRequest;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.clients.SmsRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Long DEFAULT_ROOM_ID = 1L;
    private static final String MESSAGE_USUARIO_GUARDADO = "usuario guardado";

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DniClient dniClient;
    private final NotificationClient notificationClient;

    public UserService(UserRepository userRepository,
                       RoomRepository roomRepository,
                       DniClient dniClient,
                       NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.dniClient = dniClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        validateRequest(request);
        validateUserDoesNotExist(request.getEmail());
        validateDni(request.getDni());

        Room room = roomRepository.findById(DEFAULT_ROOM_ID)
                .orElseThrow(() -> new ResourceNotFoundException("room not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDni(request.getDni());
        user.setRole(request.getRol());
        user.setRoom(room);

        User savedUser = userRepository.save(user);

        sendNotification(savedUser);

        return new CreateUserResponse(savedUser.getId());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        if (user.getRoom() == null || !DEFAULT_ROOM_ID.equals(user.getRoom().getId())) {
            throw new ResourceNotFoundException("user not found in room 1");
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getDni(),
                user.getRoom().getId()
        );
    }

    private void validateRequest(CreateUserRequest request) {
        validateName(request.getName());
        validateEmail(request.getEmail());
        validateRole(request.getRol());
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 6) {
            throw new ValidationException("userName");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@") || !email.contains(".")) {
            throw new ValidationException("email");
        }
    }

    private void validateRole(String rol) {
        if (rol == null || (!rol.equals("admin") && !rol.equals("superadmin"))) {
            throw new ValidationException("rol");
        }
    }

    private void validateUserDoesNotExist(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("email");
        }
    }

    private void validateDni(String dni) {
        try {
            dniClient.check(new CheckDniRequest(dni));
        } catch (FeignException.Conflict ex) {
            throw new ValidationException("dni");
        }
    }

    private void sendNotification(User user) {
        if ("admin".equals(user.getRole())) {
            notificationClient.sendEmail(
                    new EmailRequest(user.getEmail(), MESSAGE_USUARIO_GUARDADO)
            );
            return;
        }

        if ("superadmin".equals(user.getRole())) {
            notificationClient.sendSms(
                    new SmsRequest(user.getPhone(), MESSAGE_USUARIO_GUARDADO)
            );
        }
    }
}
