package com.isc.contract.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Business aggregate categories")
public enum AggregateType {

    CUSTOMER,
    SESSION,
    DEVICE,
    TOKEN
}
