package com.poly.hotel.repositories;

import com.poly.hotel.models.HotelImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelImageRepository
    extends JpaRepository<HotelImage, Long>, JpaSpecificationExecutor<HotelImage> {
  List<HotelImage> findByHotelIdAndDelIfFalse(Long hotelId);
}
