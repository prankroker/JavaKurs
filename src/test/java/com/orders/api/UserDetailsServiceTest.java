package com.orders.api;

import com.orders.api.configuration.security.UserAdapter;
import com.orders.api.dto.request.RegistrationRequest;
import com.orders.api.model.User;
import com.orders.api.repository.UserRepository;
import com.orders.api.service.UserDetailsServiceImp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserDetailsServiceImp userDetailsService;

    @Test
    @DisplayName("Test for registration")
    void testRegistration() {
        ResponseEntity<String> expect = new ResponseEntity<>("Successfully registered, your email is your username",
                HttpStatus.OK);
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "name",
                "email@gmail.com",
                "1234",
                "USER"
        );

        when(userRepository.findUserByEmail(registrationRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThat(userDetailsService.register(registrationRequest)).isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for registration, already registered")
    void testRegistration_AlreadyRegistered() {
        ResponseEntity<String> expect = new ResponseEntity<>("Such a user already exists!",
                HttpStatus.BAD_REQUEST);
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "name",
                "email@gmail.com",
                "1234",
                "USER"
        );

        when(userRepository.findUserByEmail(registrationRequest.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThat(userDetailsService.register(registrationRequest)).isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for registration, wrong role")
    void testRegistration_WrongRole() {
        ResponseEntity<String> expect = new ResponseEntity<>("Wrong role provided",
                HttpStatus.BAD_REQUEST);
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "name",
                "email@gmail.com",
                "1234",
                "megauser"
        );

        when(userRepository.findUserByEmail(registrationRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThat(userDetailsService.register(registrationRequest)).isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for authentication")
    void testAuthentication() {
        String email = "email@gmail.com";
        User user = new User();
        UserAdapter expect = new UserAdapter(user);

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.of(user));

        assertThat(userDetailsService.loadUserByUsername(email)).isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for authentication, user not found")
    void testAuthentication_NoUser() {
        String email = "email@gmail.com";

        when(userRepository.findUserByEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Not found!");
    }
}
