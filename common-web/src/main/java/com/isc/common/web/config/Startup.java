package com.isc.common.web.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class Startup {
    private final Environment env;

    @PostConstruct
    void init() {
        System.out.println("Active profile: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("security.authentication.enabled = " + env.getProperty("security.authentication.enabled"));
    }
}
