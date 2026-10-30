package com.construction.construction_management.dto;

import com.construction.construction_management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Minimal public details shown when choosing a contractor or labourer. No email, no login data. */
@Getter
@Setter
@AllArgsConstructor
public class DirectoryUserResponse {
    int id;
    String name;
    String phone;
    String address;

    public static DirectoryUserResponse from(User u) {
        return new DirectoryUserResponse(u.getId(), u.getName(), u.getPhone(), u.getAddress());
    }
}
