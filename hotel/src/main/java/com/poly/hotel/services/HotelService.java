package com.poly.hotel.services;

import com.poly.hotel.dtos.requests.HotelRequest;
import com.poly.hotel.dtos.responses.HotelResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.exceptions.ResourceConflictException;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.mappers.HotelMapper;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.HotelImage;
import com.poly.hotel.models.Room;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.HotelImageRepository;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.HotelSpecification;
import com.poly.hotel.repositories.RoomRepository;
import com.poly.hotel.repositories.RoomTypeRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelService {
  private final HotelRepository repository;
  private final HotelMapper mapper;
  private final BookingRepository bookingRepository;
  private final HotelImageRepository hotelImageRepository;
  private final RoomTypeRepository roomTypeRepository;
  private final RoomRepository roomRepository;

  @Transactional(readOnly = true)
  public PageResponse<HotelResponse> filterAndPaginate(
      Long id,
      String name,
      String description,
      String address,
      String city,
      String country,
      String phone,
      String email,
      Integer starRatingFrom,
      Integer starRatingTo,
      LocalTime checkInTimeFrom,
      LocalTime checkInTimeTo,
      LocalTime checkOutTimeFrom,
      LocalTime checkOutTimeTo,
      Boolean active,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<Hotel> page =
        repository.findAll(
            HotelSpecification.filter(
                id,
                name,
                description,
                address,
                city,
                country,
                phone,
                email,
                starRatingFrom,
                starRatingTo,
                checkInTimeFrom,
                checkInTimeTo,
                checkOutTimeFrom,
                checkOutTimeTo,
                active,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public HotelResponse getById(Long id) {
    Hotel hotel =
        repository
            .findById(id)
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    return mapper.toResponse(hotel);
  }

  @Transactional
  public HotelResponse create(HotelRequest request) {
    Hotel hotel = mapper.toEntity(request);
    hotel.setDelIf(false);
    hotel.setCreatedAt(Instant.now());
    hotel.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(hotel));
  }

  @Transactional
  public HotelResponse update(Long id, HotelRequest request) {
    Hotel hotel =
        repository
            .findById(id)
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    mapper.updateEntity(request, hotel);
    hotel.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(hotel));
  }

  @Transactional
  public void delete(Long id) {
    Hotel hotel =
        repository
            .findById(id)
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    if (bookingRepository.existsByHotelIdAndDelIfFalse(id)) {
      throw new ResourceConflictException("Cannot delete hotel because it has active bookings");
    }

    hotel.setDelIf(true);
    hotel.setUpdatedAt(Instant.now());
    repository.save(hotel);

    List<HotelImage> images = hotelImageRepository.findByHotelIdAndDelIfFalse(id);
    images.forEach(
        img -> {
          img.setDelIf(true);
          hotelImageRepository.save(img);
        });

    List<RoomType> roomTypes = roomTypeRepository.findByHotelIdAndDelIfFalse(id);
    roomTypes.forEach(
        rt -> {
          rt.setDelIf(true);
          rt.setUpdatedAt(Instant.now());
          roomTypeRepository.save(rt);
        });

    List<Room> rooms = roomRepository.findByHotelIdAndDelIfFalse(id);
    rooms.forEach(
        r -> {
          r.setDelIf(true);
          r.setUpdatedAt(Instant.now());
          roomRepository.save(r);
        });
  }
}
