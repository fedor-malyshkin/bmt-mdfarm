package test.bmt.mdfarm.service;

import fj.Function;
import fj.Unit;
import fj.data.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bmt.mdfarm.model.MobileDeviceModelCode;
import test.bmt.mdfarm.model.dto.MobileDeviceDto;
import test.bmt.mdfarm.model.dto.MobileDeviceSpecDto;
import test.bmt.mdfarm.model.dto.UserDto;
import test.bmt.mdfarm.model.mapper.MobileDeviceMapper;
import test.bmt.mdfarm.repository.MobileDeviceRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileDevicesFarmServiceImpl implements MobileDevicesFarmService {
    private final MobileDeviceRepository mdRepository;
    private final MobileDeviceMapper mdMapper;
    private final SpecificationExtractor extractor = new SpecificationExtractor();

    @Override
    public Either<String, MobileDeviceDto> book(MobileDeviceModelCode mobileModelCode, UserDto user) {
        return mdRepository.book(mobileModelCode, user)
                .bimap(logMapFun(err -> log.warn("the device <{}> cannot be booked because of <{}>", mobileModelCode, err)),
                        logMapFun(res -> log.info("the device <{}> was booked by <{}>", mobileModelCode, user)));
    }

    @Override
    public Either<String, MobileDeviceDto> release(MobileDeviceModelCode mobileModelCode, UserDto user) {
        return mdRepository.release(mobileModelCode, user)
                .bimap(logMapFun(err -> log.warn("the device <{}> cannot be released because of <{}>", mobileModelCode, err)),
                        logMapFun(res -> log.info("the device <{}> was released by <{}>", mobileModelCode, user)));
    }

    @Override
    public Either<String, List<MobileDeviceDto>> list() {
        return mdRepository.list()
                .rightMap(this::usePlaceholderForNonSynced)
                .leftMap(logMapFun(err -> log.warn("the device list cannot be generated because of <{}>", err)));
    }

    private List<MobileDeviceDto> usePlaceholderForNonSynced(List<MobileDeviceDto> mobileDevices) {
        return mobileDevices.stream()
                .map(this::usePlaceholderForNonSynced)
                .toList();
    }

    private MobileDeviceDto usePlaceholderForNonSynced(MobileDeviceDto original) {
        if (original.synced()) return original;
        return mdMapper.usePlaceholderForNonSynced(original);
    }


    private Either<String, Unit> updateSpecification(MobileDeviceSpecDto specification) {
        return mdRepository.updateSpecification(specification)
                .leftMap(logMapFun(err -> log.warn("unable update device specification <{}> because of <{}>",
                        specification.modelCode(), err)));
    }

    private Either<String, List<MobileDeviceSpecDto>> listSpecifications() {
        return mdRepository.listSpecifications()
                .leftMap(logMapFun(err -> log.warn("unable to get specifications because of <{}>", err)));
    }

    @Override
    public void updateSpecification() {
        listSpecifications()
                .rightMap(this::syncAndUpdateSpecifications)
                .bimap(logMapFun(err -> log.warn("the specifications cannot be updated because of <{}>", err)),
                        logMapFun(res -> log.info("the specifications <{}> were updated", res)));
    }

    private List<MobileDeviceSpecDto> syncAndUpdateSpecifications(List<MobileDeviceSpecDto> mobileDeviceSpecs) {
        return mobileDeviceSpecs.stream()
                .filter(s -> !s.synced())
                .map(this::syncAndUpdateSpecification)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<MobileDeviceSpecDto> syncAndUpdateSpecification(MobileDeviceSpecDto input) {
        var specOpt = extractor.extract(input);
        return specOpt.flatMap(spec -> {
            var updated = mdMapper.updateSpec(input, spec);
            return updateSpecification(updated)
                    .either((x)->Optional.empty(), (x)->Optional.of(updated));
        });
    }


    public <T> fj.F<T, T> logMapFun(Consumer<T> fun) {
        return (v) -> {
            fun.accept(v);
            return v;
        };
    }
}
