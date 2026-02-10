package com.example.demographql.graphql;

import com.example.demographql.domain.Customer;
import com.example.demographql.persistence.PageResult;
import java.util.List;

public record CustomerConnection(List<Customer> items, PageInfo pageInfo) {

    public static CustomerConnection from(PageResult<Customer> result) {
        return new CustomerConnection(result.items(), PageInfo.from(result));
    }
}
