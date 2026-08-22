package com.isc.common.web;

import com.isc.common.web.filter.RequestCorrelationFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestCorrelationFilterTest {
    @Test
    void correlationHeaderNameShouldBeStable() {
        assertEquals("X-Correlation-Id", RequestCorrelationFilter.HEADER);
    }
}
