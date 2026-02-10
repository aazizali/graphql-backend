//package com.example.demographql;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.math.BigDecimal;
//import java.util.Map;
//import java.util.UUID;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.graphql.test.tester.GraphQlTester;
//import org.springframework.graphql.test.tester.AutoConfigureGraphQlTester;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//@Testcontainers
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureGraphQlTester
//class GraphQlIntegrationTest {
//
//    private static final UUID CUSTOMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
//
//    @Container
//    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
//            DockerImageName.parse("postgres:16-alpine")
//    ).withDatabaseName("demo_graphql")
//            .withUsername("demo")
//            .withPassword("demo");
//
//    @DynamicPropertySource
//    static void registerProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired
//    private GraphQlTester graphQlTester;
//
//    @Test
//    void customersQueryReturnsPage() {
//        String document = """
//                query($page: OffsetPage) {
//                  customers(page: $page) {
//                    items {
//                      id
//                      email
//                      name
//                    }
//                    pageInfo {
//                      total
//                      offset
//                      limit
//                      hasNext
//                    }
//                  }
//                }
//                """;
//
//        GraphQlTester.Response response = graphQlTester.document(document)
//                .variable("page", Map.of("offset", 0, "limit", 2))
//                .execute();
//
//        response.path("customers.items")
//                .entityList(Map.class)
//                .hasSize(2);
//        response.path("customers.pageInfo.total")
//                .entity(Long.class)
//                .satisfies(total -> assertThat(total).isGreaterThan(0L));
//        response.path("customers.pageInfo.offset")
//                .entity(Integer.class)
//                .isEqualTo(0);
//        response.path("customers.pageInfo.limit")
//                .entity(Integer.class)
//                .isEqualTo(2);
//    }
//
//    @Test
//    void ordersQueryFiltersByCustomer() {
//        String document = """
//                query($customerId: UUID!, $page: OffsetPage) {
//                  orders(customerId: $customerId, page: $page) {
//                    items {
//                      id
//                      status
//                      totalAmount
//                    }
//                    pageInfo {
//                      total
//                    }
//                  }
//                }
//                """;
//
//        GraphQlTester.Response response = graphQlTester.document(document)
//                .variable("customerId", CUSTOMER_ID)
//                .variable("page", Map.of("offset", 0, "limit", 10))
//                .execute();
//
//        response.path("orders.items")
//                .entityList(Map.class)
//                .hasSize(2);
//        response.path("orders.pageInfo.total")
//                .entity(Long.class)
//                .isEqualTo(2L);
//    }
//
//    @Test
//    void createCustomerMutation() {
//        String document = """
//                mutation($input: CreateCustomerInput!) {
//                  createCustomer(input: $input) {
//                    id
//                    email
//                    name
//                  }
//                }
//                """;
//
//        GraphQlTester.Response response = graphQlTester.document(document)
//                .variable("input", Map.of("email", "new@example.com", "name", "New Customer"))
//                .execute();
//
//        response.path("createCustomer.id").hasValue();
//        response.path("createCustomer.email")
//                .entity(String.class)
//                .isEqualTo("new@example.com");
//    }
//
//    @Test
//    void createOrderMutation() {
//        String document = """
//                mutation($input: CreateOrderInput!) {
//                  createOrder(input: $input) {
//                    id
//                    customerId
//                    status
//                    totalAmount
//                  }
//                }
//                """;
//
//        GraphQlTester.Response response = graphQlTester.document(document)
//                .variable("input", Map.of(
//                        "customerId", CUSTOMER_ID,
//                        "status", "NEW",
//                        "totalAmount", new BigDecimal("10.00")
//                ))
//                .execute();
//
//        response.path("createOrder.id").hasValue();
//        response.path("createOrder.customerId")
//                .entity(UUID.class)
//                .isEqualTo(CUSTOMER_ID);
//        response.path("createOrder.totalAmount")
//                .entity(BigDecimal.class)
//                .satisfies(amount -> assertThat(amount).isEqualByComparingTo("10.00"));
//    }
//}
