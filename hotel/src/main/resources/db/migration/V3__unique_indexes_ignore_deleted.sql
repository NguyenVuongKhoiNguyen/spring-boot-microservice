ALTER TABLE room_types DROP CONSTRAINT uq_hotel_room_type_name;
ALTER TABLE rooms DROP CONSTRAINT uq_hotel_room_number;

CREATE UNIQUE INDEX uq_hotel_room_type_name ON room_types (hotel_id, name) WHERE del_if = FALSE;
CREATE UNIQUE INDEX uq_hotel_room_number ON rooms (hotel_id, room_number) WHERE del_if = FALSE;
