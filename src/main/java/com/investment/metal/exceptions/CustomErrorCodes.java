package com.investment.metal.exceptions;

import org.apache.http.HttpStatus;

public interface CustomErrorCodes extends HttpStatus {

    int REGISTER_NEW_USER = 1000;

    int MAIL_SERVICE = 1001;

    int VALIDATE_ACCOUNT = 1002;

    int USER_RETRIEVE = 1003;

    int PURCHASE = 1004;
}
