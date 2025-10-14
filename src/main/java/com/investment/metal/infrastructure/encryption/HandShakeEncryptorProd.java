package com.investment.metal.infrastructure.encryption;

import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.service.BlockedIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class HandShakeEncryptorProd extends AbstractHandShakeEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandShakeEncryptorProd.class);

    private static final int TIME_THRESHOLD = 60 * 1000;

    public HandShakeEncryptorProd() {
        super();
    }

    @Override
    public void check(String value) throws BusinessException {
        this.blockIpService.checkBlockedIP(BlockedIpService.ID_GLOBAL_USER);
        try {
            long requestTime = Long.parseLong(this.aesEncryptor.decrypt(value));
            long diffTime = System.currentTimeMillis() - requestTime;
            if (diffTime > TIME_THRESHOLD || diffTime < 0) {
                throw new IllegalArgumentException("Invalid handshake token");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            this.blockIp();
        }
    }

}
