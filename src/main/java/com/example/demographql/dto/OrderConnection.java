package com.example.demographql.dto;

import com.example.demographql.domain.Order;
import com.example.demographql.persistence.PageResult;
import java.util.List;

public record OrderConnection(List<Order> items, PageInfo pageInfo) {

    public static OrderConnection from(PageResult<Order> result) {
        return new OrderConnection(result.items(), PageInfo.from(result));
    }
}
