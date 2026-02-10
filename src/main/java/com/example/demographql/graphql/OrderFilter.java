package com.example.demographql.graphql;

import com.example.demographql.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderFilter(
        List<OrderStatus> statusIn,
        BigDecimal minTotal,
        BigDecimal maxTotal,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo
) {
}
