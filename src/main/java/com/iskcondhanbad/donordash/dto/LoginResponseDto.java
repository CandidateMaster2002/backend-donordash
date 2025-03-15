package com.iskcondhanbad.donordash.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String userType;
    private Object userDetails;
}
