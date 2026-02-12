package com.example.demographql.persistence;

import com.example.demographql.domain.Customer;
import com.example.demographql.domain.QCustomer;
import com.example.demographql.dto.CustomerFilter;
import com.example.demographql.dto.CustomerSort;
import com.example.demographql.dto.CustomerSortField;
import com.example.demographql.dto.OffsetPage;
import com.example.demographql.dto.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CustomerRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<Customer> findCustomers(CustomerFilter filter, CustomerSort sort, OffsetPage page) {
        QCustomer customer = QCustomer.customer;
        BooleanBuilder where = new BooleanBuilder();

        if (filter != null) {
            if (filter.emailContains() != null && !filter.emailContains().isBlank()) {
                where.and(customer.email.containsIgnoreCase(filter.emailContains().trim()));
            }
            if (filter.nameContains() != null && !filter.nameContains().isBlank()) {
                where.and(customer.name.containsIgnoreCase(filter.nameContains().trim()));
            }
            if (filter.createdFrom() != null) {
                where.and(customer.createdAt.goe(filter.createdFrom()));
            }
            if (filter.createdTo() != null) {
                where.and(customer.createdAt.loe(filter.createdTo()));
            }
        }

        OffsetPage effectivePage = OffsetPage.defaultPageIfNull(page);
        OrderSpecifier<?> primaryOrder = customerOrderSpecifier(sort, customer);

        List<Customer> items = queryFactory
                .selectFrom(customer)
                .where(where)
                .orderBy(primaryOrder, customer.id.asc())
                .offset(effectivePage.offsetValue())
                .limit(effectivePage.limitValue())
                .fetch();

        Long total = queryFactory
                .select(customer.count())
                .from(customer)
                .where(where)
                .fetchOne();

        long totalValue = total != null ? total : 0L;
        return new PageResult<>(items, totalValue, effectivePage.offsetValue(), effectivePage.limitValue());
    }

    private OrderSpecifier<?> customerOrderSpecifier(CustomerSort sort, QCustomer customer) {
        CustomerSortField field = sort != null && sort.field() != null
                ? sort.field()
                : CustomerSortField.CREATED_AT;
        SortDirection direction = sort != null && sort.direction() != null
                ? sort.direction()
                : SortDirection.DESC;
        com.querydsl.core.types.Order order = direction == SortDirection.ASC
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        return switch (field) {
            case EMAIL -> new OrderSpecifier<>(order, customer.email);
            case NAME -> new OrderSpecifier<>(order, customer.name);
            case CREATED_AT -> new OrderSpecifier<>(order, customer.createdAt);
        };
    }
}
