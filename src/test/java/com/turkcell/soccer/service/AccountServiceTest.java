package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.AccountRequest;
import com.turkcell.soccer.dto.request.AccountUpdateRequest;
import com.turkcell.soccer.dto.response.AccountInfoResponse;
import com.turkcell.soccer.dto.response.AccountResponse;
import com.turkcell.soccer.dto.response.AccountUpdateResponse;
import com.turkcell.soccer.mapper.AccountMapper;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.repository.AccountRepository;
import com.turkcell.soccer.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private SecurityContextHolder securityContextHolder;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, roleRepository, passwordEncoder, accountMapper);
    }

    @Test
    void createAccount_whenUsernameAndEmailAreUnique_shouldReturnAccountResponse() {
        AccountRequest accountRequest = new AccountRequest("unitTest", "unitTest@example.com", "Password123!");

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        Role userRole = new Role();
        userRole.setName("USER");

        AccountResponse mockResponse = new AccountResponse();
        mockResponse.setUsername("unitTest");
        when(accountMapper.toAccountResponse(any(Account.class))).thenReturn(mockResponse);

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername(accountRequest.getUsername());
        savedAccount.setPassword("hashedPassword");

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.createAccount(accountRequest);

        assertNotNull(response);
        assertEquals("unitTest", response.getUsername());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(accountRepository, times(1)).save(any(Account.class));

    }

    @Test
    void createAccount_whenUsernameAlreadyExists_shouldReturnRuntimeException() {
        // Given
        String username = "testUsername";
        AccountRequest request = new AccountRequest();
        request.setUsername(username);
        request.setPassword("Password123!");
        request.setEmail("john.doe@example.com");


        // When
        when(accountRepository.existsByUsername(username)).thenReturn(true);

        // Then
        assertThrowsExactly(RuntimeException.class,
                () -> accountService.createAccount(request));
    }

    @Test
    void createAccount_whenEmailAlreadyExists_shouldReturnRuntimeException() {
        // Given
        String username = "testUsername";
        AccountRequest request = new AccountRequest();
        request.setUsername(username);
        request.setPassword("Password123!");
        request.setEmail("john.doe@example.com");

        // When
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Then
        assertThrowsExactly(RuntimeException.class,
                () -> accountService.createAccount(request));

    }

    @Test
    void createAccount_whenRoleIsNull_shouldReturnIllegalStateException() {
        // Given
        String username = "testUsername";
        AccountRequest request = new AccountRequest();
        request.setUsername(username);
        request.setPassword("Password123!");
        request.setEmail("john.doe@example.com");

        // When
        when(accountRepository.existsByUsername(username)).thenReturn(false);
        when(accountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(IllegalStateException.class,
                () -> accountService.createAccount(request));

    }

    @Test
    void deleteAccount_whenAccountNameExists_shouldReturnVoid() {
        // Given
        String username = "testUsername";

        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(new Account()));
        // Then
        accountService.deleteAccount(username);
        verify(accountRepository, times(1)).delete(any(Account.class));

    }

    @Test
    void deleteAccount_whenAccountNameDoesNotExists_shouldReturnUsernameNotFoundException() {
        // Given
        String username = "testUsername";
        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());
        // Then
        assertThrowsExactly(UsernameNotFoundException.class,
                () ->  accountService.deleteAccount(username));
    }

    @Test
    void getAuthentication_whenAuthenticationIsNull_shouldReturnAuthenticationCredentialsNotFoundException() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic =
                     mockStatic(SecurityContextHolder.class)) {

            SecurityContext securityContext = mock(SecurityContext.class);

            // When
            mockedStatic.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Then
            assertThrowsExactly(AuthenticationCredentialsNotFoundException.class,
                    () -> accountService.getAuthentication());
        }
    }

    @Test
    void getAuthentication_whenAuthenticationIsNotAuthenticated_shouldReturnAuthenticationCredentialsNotFoundException() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic =
                     mockStatic(SecurityContextHolder.class)) {

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            // When
            mockedStatic.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // Then
            assertThrowsExactly(AuthenticationCredentialsNotFoundException.class,
                    () -> accountService.getAuthentication());
        }
    }

    @Test
    void getAuthentication_whenAuthenticationIsAuthenticatedAndNotNull_shouldReturnAuthentication() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic =
                     mockStatic(SecurityContextHolder.class)) {

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            // When
            mockedStatic.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // Then
            assertEquals(authentication, accountService.getAuthentication());
            verify(securityContext, times(1)).getAuthentication();
        }
    }

    @Test
    void getAccount_whenAuthenticationAndUsernameExists_shouldReturnAccount() {

        try (MockedStatic<SecurityContextHolder> mockedStatic =
                     mockStatic(SecurityContextHolder.class)) {

            // Given
            String username = "testUsername";
            Account account = new Account();
            account.setUsername(username);
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            // When
            mockedStatic.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(username);
            when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));

            // Then
            assertEquals(username, accountService.getAccount().getUsername());
            verify(securityContext, times(1)).getAuthentication();
            verify(accountRepository, times(1)).findByUsername(username);
        }

    }

    @Test
    void authenticate_whenPasswordIsWrong_shouldThrowBadCredentialsException() {
        // Given
        String username = "testUsername";
        String password = "Password123!";
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);

        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Then
        assertThrowsExactly(BadCredentialsException.class,
                () -> accountService.authenticate(username, password));
        verify(accountRepository, times(1)).findByUsername(username);

    }

    @Test
    void authenticate_whenPasswordAndUsernameIsCorrect_shouldReturnAccount() {
        // Given
        String username = "testUsername";
        String password = "Password123!";
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);

        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Then
        assertEquals(account, accountService.authenticate(username, password));
        verify(accountRepository, times(1)).findByUsername(username);

    }

    @Test
    void updateAccount_whenEmailAndPasswordIsNotNull_shouldReturnAccountUpdateResponse() {
        // Given
        String username = "testUsername";
        String password = "Password123!";
        String email = "testEmail@tc.com";
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        AccountUpdateResponse accountUpdateResponse = new AccountUpdateResponse(1L, username, email);
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setPassword(password);
        request.setEmail(email);

        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toAccountUpdateResponse(any(Account.class))).thenReturn(accountUpdateResponse);

        // Then
        assertEquals(accountUpdateResponse, accountService.updateAccount(request, username));
        verify(accountRepository, times(1)).findByUsername(username);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void updateAccount_whenEmailIsNull_shouldReturnAccountUpdateResponse() {
        // Given
        String username = "testUsername";
        String password = "Password123!";
        String email = null;
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        AccountUpdateResponse accountUpdateResponse = new AccountUpdateResponse(1L, username, email);
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setPassword(password);
        request.setEmail(email);

        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toAccountUpdateResponse(any(Account.class))).thenReturn(accountUpdateResponse);

        // Then
        assertEquals(accountUpdateResponse, accountService.updateAccount(request, username));
        verify(accountRepository, times(1)).findByUsername(username);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void updateAccount_whenPasswordIsNull_shouldReturnAccountUpdateResponse() {
        // Given
        String username = "testUsername";
        String password = null;
        String email = "testEmail@tc.com";
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        AccountUpdateResponse accountUpdateResponse = new AccountUpdateResponse(1L, username, email);
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setPassword(password);
        request.setEmail(email);

        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toAccountUpdateResponse(any(Account.class))).thenReturn(accountUpdateResponse);

        // Then
        assertEquals(accountUpdateResponse, accountService.updateAccount(request, username));
        verify(accountRepository, times(1)).findByUsername(username);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void getAccountInfo_whenGivenUsername_shouldReturnAccountInfoResponse() {
        // Given
        String username = "unitTest";
        Account account = new Account();
        account.setUsername(username);
        AccountInfoResponse mockResponse = new AccountInfoResponse(1L, username, "asdsa", LocalDateTime.now());
        // When
        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountInfoResponse(any(Account.class))).thenReturn(mockResponse);
        // Then
        AccountInfoResponse response = accountService.getAccountInfo(username);

        assertNotNull(response);
        assertEquals("unitTest", response.getUsername());
        verify(accountRepository, times(1)).findByUsername(username);
        verify(accountMapper, times(1)).toAccountInfoResponse(any(Account.class));

    }
}