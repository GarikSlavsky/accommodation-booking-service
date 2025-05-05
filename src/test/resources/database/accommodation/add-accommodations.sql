INSERT INTO accommodations (id, type, location, size, daily_rate, availability, is_deleted)
VALUES (15, 'APARTMENT', 'New York', 'Standard', 30.00, 10, false),
       (18, 'CONDO', 'Miami', 'Standard', 140.00, 10, false);

INSERT INTO accommodation_amenities (accommodation_id, amenities)
VALUES (15, 'WiFi'),
       (15, 'Pool'),
       (18, 'Dining'),
       (18, 'Parking');
