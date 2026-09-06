package com.isc.delivery;

import com.isc.common.redis.RedisOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "common.redis.enabled=true",
        "common.redis.url=redis://localhost:6379",
        "delivery.enabled=false",
        "delivery.oracle.enabled=false",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class DeliveryServiceApplicationTest {

    @Autowired
    private org.springframework.context.ApplicationContext context;

    @Test
    void loadsCommonRedisWithoutCreatingDisabledOracleDataSource() {
        assertThat(context.getBean(RedisOperations.class)).isNotNull();
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
    }
}