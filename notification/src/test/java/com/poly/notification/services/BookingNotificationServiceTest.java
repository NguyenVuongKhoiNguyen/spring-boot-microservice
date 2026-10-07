package com.poly.notification.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.notification.dtos.requests.BookingNotificationRequest;
import com.poly.notification.dtos.responses.BookingNotificationResponse;
import com.poly.notification.dtos.responses.PageResponse;
import com.poly.notification.exceptions.ResourceNotFoundException;
import com.poly.notification.mappers.BookingNotificationMapper;
import com.poly.notification.models.BookingNotification;
import com.poly.notification.repositories.BookingNotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookingNotificationServiceTest {

  @Mock private BookingNotificationRepository repository;
  @Mock private BookingNotificationMapper mapper;
  @InjectMocks private BookingNotificationService service;

  @Nested
  @DisplayName("CRUD")
  class CRUD {
    @Test
    void getById_whenExistsAndNotDeleted_returnsResponse() {
      BookingNotification entity = BookingNotification.builder().id(1L).delIf(false).build();
      BookingNotificationResponse response =
          new BookingNotificationResponse(
              1L, 100L, "Title", "Message", "GENERAL", null, false, null);
      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(mapper.toResponse(entity)).thenReturn(response);

      BookingNotificationResponse result = service.getById(1L);

      assertThat(result).isEqualTo(response);
    }

    @Test
    void getById_whenDeleted_throwsException() {
      BookingNotification entity = BookingNotification.builder().id(1L).delIf(true).build();
      when(repository.findById(1L)).thenReturn(Optional.of(entity));

      assertThatThrownBy(() -> service.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_whenNotFound_throwsException() {
      when(repository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenValidRequest_returnsResponse() {
      BookingNotificationRequest request =
          new BookingNotificationRequest(100L, "Title", "Message", "GENERAL", null);
      BookingNotification entity = BookingNotification.builder().build();
      BookingNotification savedEntity =
          BookingNotification.builder().id(1L).delIf(false).isRead(false).build();
      BookingNotificationResponse response =
          new BookingNotificationResponse(
              1L, 100L, "Title", "Message", "GENERAL", null, false, null);

      when(mapper.toEntity(request)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(savedEntity);
      when(mapper.toResponse(savedEntity)).thenReturn(response);

      BookingNotificationResponse result = service.create(request);

      assertThat(result).isEqualTo(response);
      assertThat(entity.getDelIf()).isFalse();
      assertThat(entity.getIsRead()).isFalse();
    }

    @Test
    void update_whenExistsAndNotDeleted_updatesAndReturns() {
      BookingNotificationRequest request =
          new BookingNotificationRequest(100L, "Title", "Message", "GENERAL", null);
      BookingNotification entity = BookingNotification.builder().id(1L).delIf(false).build();
      BookingNotification savedEntity = BookingNotification.builder().id(1L).delIf(false).build();
      BookingNotificationResponse response =
          new BookingNotificationResponse(
              1L, 100L, "Title", "Message", "GENERAL", null, false, null);

      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(repository.save(entity)).thenReturn(savedEntity);
      when(mapper.toResponse(savedEntity)).thenReturn(response);

      BookingNotificationResponse result = service.update(1L, request);

      verify(mapper).updateEntity(request, entity);
      assertThat(result).isEqualTo(response);
    }

    @Test
    void update_whenDeleted_throwsException() {
      BookingNotificationRequest request =
          new BookingNotificationRequest(100L, "Title", "Message", "GENERAL", null);
      BookingNotification entity = BookingNotification.builder().id(1L).delIf(true).build();
      when(repository.findById(1L)).thenReturn(Optional.of(entity));

      assertThatThrownBy(() -> service.update(1L, request))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExistsAndNotDeleted_setsDelIfToTrueAndSaves() {
      BookingNotification entity = BookingNotification.builder().id(1L).delIf(false).build();
      when(repository.findById(1L)).thenReturn(Optional.of(entity));

      service.delete(1L);

      assertThat(entity.getDelIf()).isTrue();
      verify(repository).save(entity);
    }

    @Test
    void delete_whenDeleted_throwsException() {
      BookingNotification entity = BookingNotification.builder().id(1L).delIf(true).build();
      when(repository.findById(1L)).thenReturn(Optional.of(entity));

      assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {
    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      BookingNotification notification = BookingNotification.builder().id(1L).userId(100L).build();
      BookingNotificationResponse response =
          new BookingNotificationResponse(1L, 100L, "Title", "Message", "INFO", null, false, null);

      Page<BookingNotification> page = new PageImpl<>(List.of(notification));
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(mapper.toResponse(notification)).thenReturn(response);

      PageResponse<BookingNotificationResponse> result =
          service.filterAndPaginate(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().id()).isEqualTo(1L);
      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmpty() {
      Page<BookingNotification> page = new PageImpl<>(List.of());
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      PageResponse<BookingNotificationResponse> result =
          service.filterAndPaginate(
              1L,
              100L,
              "Title",
              "Message",
              "INFO",
              200L,
              false,
              Instant.now(),
              Instant.now(),
              Instant.now(),
              Instant.now(),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).isEmpty();
    }
  }
}
