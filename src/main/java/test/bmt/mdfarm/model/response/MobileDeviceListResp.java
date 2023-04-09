package test.bmt.mdfarm.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MobileDeviceListResp(
        String error,
        List<MobileDeviceResp> devices) {
}