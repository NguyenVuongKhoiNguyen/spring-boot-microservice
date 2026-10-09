package com.poly.payment.mappers;

import com.poly.payment.dtos.requests.BookingPaymentRequest;
import com.poly.payment.dtos.responses.BookingPaymentResponse;
import com.poly.payment.dtos.responses.PaymentStatusResponse;
import com.poly.payment.dtos.responses.QrPaymentResponse;
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

  @Mapping(target = "paymentId", source = "entity.id")
  @Mapping(target = "paymentCode", source = "entity.paymentCode")
  @Mapping(target = "amount", source = "entity.amount")
  @Mapping(target = "paymentMethod", source = "entity.paymentMethod")
  @Mapping(target = "status", source = "entity.status")
  @Mapping(target = "transferContent", source = "entity.paymentCode")
  @Mapping(target = "qrUrl", source = "qrUrl")
  @Mapping(target = "bankId", source = "bankId")
  @Mapping(target = "accountNumber", source = "accountNumber")
  @Mapping(target = "accountName", source = "accountName")
  QrPaymentResponse toQrResponse(
      BookingPayment entity, String qrUrl, String bankId, String accountNumber, String accountName);

  PaymentStatusResponse toStatusResponse(BookingPayment entity);
}
