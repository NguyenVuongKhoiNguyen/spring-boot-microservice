package com.poly.payment.mappers;

import com.poly.payment.dtos.requests.PaymentRequest;
import com.poly.payment.dtos.responses.PaymentResponse;
import com.poly.payment.models.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

  PaymentResponse toResponse(Payment entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Payment toEntity(PaymentRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(PaymentRequest request, @MappingTarget Payment entity);
}
