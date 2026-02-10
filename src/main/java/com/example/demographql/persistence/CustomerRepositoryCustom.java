package com.example.demographql.persistence;

import com.example.demographql.domain.Customer;
import com.example.demographql.graphql.CustomerFilter;
import com.example.demographql.graphql.CustomerSort;
import com.example.demographql.graphql.OffsetPage;

public interface CustomerRepositoryCustom {

    PageResult<Customer> findCustomers(CustomerFilter filter, CustomerSort sort, OffsetPage page);
}
