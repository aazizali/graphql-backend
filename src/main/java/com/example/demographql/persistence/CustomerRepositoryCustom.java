package com.example.demographql.persistence;

import com.example.demographql.domain.Customer;
import com.example.demographql.dto.CustomerFilter;
import com.example.demographql.dto.CustomerSort;
import com.example.demographql.dto.OffsetPage;

public interface CustomerRepositoryCustom {

    PageResult<Customer> findCustomers(CustomerFilter filter, CustomerSort sort, OffsetPage page);
}
