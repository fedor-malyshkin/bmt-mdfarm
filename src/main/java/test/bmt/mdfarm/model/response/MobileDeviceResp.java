package test.bmt.mdfarm.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import test.bmt.mdfarm.model.MobileDeviceModelCode;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MobileDeviceResp(
        String error,
        String id,
        @JsonProperty("model_code")
        MobileDeviceModelCode modelCode,
        String model,
        boolean booked,
        @JsonProperty("book_time")
        String bookTime,
        @JsonProperty("booking_owner")
        String bookingOwner,
        String technology,
        @JsonProperty("bands_2g")
        String bands2g,
        @JsonProperty("bands_3g")
        String bands3g,
        @JsonProperty("bands_4g")
        String bands4g) {
}