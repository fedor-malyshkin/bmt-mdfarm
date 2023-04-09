package test.bmt.mdfarm.model.dto;

import test.bmt.mdfarm.model.MobileDeviceModelCode;

import java.time.LocalDateTime;
import java.util.Optional;

public record MobileDeviceDto(String id,
                              MobileDeviceModelCode modelCode,
                              String model,
                              boolean booked,
                              Optional<LocalDateTime> bookTime,
                              Optional<UserDto> bookingOwner,
                              boolean synced,
                              String technology,
                              String bands2g,
                              String bands3g,
                              String bands4g) {
}
