package com.example.demographql.persistence;

import com.example.demographql.domain.Order;
import com.example.demographql.dto.OffsetPage;
import com.example.demographql.dto.OrderFilter;
import com.example.demographql.dto.OrderSort;
import java.util.UUID;

public interface OrderRepositoryCustom {

    PageResult<Order> findOrders(UUID customerId, OrderFilter filter, OrderSort sort, OffsetPage page);
}
