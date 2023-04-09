package test.bmt.mdfarm.service;

import fj.Unit;
import fj.data.Either;
import test.bmt.mdfarm.model.MobileDeviceModelCode;
import test.bmt.mdfarm.model.dto.MobileDeviceDto;
import test.bmt.mdfarm.model.dto.MobileDeviceSpecDto;
import test.bmt.mdfarm.model.dto.UserDto;

import java.util.List;

public interface MobileDevicesFarmService {
    Either<String, MobileDeviceDto> book(MobileDeviceModelCode mobileModelCode, UserDto user);

    Either<String, MobileDeviceDto> release(MobileDeviceModelCode mobileModelCode, UserDto user);

    Either<String, List<MobileDeviceDto>> list();

    void updateSpecification();
}
