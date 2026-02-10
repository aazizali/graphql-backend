create table customers (
    id uuid primary key,
    email varchar(255) not null unique,
    name varchar(255) not null,
    created_at timestamptz not null
);

create table orders (
    id uuid primary key,
    customer_id uuid not null references customers(id),
    status varchar(20) not null,
    total_amount numeric(19, 2) not null,
    created_at timestamptz not null
);

create index idx_customers_email on customers(email);
create index idx_orders_customer_id on orders(customer_id);
create index idx_orders_created_at on orders(created_at);
