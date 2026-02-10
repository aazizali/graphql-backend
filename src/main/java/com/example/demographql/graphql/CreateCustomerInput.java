package com.example.demographql.graphql;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerInput(
        @Email @NotBlank String email,
        @NotBlank String name
) {
}
