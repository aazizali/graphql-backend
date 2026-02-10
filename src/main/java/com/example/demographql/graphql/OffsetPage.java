package com.example.demographql.graphql;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OffsetPage(@Min(0) Integer offset, @Min(1) @Max(100) Integer limit) {

    public static OffsetPage defaultPage() {
        return new OffsetPage(0, 20);
    }

    public static OffsetPage defaultPageIfNull(OffsetPage page) {
        return page != null ? page : defaultPage();
    }

    public int offsetValue() {
        return offset != null ? offset : 0;
    }

    public int limitValue() {
        return limit != null ? limit : 20;
    }
}
