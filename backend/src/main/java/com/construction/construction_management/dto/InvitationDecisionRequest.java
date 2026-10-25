package com.construction.construction_management.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationDecisionRequest {

    @Size(max = 500, message = "Message is too long")
    String responseMessage;
}
