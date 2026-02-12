package com.example.demographql.persistence;

import com.example.demographql.domain.Order;
import com.example.demographql.domain.QOrder;
import com.example.demographql.dto.OffsetPage;
import com.example.demographql.dto.OrderFilter;
import com.example.demographql.dto.OrderSort;
import com.example.demographql.dto.OrderSortField;
import com.example.demographql.dto.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<Order> findOrders(UUID customerId, OrderFilter filter, OrderSort sort, OffsetPage page) {
        QOrder order = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        where.and(order.customerId.eq(customerId));

        if (filter != null) {
            if (filter.statusIn() != null && !filter.statusIn().isEmpty()) {
                where.and(order.status.in(filter.statusIn()));
            }
            if (filter.minTotal() != null) {
                where.and(order.totalAmount.goe(filter.minTotal()));
            }
            if (filter.maxTotal() != null) {
                where.and(order.totalAmount.loe(filter.maxTotal()));
            }
            if (filter.createdFrom() != null) {
                where.and(order.createdAt.goe(filter.createdFrom()));
            }
            if (filter.createdTo() != null) {
                where.and(order.createdAt.loe(filter.createdTo()));
            }
        }

        OffsetPage effectivePage = OffsetPage.defaultPageIfNull(page);
        OrderSpecifier<?> primaryOrder = orderOrderSpecifier(sort, order);

        List<Order> items = queryFactory
                .selectFrom(order)
                .where(where)
                .orderBy(primaryOrder, order.id.asc())
                .offset(effectivePage.offsetValue())
                .limit(effectivePage.limitValue())
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(where)
                .fetchOne();

        long totalValue = total != null ? total : 0L;
        return new PageResult<>(items, totalValue, effectivePage.offsetValue(), effectivePage.limitValue());
    }

    private OrderSpecifier<?> orderOrderSpecifier(OrderSort sort, QOrder order) {
        OrderSortField field = sort != null && sort.field() != null
                ? sort.field()
                : OrderSortField.CREATED_AT;
        SortDirection direction = sort != null && sort.direction() != null
                ? sort.direction()
                : SortDirection.DESC;
        com.querydsl.core.types.Order directionValue = direction == SortDirection.ASC
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        return switch (field) {
            case STATUS -> new OrderSpecifier<>(directionValue, order.status);
            case TOTAL_AMOUNT -> new OrderSpecifier<>(directionValue, order.totalAmount);
            case CREATED_AT -> new OrderSpecifier<>(directionValue, order.createdAt);
        };
    }
}
