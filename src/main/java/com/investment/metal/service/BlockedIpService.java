package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.Util;
import com.investment.metal.database.BanIp;
import com.investment.metal.database.BanIpRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class BlockedIpService extends AbstractService {

    public static final Long ID_GLOBAL_USER = -1L;

    @Autowired
    private BanIpRepository banIpRepository;

    @Autowired
    private HttpServletRequest request;

    public void blockIP(long userId, String ip) throws BusinessException {
        Optional<BanIp> op = this.banIpRepository.findByUserIdAndIp(userId, ip);
        if (op.isPresent()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.BANED_IP)
                    .setArguments(ip)
                    .build();
        } else {
            BanIp banIp = new BanIp();
            banIp.setUserId(userId);
            banIp.setIp(ip);
            this.banIpRepository.save(banIp);
        }
    }

    public void unblockIP(Long userId, String ip) {
        this.banIpRepository.findByUserIdAndIp(userId, ip)
                .ifPresent(banIp -> banIpRepository.delete(banIp));
    }

    public void checkBlockedIP(long userId) throws BusinessException {
        final String ip = Util.getClientIpAddress(this.request);
        Optional<BanIp> banIp = this.banIpRepository.findByUserIdAndIp(userId, ip);
        Optional<BanIp> banIpGlobal = this.banIpRepository.findByUserIdAndIp(ID_GLOBAL_USER, ip);
        if (banIp.isPresent() || banIpGlobal.isPresent()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.BANED_IP)
                    .setArguments(ip)
                    .build();
        }
    }
}
