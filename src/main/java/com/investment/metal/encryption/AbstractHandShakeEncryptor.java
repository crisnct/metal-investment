package com.investment.metal.encryption;

import com.investment.metal.MessageKey;
import com.investment.metal.common.Util;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.service.BlockedIpService;
import com.investment.metal.service.exception.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public abstract class AbstractHandShakeEncryptor {

    private static final String AES_KEY_HANDSHAKE = "metal$investment";

    @Autowired
    protected BlockedIpService blockIpService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected ExceptionService exceptionService;

    protected final AESEncryptor aesEncryptor;

    public AbstractHandShakeEncryptor() {
        this.aesEncryptor = new AESEncryptor(StandardCharsets.UTF_8);
        this.aesEncryptor.setKey(AES_KEY_HANDSHAKE);
    }

    @Transactional
    protected void blockIp() {
        final String ip = Util.getClientIpAddress(this.request);
        try {
            this.blockIpService.blockIP(BlockedIpService.ID_GLOBAL_USER, ip);
        } catch (BusinessException e) {
            //do nothing
        }
        throw exceptionService
                .createBuilder(MessageKey.INVALID_REQUEST)
                .setArguments("The request was not placed from an unauthorized source. The IP: " + ip + " is blocked.")
                .setException(NoRollbackBusinessException::new)
                .build();
    }

    public abstract void check(String hs) throws BusinessException;

}
