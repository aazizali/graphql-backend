package com.example.demographql.dto;

import com.example.demographql.domain.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderInput(
        @NotNull UUID customerId,
        @NotNull OrderStatus status,
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount
) {
}
