package com.construction.construction_management.dto;

import com.construction.construction_management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** A party shown on an invitation. Contact details appear only after the invitation is accepted. */
@Getter
@AllArgsConstructor
public class InvitationPartyResponse {
    int id;
    String name;

    public static InvitationPartyResponse from(User user) {
        return new InvitationPartyResponse(user.getId(), user.getName());
    }
}
