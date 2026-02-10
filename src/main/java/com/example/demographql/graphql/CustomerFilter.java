package com.example.demographql.graphql;

import java.time.OffsetDateTime;

public record CustomerFilter(
        String emailContains,
        String nameContains,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo
) {
}
