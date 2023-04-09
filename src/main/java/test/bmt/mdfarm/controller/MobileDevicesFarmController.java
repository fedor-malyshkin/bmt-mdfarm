package test.bmt.mdfarm.controller;


import io.swagger.v3.oas.annotations.Parameter;
import test.bmt.mdfarm.model.MobileDeviceModelCode;
import test.bmt.mdfarm.model.dto.UserDto;
import test.bmt.mdfarm.model.mapper.MobileDeviceMapper;
import test.bmt.mdfarm.model.response.MobileDeviceListResp;
import test.bmt.mdfarm.model.response.MobileDeviceResp;
import test.bmt.mdfarm.service.MobileDevicesFarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "accounts", description = "Accounts API")
// ---
@RestController
@RequestMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MobileDevicesFarmController {
    private final MobileDevicesFarmService mobileDevicesFarmService;
    private final MobileDeviceMapper mobileDeviceMapper;


    @Operation(summary = "Book mobile device")
    @PostMapping(path = "/devices:book")
    public ResponseEntity<MobileDeviceResp> book(@Parameter(description = "Model")
                                                 @RequestParam(name = "model") MobileDeviceModelCode model,
                                                 @Parameter(description = "User ID")
                                                 @RequestParam(name = "user_id") String userId) {
        return mobileDevicesFarmService.book(model, new UserDto(userId))
                .rightMap(mobileDeviceMapper::toResponse)
                .leftMap(mobileDeviceMapper::toErrorResponse)
                .either(l -> ResponseEntity.status(HttpStatus.CONFLICT).body(l),
                        ResponseEntity::ok);
    }

    @Operation(summary = "Release mobile device")
    @PostMapping(path = "/devices:release")
    public ResponseEntity<MobileDeviceResp> release(@Parameter(description = "Model")
                                                 @RequestParam(name = "model") MobileDeviceModelCode model,
                                                 @Parameter(description = "User ID")
                                                 @RequestParam(name = "user_id") String userId) {
        return mobileDevicesFarmService.release(model, new UserDto(userId))
                .rightMap(mobileDeviceMapper::toResponse)
                .leftMap(mobileDeviceMapper::toErrorResponse)
                .either(l -> ResponseEntity.status(HttpStatus.CONFLICT).body(l),
                        ResponseEntity::ok);
    }

    @Operation(summary = "List mobile devices")
    @GetMapping(path = "/devices")
    public ResponseEntity<MobileDeviceListResp> list() {
        return mobileDevicesFarmService.list()
                .rightMap(mobileDeviceMapper::toResponse)
                .leftMap(mobileDeviceMapper::toListErrorResponse)
                .either(l -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(l),
                        ResponseEntity::ok);
    }

}
