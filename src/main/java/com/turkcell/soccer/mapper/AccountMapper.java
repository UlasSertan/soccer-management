package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.response.AccountInfoResponse;
import com.turkcell.soccer.dto.response.AccountResponse;
import com.turkcell.soccer.dto.response.AccountUpdateResponse;
import com.turkcell.soccer.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountInfoResponse toAccountInfoResponse(Account account);
    AccountUpdateResponse toAccountUpdateResponse(Account account);
    AccountResponse toAccountResponse(Account account);

}
