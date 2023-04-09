package test.bmt.mdfarm.repository;

import fj.Unit;
import fj.data.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Service;
import test.bmk.mdfarm.model.persistence.jooq.tables.pojos.MobileDeviceSpecs;
import test.bmk.mdfarm.model.persistence.jooq.tables.pojos.MobileDevices;
import test.bmt.mdfarm.model.MobileDeviceModelCode;
import test.bmt.mdfarm.model.dto.MobileDeviceDto;
import test.bmt.mdfarm.model.dto.MobileDeviceSpecDto;
import test.bmt.mdfarm.model.dto.UserDto;
import test.bmt.mdfarm.model.mapper.MobileDeviceMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static test.bmk.mdfarm.model.persistence.jooq.tables.MobileDeviceSpecs.MOBILE_DEVICE_SPECS;
import static test.bmk.mdfarm.model.persistence.jooq.tables.MobileDevices.MOBILE_DEVICES;


@Service
@RequiredArgsConstructor
@Slf4j
public class MobileDeviceRepositoryImpl implements MobileDeviceRepository {
    private final DSLContext dslContext;
    private final MobileDeviceMapper mdMapper;

    @Override
    public Either<String, MobileDeviceDto> book(MobileDeviceModelCode mobileModelCode, UserDto user) {
        return runSafely(() -> dslContext.transactionResult((Configuration tnx) -> {

                    var dsl = tnx.dsl();

                    var mdOpt = dsl.selectFrom(MOBILE_DEVICES)
                            .where(MOBILE_DEVICES.MODEL_CODE.equalIgnoreCase(mobileModelCode.name())
                                    .and(MOBILE_DEVICES.BOOKED.eq(false)))
                            .limit(1)
                            .fetchOptional()
                            .map(r -> r.into(MobileDevices.class));

                    if (mdOpt.isEmpty())
                        return Either.left(String.format("There is no free device of type <%s>", mobileModelCode));

                    dsl.update(MOBILE_DEVICES)
                            .set(MOBILE_DEVICES.BOOKED, true)
                            .set(MOBILE_DEVICES.BOOK_TIME, LocalDateTime.now())
                            .set(MOBILE_DEVICES.BOOKING_OWNER, user.id())
                            .where(MOBILE_DEVICES.ID.eq(mdOpt.get().getId()))
                            .execute();

                    return queryById(mobileModelCode, user, dsl, mdOpt.get().getId());

                })
        );
    }

    private Either<String, MobileDeviceDto> queryById(MobileDeviceModelCode mobileModelCode,
                                                      UserDto user,
                                                      DSLContext dsl,
                                                      String id) {
        return dsl.selectFrom(MOBILE_DEVICES)
                .where(MOBILE_DEVICES.ID.eq(id))
                .limit(1)
                .fetchOptional()
                .map(r -> r.into(MobileDevices.class))
                .map(mdMapper::toDto)
                .map(Either::<String, MobileDeviceDto>right)
                .orElse(Either.left(String.format("There is no free device of type <%s> with id <%s>", mobileModelCode, user.id())));
    }

    @Override
    public Either<String, MobileDeviceDto> release(MobileDeviceModelCode mobileModelCode, UserDto user) {
        return runSafely(() -> dslContext.transactionResult((Configuration tnx) -> {
                    var dsl = tnx.dsl();
                    var mdOpt = dsl.selectFrom(MOBILE_DEVICES)
                            .where(MOBILE_DEVICES.MODEL_CODE.equalIgnoreCase(mobileModelCode.name())
                                    .and(MOBILE_DEVICES.BOOKING_OWNER.equalIgnoreCase(user.id()))
                                    .and(MOBILE_DEVICES.BOOKED.eq(true)))
                            .limit(1)
                            .fetchOptional()
                            .map(r -> r.into(MobileDevices.class));

                    if (mdOpt.isEmpty())
                        return Either.left(String.format("There is no device of type <%s> booked by <%s>", mobileModelCode, user));

                    dsl.update(MOBILE_DEVICES)
                            .set(MOBILE_DEVICES.BOOKED, false)
                            .setNull(MOBILE_DEVICES.BOOK_TIME)
                            .setNull(MOBILE_DEVICES.BOOKING_OWNER)
                            .where(MOBILE_DEVICES.ID.eq(mdOpt.get().getId()))
                            .execute();

                    return queryById(mobileModelCode, user, dsl, mdOpt.get().getId());
                })
        );
    }

    @Override
    public Either<String, List<MobileDeviceDto>> list() {
        return runSafely(() -> {
            var fields = joinArrays(MOBILE_DEVICES.fields(), MOBILE_DEVICE_SPECS.fields());
            var list = dslContext.select(fields)
                    .from(MOBILE_DEVICES.leftOuterJoin(MOBILE_DEVICE_SPECS)
                            .on(MOBILE_DEVICES.MODEL_CODE.eq(MOBILE_DEVICE_SPECS.MODEL_CODE)))
                    .fetch()
                    .map(r -> {
                        var pojoMD = r.into(MOBILE_DEVICES).into(MobileDevices.class);
                        var temp = r.into(MOBILE_DEVICE_SPECS);
                        if (temp.get("id") == null)
                            return mdMapper.toDto(pojoMD);
                        var pojoMDS = temp.into(MobileDeviceSpecs.class);
                        return mdMapper.toDto(pojoMD, pojoMDS);
                    });
            return Either.right(list);
        });
    }


    @Override
    public Either<String, List<MobileDeviceSpecDto>> listSpecifications() {
        return runSafely(() -> {
            var list = dslContext.selectFrom(MOBILE_DEVICE_SPECS)
                    .fetchStream()
                    .map(r -> r.into(MobileDeviceSpecs.class))
                    .map(mdMapper::toDto)
                    .toList();
            return Either.right(list);
        });
    }

    @Override
    public Either<String, Unit> updateSpecification(MobileDeviceSpecDto specification) {
        return runSafely(() -> dslContext.transactionResult((Configuration tnx) -> {
                    var dsl = tnx.dsl();
                    dsl.update(MOBILE_DEVICE_SPECS)
                            .set(MOBILE_DEVICE_SPECS.SYNCED, true)
                            .set(MOBILE_DEVICE_SPECS.TECHNOLOGY, specification.technology())
                            .set(MOBILE_DEVICE_SPECS.BANDS2G, specification.bands2g())
                            .set(MOBILE_DEVICE_SPECS.BANDS3G, specification.bands3g())
                            .set(MOBILE_DEVICE_SPECS.BANDS4G, specification.bands4g())
                            .where(MOBILE_DEVICE_SPECS.ID.eq(specification.id()))
                            .execute();
                    return Either.right(Unit.unit());
                })
        );
    }

    private <R> Either<String, R> runSafely(Supplier<Either<String, R>> dbCall) {
        try {
            return dbCall.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Either.left(e.getMessage());
        }
    }


    private static Field<?>[] joinArrays(Field<?>[] arr1, Field<?>[] arr2) {
        var fields = new Field<?>[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, fields, 0, arr1.length);
        System.arraycopy(arr2, 0, fields, arr1.length, arr2.length);
        return fields;
    }
}
