package test.bmt.mdfarm.model.dto;

import test.bmt.mdfarm.model.MobileDeviceModelCode;

public record MobileDeviceSpecDto(String id,
                                  MobileDeviceModelCode modelCode,
                                  String source,
                                  boolean synced,
                                  String technology,
                                  String bands2g,
                                  String bands3g,
                                  String bands4g) {
}
