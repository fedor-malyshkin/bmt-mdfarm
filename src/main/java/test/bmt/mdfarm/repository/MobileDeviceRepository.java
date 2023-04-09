package test.bmt.mdfarm.repository;

import fj.Unit;
import fj.data.Either;
import test.bmt.mdfarm.model.MobileDeviceModelCode;
import test.bmt.mdfarm.model.dto.MobileDeviceDto;
import test.bmt.mdfarm.model.dto.MobileDeviceSpecDto;
import test.bmt.mdfarm.model.dto.UserDto;

import java.util.List;

public interface MobileDeviceRepository {

    Either<String, MobileDeviceDto> book(MobileDeviceModelCode mobileModelCode, UserDto user);

    Either<String, MobileDeviceDto> release(MobileDeviceModelCode mobileModelCode, UserDto user);

    Either<String, List<MobileDeviceDto>> list();

    Either<String, Unit> updateSpecification(MobileDeviceSpecDto specification);

    Either<String, List<MobileDeviceSpecDto>> listSpecifications();
}
