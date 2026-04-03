package com.finance.dto.request;

import com.finance.user.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private Role role;

    private Boolean active;
}
