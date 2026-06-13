package com.isc.tokenservice.vault;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isc.tokenservice.config.JwtProperties;
import com.isc.tokenservice.dto.JwtKeyPolicy;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtKeyPolicyService {

    private final VaultTemplate vaultTemplate;
    private final JwtProperties jwtProperties;
    @Getter
    private volatile JwtKeyPolicy cachedPolicy;
    private final ObjectMapper mapper;

    @PostConstruct
    public void init() {
        refresh();
    }

    public JwtKeyPolicy getPolicy() {
        return cachedPolicy;
    }

    public synchronized void refresh() {

        VaultVersionedKeyValueOperations kv =
                vaultTemplate.opsForVersionedKeyValue(jwtProperties.getKvMount());

        Versioned<Map<String, Object>> response = kv.get(jwtProperties.getKvPath());

        assert response != null;
        cachedPolicy = mapper.convertValue(
                response.getData(),
                JwtKeyPolicy.class
        );
    }

    public JwtKeyPolicy loadPolicy() {
        try {

            VaultVersionedKeyValueOperations kv =
                    vaultTemplate.opsForVersionedKeyValue(
                            jwtProperties.getKvMount()
                    );

            Versioned<Map<String, Object>> response =
                    kv.get(jwtProperties.getKvPath());

            if (response == null) {
                return createDefaultPolicy();
            }

            Map<String, Object> data = response.getData();

            return data == null
                    ? createDefaultPolicy()
                    : mapper.convertValue(data, JwtKeyPolicy.class);

        } catch (Exception e) {
            log.error("Failed to load JWT policy from Vault", e);
            return createDefaultPolicy();
        }
    }

    public JwtKeyPolicy createDefaultPolicy() {

        Instant now = Instant.now();
        final long DEFAULT_KEY_INTERVAL_DAYS = jwtProperties.getRotation().getKeyInterval().toDays();
        final long DEFAULT_OVERLAP_PERIOD_DAYS = jwtProperties.getRotation().getOverlapPeriod().toDays();
        Instant activeValidTo = now.plus(DEFAULT_KEY_INTERVAL_DAYS, ChronoUnit.DAYS);

        // Create default active key (will be populated with actual key data later)
        JwtKeyPolicy.KeyInfo active = createKeyInfo(JwtKid.newKid(now), 1, now, activeValidTo);

        // Create default next key
        Instant nextValidFrom = now.plus(DEFAULT_KEY_INTERVAL_DAYS - DEFAULT_OVERLAP_PERIOD_DAYS, ChronoUnit.DAYS);
        Instant nextValidTo = nextValidFrom.plus(DEFAULT_KEY_INTERVAL_DAYS, ChronoUnit.DAYS);
        JwtKeyPolicy.KeyInfo next = createKeyInfo(JwtKid.newKid(now.plus(1L, ChronoUnit.DAYS)), 2, nextValidFrom, nextValidTo);

        cachedPolicy.setActive(active);
        cachedPolicy.setNext(next);
        cachedPolicy.setRetired(new ArrayList<>());

        log.info("Created default JWT key policy with active key version: {}", active.getVkv());

        return cachedPolicy;
    }

    private JwtKeyPolicy.KeyInfo createKeyInfo(String kid, int vaultKeyVersion,
                                               Instant validFrom, Instant validTo) {
        JwtKeyPolicy.KeyInfo keyInfo = new JwtKeyPolicy.KeyInfo();
        keyInfo.setKid(kid);
        keyInfo.setVkv(vaultKeyVersion);
        keyInfo.setVlf(validFrom);
        keyInfo.setVlt(validTo);
        return keyInfo;
    }

    public void rotateToNext(String newKid, int newVersion) {
        try {

            if (cachedPolicy == null) {
                log.warn("No policy found, creating default policy before rotation");
                cachedPolicy = createDefaultPolicy();
            }

            // Initialize retired list if null
            if (cachedPolicy.getRetired() == null) {
                cachedPolicy.setRetired(new ArrayList<>());
            }

            // Validate that next key exists
            if (cachedPolicy.getNext() == null) {
                log.error("Cannot rotate: No next key configured in policy");
                throw new IllegalStateException("No next key available for rotation");
            }

            // move inactive → retired (if inactive exists)
            if (cachedPolicy.getInactive() != null) {
                cachedPolicy.getRetired().add(0, cachedPolicy.getInactive()); // Add to beginning of list
                log.info("Moved inactive key (kid: {}) to retired", cachedPolicy.getInactive().getKid());
            }

            // active → inactive
            cachedPolicy.setInactive(cachedPolicy.getActive());
            log.info("Changed active key (kid: {}) to inactive", cachedPolicy.getInactive().getKid());

            // next → active
            cachedPolicy.setActive(cachedPolicy.getNext());
            log.info("Promoted next key (kid: {}) to active", cachedPolicy.getActive().getKid());

            // create new next
            JwtKeyPolicy.KeyInfo next = createNextKey(newKid, newVersion, cachedPolicy);
            cachedPolicy.setNext(next);

            // Limit retired keys to prevent unlimited growth (keep last 10)
            if (cachedPolicy.getRetired().size() > 10) {
                List<JwtKeyPolicy.KeyInfo> limitedRetired = cachedPolicy.getRetired().subList(0, 10);
                cachedPolicy.setRetired(limitedRetired);
                log.info("Limited retired keys to 10 most recent");
            }

            savePolicy(cachedPolicy);
            log.info("Key rotation completed successfully. New active version: {}, New next version: {}",
                    newVersion - 1, newVersion);

        } catch (Exception e) {
            log.error("Error during key rotation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to rotate keys", e);
        }
    }

    private JwtKeyPolicy.KeyInfo createNextKey(String newKid, int newVersion, JwtKeyPolicy policy) {
        JwtKeyPolicy.KeyInfo next = new JwtKeyPolicy.KeyInfo();
        next.setKid(newKid);
        next.setVkv(newVersion);
        Instant now = Instant.now();
        Instant validFrom = now;
        Instant validTo = now.plus(jwtProperties.getRotation().getKeyInterval().toDays(), ChronoUnit.DAYS);

        next.setVlf(validFrom);
        next.setVlt(validTo);

        return next;
    }

    public boolean isPolicyInitialized() {
        try {

            VaultVersionedKeyValueOperations kv =
                    vaultTemplate.opsForVersionedKeyValue(
                            jwtProperties.getKvMount()
                    );

            Versioned<JwtKeyPolicy> response =
                    kv.get(jwtProperties.getKvPath(), JwtKeyPolicy.class);

            return response != null && response.getData() != null;

        } catch (Exception e) {
            log.debug("Policy not initialized: {}", e.getMessage());
            return false;
        }
    }

    public void initializeIfNeeded() {
        if (!isPolicyInitialized()) {
            log.info("Initializing JWT key policy in Vault");
            JwtKeyPolicy defaultPolicy = createDefaultPolicy();
            savePolicy(defaultPolicy);
        }
    }

    public void savePolicy(JwtKeyPolicy policy) {

        try {

            VaultVersionedKeyValueOperations kv =
                    vaultTemplate.opsForVersionedKeyValue(
                            jwtProperties.getKvMount()
                    );

            kv.put(
                    jwtProperties.getKvPath(),
                    mapper.convertValue(
                            policy,
                            new TypeReference<Map<String, Object>>() {
                            }
                    )
            );

            cachedPolicy = policy;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save JWT key policy", e);
        }
    }

}