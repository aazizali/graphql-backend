package com.example.demographql.graphql;

public record OrderSort(OrderSortField field, SortDirection direction) {

    public static OrderSort defaultSort() {
        return new OrderSort(OrderSortField.CREATED_AT, SortDirection.DESC);
    }
}
