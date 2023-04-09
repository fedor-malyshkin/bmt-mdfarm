package test.bmt.mdfarm.model.mapper;

import org.mapstruct.*;
import test.bmk.mdfarm.model.persistence.jooq.tables.pojos.MobileDeviceSpecs;
import test.bmk.mdfarm.model.persistence.jooq.tables.pojos.MobileDevices;
import test.bmt.mdfarm.model.dto.MobileDeviceDto;
import test.bmt.mdfarm.model.dto.MobileDeviceSpecDto;
import test.bmt.mdfarm.model.dto.UserDto;
import test.bmt.mdfarm.model.response.MobileDeviceListResp;
import test.bmt.mdfarm.model.response.MobileDeviceResp;
import test.bmt.mdfarm.service.SpecificationExtractor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        componentModel = "spring")
public abstract class MobileDeviceMapper {
    public static final String DATE_TIME_FORMATTER_PATTERN = "yyyy-MM-dd'T'HH:mm:sszzz";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER_PATTERN);

    @Mapping(target = "bookTime", source = "bookTime", qualifiedByName = "wrap")
    @Mapping(target = "bookingOwner", source = "bookingOwner", qualifiedByName = "wrap")
    public abstract MobileDeviceDto toDto(MobileDevices source);

    @Mapping(target = "id", source = "device.id")
    @Mapping(target = "modelCode", source = "device.modelCode")
    @Mapping(target = "bookTime", source = "device.bookTime", qualifiedByName = "wrap")
    @Mapping(target = "bookingOwner", source = "device.bookingOwner", qualifiedByName = "wrap")
    @Mapping(target = "technology", source = "specification.technology")
    @Mapping(target = "bands2g", source = "specification.bands2g")
    @Mapping(target = "bands3g", source = "specification.bands3g")
    @Mapping(target = "bands4g", source = "specification.bands4g")
    public abstract MobileDeviceDto toDto(MobileDevices device, MobileDeviceSpecs specification);

    public List<MobileDeviceDto> toDtoList(List<MobileDevices> source) {
        return source.stream()
                .map(this::toDto)
                .toList();
    }

    public abstract MobileDeviceSpecDto toDto(MobileDeviceSpecs mobileDeviceSpecs);

    @Named("wrap")
    protected <T> Optional<T> wrap(T source) {
        return Optional.ofNullable(source);
    }

    @Named("unwrap")
    protected <T> T unwrap(Optional<T> source) {
        if (source == null) return null;
        return source.orElse(null);
    }

    protected UserDto toUserDto(String source) {
        return new UserDto(source);
    }

    protected String fromUserDto(UserDto source) {
        if (source == null) return null;
        return source.id();
    }

    @Mapping(target = "bookTime", source = "bookTime", qualifiedByName = "unwrap")
    @Mapping(target = "bookingOwner", source = "bookingOwner", qualifiedByName = "unwrap")
    @Mapping(target = "technology", source = "technology")
    @Mapping(target = "bands2g", source = "bands2g")
    @Mapping(target = "bands3g", source = "bands3g")
    @Mapping(target = "bands4g", source = "bands4g")
    public abstract MobileDeviceResp toResponse(MobileDeviceDto mobileDeviceDto);

    public MobileDeviceListResp toResponse(List<MobileDeviceDto> source) {
        return new MobileDeviceListResp(null, source.stream()
                .map(this::toResponse)
                .toList());
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "error", source = "error")
    public abstract MobileDeviceResp toErrorResponse(String error);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "error", source = "error")
    public abstract MobileDeviceListResp toListErrorResponse(String error);


    public String mapLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null)
            return localDateTime.atZone(ZoneOffset.UTC).format(DATE_TIME_FORMATTER);
        else return null;
    }

    @Mapping(target = "technology", constant = "updating...")
    @Mapping(target = "bands2g", constant = "updating...")
    @Mapping(target = "bands3g", constant = "updating...")
    @Mapping(target = "bands4g", constant = "updating...")
    public abstract MobileDeviceDto usePlaceholderForNonSynced(MobileDeviceDto source);

    @Mapping(target = "technology", source = "spec.technology")
    @Mapping(target = "bands2g", source = "spec.bands2g")
    @Mapping(target = "bands3g", source = "spec.bands3g")
    @Mapping(target = "bands4g", source = "spec.bands4g")
    public abstract MobileDeviceSpecDto updateSpec(MobileDeviceSpecDto source, SpecificationExtractor.Specification spec);

}
