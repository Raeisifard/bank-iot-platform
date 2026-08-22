package com.isc.common.security.cache;
import org.junit.jupiter.api.Test;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.security.PublicKey;
import static org.junit.jupiter.api.Assertions.*;
class SecurityKeyCacheTest {
 @Test void shouldCacheAndInvalidate() {
  var cache=new SecurityKeyCacheImpl(Caffeine.newBuilder().build());
  PublicKey key=new TestKey(); cache.put("k1",key);
  assertSame(key,cache.get("k1")); cache.invalidate("k1"); assertNull(cache.get("k1"));
 }
 static class TestKey implements PublicKey {
  public String getAlgorithm(){return "RSA";} public String getFormat(){return "X.509";} public byte[] getEncoded(){return new byte[]{1};}
 }
}
