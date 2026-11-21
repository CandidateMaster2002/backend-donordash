package com.iskcondhanbad.donordash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String userType;
    private Object userDetails;
    private String token;

    public LoginResponseDto(String userType, Object userDetails) {
        this.userType = userType;
        this.userDetails = userDetails;
    }
}
