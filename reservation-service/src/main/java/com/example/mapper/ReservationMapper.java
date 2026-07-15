package com.example.mapper;

import com.example.dto.ReservationDetailsDTO;
import com.example.dto.ReservationRequestDTO;
import com.example.dto.ReservationResponseDTO;
import com.example.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);

    ReservationDetailsDTO toDetailsDto(Reservation reservation);

    @Mapping(target = "reservationId", source = "idReservation")
    @Mapping(target = "message", ignore = true)
    ReservationResponseDTO toResponseDto(Reservation reservation);

    @Mapping(target = "idReservation", ignore = true)
    @Mapping(target = "dateCreate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Reservation toEntity(ReservationRequestDTO requestDto);

    List<ReservationDetailsDTO> toDetailsDtoList(List<Reservation> reservations);

    default ReservationResponseDTO createResponseDto(String status, String reservationId, String message) {
        return new ReservationResponseDTO(status, reservationId, message);
    }
}
