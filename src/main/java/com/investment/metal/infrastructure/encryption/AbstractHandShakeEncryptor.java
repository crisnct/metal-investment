package com.investment.metal.infrastructure.encryption;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.exception.NoRollbackBusinessException;
import com.investment.metal.domain.service.BlockedIpService;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public abstract class AbstractHandShakeEncryptor {

    public static final String AES_KEY_HANDSHAKE = "metal$investment";

    protected final AESEncryptor aesEncryptor;

    @Autowired
    protected BlockedIpService blockIpService;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected ExceptionService exceptionService;

    @Autowired
    private HttpServletRequest request;

    public AbstractHandShakeEncryptor() {
        this.aesEncryptor = new AESEncryptor(StandardCharsets.UTF_8);
        this.aesEncryptor.setKey(AES_KEY_HANDSHAKE);
    }

    @Transactional
    protected void blockIp() {
        final String ip = Util.getClientIpAddress(this.request);
        try {
            this.blockIpService.blockIP(BlockedIpService.ID_GLOBAL_USER, ip,
                    TimeUnit.DAYS.toMillis(1),
                    "Unauthorized requests. Someone is trying to hack your account.");
        } catch (BusinessException e) {
            //do nothing
        }
        throw exceptionService
                .createBuilder(MessageKey.INVALID_REQUEST)
                .setArguments("The request was not placed from an unauthorized source. The IP: " + ip + " is blocked for 24h.")
                .setException(NoRollbackBusinessException::new)
                .build();
    }

    public abstract void check(String hs) throws BusinessException;

}
