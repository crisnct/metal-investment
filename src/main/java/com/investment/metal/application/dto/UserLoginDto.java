package com.investment.metal.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login response.
 * Contains authentication token and user information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto {
    
    private String token;
    private String username;
    private String email;
}
