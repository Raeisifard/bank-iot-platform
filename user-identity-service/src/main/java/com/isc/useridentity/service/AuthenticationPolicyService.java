package com.isc.useridentity.service;
import com.isc.useridentity.domain.*; import org.springframework.stereotype.Service;
@Service public class AuthenticationPolicyService {
 public AssuranceLevel minimum(AuthenticationPurpose p){return switch(p){case LOGIN,SESSION_REFRESH->AssuranceLevel.IAL_2;case ACCOUNT_RECOVERY->AssuranceLevel.IAL_3;case STEP_UP,BENEFICIARY_CHANGE,PAYMENT,PROFILE_CHANGE->AssuranceLevel.IAL_3;};}
 public AssuranceLevel levelFor(AuthenticationMethod m){return switch(m){case OTP->AssuranceLevel.IAL_2;case PIN,PASSWORD->AssuranceLevel.IAL_1;case BIOMETRIC,DEVICE_SIGNATURE,PASSKEY->AssuranceLevel.IAL_3;};}
 public boolean allowed(AuthenticationPurpose p, AuthenticationMethod m, AssuranceLevel required){return levelFor(m).satisfies(required) || (required==AssuranceLevel.IAL_3 && m==AuthenticationMethod.OTP);}
}
