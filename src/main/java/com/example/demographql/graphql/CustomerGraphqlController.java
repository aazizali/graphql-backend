package com.example.demographql.graphql;

import com.example.demographql.domain.Customer;
import com.example.demographql.domain.Order;
import com.example.demographql.exception.NotFoundException;
import com.example.demographql.persistence.CustomerRepository;
import com.example.demographql.persistence.OrderRepository;
import com.example.demographql.persistence.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class CustomerGraphqlController {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerGraphqlController(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @QueryMapping
    public Customer customer(@Argument @NotNull UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    @QueryMapping
    public CustomerConnection customers(
            @Argument @Valid CustomerFilter filter,
            @Argument CustomerSort sort,
            @Argument @Valid OffsetPage page
    ) {
        OffsetPage effectivePage = OffsetPage.defaultPageIfNull(page);
        PageResult<Customer> result = customerRepository.findCustomers(filter, sort, effectivePage);
        return CustomerConnection.from(result);
    }

    @QueryMapping
    public OrderConnection orders(
            @Argument @NotNull UUID customerId,
            @Argument @Valid OrderFilter filter,
            @Argument OrderSort sort,
            @Argument @Valid OffsetPage page
    ) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer not found: " + customerId);
        }
        OffsetPage effectivePage = OffsetPage.defaultPageIfNull(page);
        PageResult<Order> result = orderRepository.findOrders(customerId, filter, sort, effectivePage);
        return OrderConnection.from(result);
    }

    @MutationMapping
    public Customer createCustomer(@Argument @Valid CreateCustomerInput input) {
        Customer customer = new Customer();
        customer.setEmail(input.email());
        customer.setName(input.name());
        return customerRepository.save(customer);
    }

    @MutationMapping
    public Order createOrder(@Argument @Valid CreateOrderInput input) {
        if (!customerRepository.existsById(input.customerId())) {
            throw new NotFoundException("Customer not found: " + input.customerId());
        }
        Order order = new Order();
        order.setCustomerId(input.customerId());
        order.setStatus(input.status());
        order.setTotalAmount(input.totalAmount());
        return orderRepository.save(order);
    }
}
