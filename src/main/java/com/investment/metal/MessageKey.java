package com.investment.metal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public enum MessageKey {

    //There user {0} is already registered in the database
    ALREADY_EXISTING_USER(1000),

    //Invalid request. {0}
    INVALID_REQUEST(1002),

    //Password doesn't match!
    PASSWORD_DO_NOT_MATCH(1003),

    //You can not sell more than you have in your account! Total amount of {0} that you have is {1}
    SELL_MORE_THAN_YOU_HAVE(1004),

    //Token not found
    WRONG_TOKEN(1005),

    //This account is banned until {0}. Reason: {1}
    BANNED_ACCOUNT_UNTIL(1006),

    //The username {0} doesn't exists in the database.
    INEXISTING_USER(1007),

    //The user id {0} doesn't exists in the database.
    INEXISTING_USER_ID(1008),

    //Failed to login for {0}.
    LOGIN_FAILED(1009),

    //Wrong code. Your account has been banned for 24h.
    WRONG_CODE_ACCOUNT_BANNED(1010),

    //Login/validation failed. Please try again. Number attempts: {0}.
    FAILED_LOGIN_VALIDATION(1011),

    //User is not registered.
    USER_NOT_REGISTERED(1012),

    //The user is not logged in!
    USER_NOT_LOGIN(1013),

    //The token expired! You need to validate your account.
    EXPIRED_TOKEN(1014),

    //The account needs to be validated first!
    NEEDS_VALIDATION(1015),

    //Currency not found in the database: {0}
    INEXISTING_CURRENCY(1016),

    //There user {0} is already registered in the database
    ALREADY_EXISTING_EMAIL_ADDRESS(1017),

    //The ip {0} is banned for this user. Reason: {1}
    BANED_IP(1018),

    //Fail to send email to {0}
    FAIL_TO_SEND_EMAIL(1019);

    static {
        Set<Integer> collection = new HashSet<>();
        for (MessageKey key : MessageKey.values()) {
            if (collection.contains(key.code)) {
                throw new RuntimeException("Duplicate codes in MessageKey: " + key.code);
            }
            collection.add(key.code);
        }
    }

    @Getter
    final int code;

}
