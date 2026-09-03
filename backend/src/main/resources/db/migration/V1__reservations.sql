CREATE TABLE bookings (
 id VARCHAR(36) PRIMARY KEY, room VARCHAR(10) NOT NULL CHECK(room IN ('norte','sur')),
 booking_date VARCHAR(10) NOT NULL, start_minute INT NOT NULL CHECK(start_minute>=540 AND MOD(start_minute,30)=0),
 duration INT NOT NULL CHECK(duration IN (30,60,90)), title VARCHAR(80) NOT NULL, name VARCHAR(60) NOT NULL,
 status VARCHAR(10) NOT NULL CHECK(status IN ('CONFIRMED','CANCELLED')), CHECK(start_minute+duration<=1080)
);
CREATE INDEX idx_bookings_date ON bookings(booking_date);
CREATE TABLE slots (id VARCHAR(36) PRIMARY KEY, booking_id VARCHAR(36) NOT NULL REFERENCES bookings(id),
 room VARCHAR(10) NOT NULL, booking_date VARCHAR(10) NOT NULL, start_minute INT NOT NULL,
 CONSTRAINT uq_room_date_start UNIQUE(room,booking_date,start_minute));
CREATE INDEX idx_slots_booking ON slots(booking_id);
