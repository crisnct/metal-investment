package com.investment.metal.infrastructure.encryption;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@Slf4j
public class HandShakeEncryptorDev extends AbstractHandShakeEncryptor {

    private static final long TIME_THRESHOLD = TimeUnit.HOURS.toMillis(5);

    private static final String HIJACK_HEADER_RESPONSE = "hijack";

    @Override
    public void check(String value) throws BusinessException {
        try {
            long requestTime = Long.parseLong(this.aesEncryptor.decrypt(value));
            long diffTime = System.currentTimeMillis() - requestTime;
            if (diffTime > TIME_THRESHOLD || diffTime < 0) {
                throw new IllegalArgumentException("Invalid handshake token");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("The request was not placed from an unauthorized source")
                    .build();
        } finally {
            this.response.setHeader(HIJACK_HEADER_RESPONSE, this.aesEncryptor.encrypt(String.valueOf(System.currentTimeMillis())));
        }
    }
}