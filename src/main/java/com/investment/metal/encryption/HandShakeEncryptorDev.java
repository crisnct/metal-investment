package com.investment.metal.encryption;

import com.investment.metal.MessageKey;
import com.investment.metal.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class HandShakeEncryptorDev extends AbstractHandShakeEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandShakeEncryptorProd.class);

    private static final int TIME_THRESHOLD = 5 * 60 * 1000;

    public HandShakeEncryptorDev() {
        super();
    }

    @Override
    public void check(String value) throws BusinessException {
        try {
            long requestTime = Long.parseLong(this.aesEncryptor.decrypt(value));
            long diffTime = System.currentTimeMillis() - requestTime;
            if (diffTime > TIME_THRESHOLD || diffTime < 0) {
                throw new IllegalArgumentException("Invalid handshake token");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("The request was not placed from an unauthorized source")
                    .build();
        } finally {
            this.response.setHeader("hijack", this.aesEncryptor.encrypt(String.valueOf(System.currentTimeMillis())));
        }
    }
}
