insert into customers (id, email, name, created_at) values
    ('11111111-1111-1111-1111-111111111111', 'alice@example.com', 'Alice Adams', '2024-01-01T10:00:00Z'),
    ('22222222-2222-2222-2222-222222222222', 'bob@example.com', 'Bob Brown', '2024-01-02T11:00:00Z');

insert into orders (id, customer_id, status, total_amount, created_at) values
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'NEW', 120.50, '2024-01-03T12:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 'PAID', 55.00, '2024-01-04T13:00:00Z'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222', 'SHIPPED', 200.00, '2024-01-05T14:00:00Z');
