package com.vtn.service;

import com.vtn.dto.request.AccountRequest;
import com.vtn.dto.request.ChangePasswordRequest;
import com.vtn.entity.AccountEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.utils.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public BaseResponse getAllAccounts(AccountRequest request) {
        List<AccountEntity> accounts;
        if (isAllParametersNull(request)) {
            accounts = accountRepository.findAll();
        } else {
            accounts = accountRepository.getAllByCondition(
                    request.getUsername(),
                    request.getRole(),
                    request.getActive(),
                    request.getCreatedBy(),
                    request.getUpdatedBy()
            );
        }
        return new BaseResponse(200, accounts, "Get all accounts successful", null,null);
    }

    public BaseResponse getAllAccountsByRole(AccountRequest request) {
        List<AccountEntity> accounts = accountRepository.findAccountsNotUsedByEmployee(request.getRole());
        return new BaseResponse(200, accounts, "Get all accounts by role successful", null,null);
    }

    public BaseResponse updateAccount(AccountRequest request) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AccountEntity account = accountRepository.findByAccountId(request.getAccountId());

        BaseResponse validationError = validateAccountExists(account);
        if (validationError != null) {
            return validationError;
        }

        account.setActive(request.getActive());
        account.setUpdatedBy(info.getUsername());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return new BaseResponse(200, "Account updated successful", null, null,null);
    }

    public BaseResponse changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        BaseResponse authError = validateAuthenticated(authentication);
        if (authError != null) {
            return authError;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        AccountEntity account = accountRepository.findByUsername(userDetails.getUsername()).orElse(null);

        BaseResponse accountError = validateAccountExists(account);
        if (accountError != null) {
            return accountError;
        }

        BaseResponse passwordError = validateOldPassword(request, account);
        if (passwordError != null) {
            return passwordError;
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setUpdatedBy(userDetails.getUsername());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        return new BaseResponse(200, "Change password successful", null, null, null);
    }

    // ------------------ validate ------------------
    private boolean isAllParametersNull(AccountRequest request) {
        return ((request.getUsername() == null) &&
                (request.getActive() == null ) &&
                (request.getRole() == null) &&
                (request.getCreatedBy() == null) &&
                (request.getUpdatedBy() == null));
    }

    private BaseResponse validateAccountExists(AccountEntity account) {
        if (account == null) {
            return new BaseResponse(404, "Account not found", null, null, null);
        }
        return null;
    }

    private BaseResponse validateAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return new BaseResponse(401, "Bạn chưa đăng nhập", null, null, null);
        }
        return null;
    }

    private BaseResponse validateOldPassword(ChangePasswordRequest request, AccountEntity account) {
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            return new BaseResponse(400, "Mật khẩu hiện tại không đúng", null, null, null);
        }
        return null;
    }
}
