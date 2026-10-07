package com.poly.hotel.repositories;

import com.poly.hotel.models.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
  List<Room> findByHotelIdAndDelIfFalse(Long hotelId);

  List<Room> findByRoomTypeIdAndDelIfFalse(Long roomTypeId);

  boolean existsByRoomTypeIdAndDelIfFalse(Long roomTypeId);
}
