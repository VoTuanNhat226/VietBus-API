package com.vtn.service;

import com.vtn.entity.AccountEntity;
import com.vtn.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
}
