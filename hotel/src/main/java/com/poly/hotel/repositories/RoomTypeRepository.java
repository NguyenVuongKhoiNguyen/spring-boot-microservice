package com.poly.hotel.repositories;

import com.poly.hotel.models.RoomType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository
    extends JpaRepository<RoomType, Long>, JpaSpecificationExecutor<RoomType> {
  List<RoomType> findByHotelIdAndDelIfFalse(Long hotelId);
}
