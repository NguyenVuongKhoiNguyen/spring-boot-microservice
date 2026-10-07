package com.poly.payment.mappers;

import com.poly.payment.dtos.requests.BookingPaymentRequest;
import com.poly.payment.dtos.responses.BookingPaymentResponse;
import com.poly.payment.models.BookingPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingPaymentMapper {

  BookingPaymentResponse toResponse(BookingPayment entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  BookingPayment toEntity(BookingPaymentRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(BookingPaymentRequest request, @MappingTarget BookingPayment entity);
}
