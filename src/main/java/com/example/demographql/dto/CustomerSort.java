package com.example.demographql.dto;

public record CustomerSort(CustomerSortField field, SortDirection direction) {

    public static CustomerSort defaultSort() {
        return new CustomerSort(CustomerSortField.CREATED_AT, SortDirection.DESC);
    }
}
