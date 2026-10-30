package com.construction.construction_management.dto;

import com.construction.construction_management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    int id;
    String name;
    String email;
    String phone;
    String address;
    String roleName;
    boolean phoneVerified;
    boolean emailVerified;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().getName().toString(),
                user.isPhoneVerified(),
                user.isEmailVerified()
        );
    }
}
