package com.isc.useridentity.provider;
import lombok.extern.slf4j.Slf4j; import org.springframework.stereotype.Component;
@Component @Slf4j public class LoggingOtpDeliveryProvider implements OtpDeliveryProvider { public void deliver(String userId,String destination,String code,String purpose){ log.info("OTP delivery requested user={} destination={} purpose={} [DEV PROVIDER]",userId,destination,purpose); log.debug("DEV OTP value for user={} is {}",userId,code); } }
