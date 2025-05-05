INSERT INTO payments (id, status, booking_id, session_url, session_id, is_deleted, amount_to_pay)
VALUES (23, 'PENDING', 36, 'http://example.com/session123', 'session123', false, 270.00),
       (40, 'PAID', 58, 'http://example.com/session456', 'session456', false, 210.00),
       (42, 'EXPIRED', 56, 'http://localhost:8081/payments/success?session_id=session_123', 'session7890', false, 210.00);