package com.example.demographql.graphql;

import com.example.demographql.persistence.PageResult;

public record PageInfo(long total, int offset, int limit, boolean hasNext) {

    public static PageInfo from(PageResult<?> result) {
        boolean hasNext = result.offset() + result.items().size() < result.total();
        return new PageInfo(result.total(), result.offset(), result.limit(), hasNext);
    }
}
