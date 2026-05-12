package com.vtn.service;

import com.vtn.dto.request.AccountRequest;
import com.vtn.entity.AccountEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.utils.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return accountRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );
    }

    private boolean isAllParametersNull(AccountRequest request) {
        return ((request.getUsername() == null) &&
                (request.getActive() == null ) &&
                (request.getRole() == null) &&
                (request.getCreatedBy() == null) &&
                (request.getUpdatedBy() == null));
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
        if (account == null ) {
            return new BaseResponse(404, "Account not found", null, null,null);
        } else {
            account.setActive(request.getActive());
            account.setUpdatedBy(info.getUsername());
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
            return new BaseResponse(200, "Account updated successful", null, null,null);
        }
    }
}
