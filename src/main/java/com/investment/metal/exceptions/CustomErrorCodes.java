package com.investment.metal.exceptions;

import org.apache.http.HttpStatus;

public interface CustomErrorCodes extends HttpStatus {

    int USER_ALREADY_EXISTS = 1000;

}
