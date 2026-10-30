package com.construction.construction_management.dto;

import com.construction.construction_management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Public project-team identity: name and role only, never contact details. */
@Getter
@AllArgsConstructor
public class SitePartyResponse {
    int id;
    String name;
    String roleName;

    public static SitePartyResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new SitePartyResponse(user.getId(), user.getName(), user.getRole().getName().name());
    }
}
