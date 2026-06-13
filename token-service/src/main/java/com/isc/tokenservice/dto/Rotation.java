package com.isc.tokenservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Duration;

@Data
public class Rotation {

    /**
     * نوع استراتژی چرخش کلید
     * MANUAL = فقط با درخواست ادمین
     * SCHEDULED = بر اساس زمان
     * EVENT_DRIVEN = بر اساس event (مثلاً risk detection)
     */
    private RotationStrategy strategy;

    /**
     * زمان‌بندی دوره‌ای برای rotation (مثلاً هر 30 روز)
     */
    @Schema(description = "Rotation interval Duration", example = "P30D")
    private Duration keyInterval;

    /**
     * grace period برای نگه داشتن کلید قبلی بعد از rotation
     * (خیلی مهم برای EMQX + JWT overlap)
     */
    private Duration overlapPeriod;

    /**
     * تعداد نسخه‌هایی که باید در حالت retired نگه داشته شوند
     */
    private int retainVersions;

    /**
     * دوره ای که Scheduler در آن JwtKeyPolicy بروز می کند
     */
    private long checkInterval;
}
