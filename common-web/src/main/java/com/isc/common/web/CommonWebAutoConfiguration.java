package com.isc.common.web;

import com.isc.common.web.config.CommonWebConfiguration;
import com.isc.common.web.error.GlobalExceptionHandler;
import com.isc.common.web.filter.CommonWebFilterConfiguration;
import com.isc.common.web.security.CommonSecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        CommonWebConfiguration.class,
        CommonWebFilterConfiguration.class,
        CommonSecurityConfiguration.class,
        GlobalExceptionHandler.class
})
public class CommonWebAutoConfiguration {
}
