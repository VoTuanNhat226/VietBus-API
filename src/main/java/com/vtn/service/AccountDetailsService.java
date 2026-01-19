package com.vtn.service;

import com.vtn.dto.request.AccountRequest;
import com.vtn.entity.AccountEntity;
import com.vtn.entity.RouteEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.utils.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AccountEntity account = accountRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return account;
    }

    public BaseResponse getAllAccountsByRole(AccountRequest request) {
        try {
            List<AccountEntity> accounts = accountRepository.findAccountsNotUsedByEmployee(request.getRole());
            return new BaseResponse(200, accounts, "Get all accounts successfully", null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
