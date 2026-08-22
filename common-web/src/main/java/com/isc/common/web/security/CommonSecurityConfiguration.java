package com.isc.common.web.security;

import com.isc.common.security.jwt.JwtValidatorService;
import com.isc.common.web.config.CommonWebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
        prefix = "common.web",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CommonSecurityConfiguration implements WebMvcConfigurer {

    private final CommonWebProperties properties;

    public CommonSecurityConfiguration(CommonWebProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnBean(JwtValidatorService.class)
    @ConditionalOnProperty(
            prefix = "common.web",
            name = "authentication-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtValidatorService validator) {
        return new JwtAuthenticationFilter(validator);
    }

    @Bean
    public SecurityFilterChain commonSecurityFilterChain(
            HttpSecurity http,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            JwtAuthenticationFilter filter) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(properties.getPublicPaths()).permitAll();

                    if (!properties.isSecurityEnabled()
                            || !properties.isAuthenticationEnabled()) {
                        auth.anyRequest().permitAll();
                        return;
                    }

                    if (properties.getProtectedPaths() != null
                            && properties.getProtectedPaths().length > 0) {
                        auth.requestMatchers(properties.getProtectedPaths())
                                .authenticated();
                    }

                    auth.anyRequest().permitAll();
                });

        if (filter != null
                && properties.isSecurityEnabled()
                && properties.isAuthenticationEnabled()) {
            http.addFilterBefore(
                    filter,
                    UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public CurrentUserArgumentResolver currentUserArgumentResolver() {
        return new CurrentUserArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(
            List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver());
    }
}
