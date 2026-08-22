package com.isc.common.web.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonWebFilterConfiguration {
    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilter() {
        FilterRegistrationBean<RequestCorrelationFilter> r = new FilterRegistrationBean<>();
        r.setFilter(new RequestCorrelationFilter());
        r.addUrlPatterns("/*");
        r.setOrder(10);
        return r;
    }
}
