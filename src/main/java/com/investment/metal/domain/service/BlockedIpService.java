package com.investment.metal.domain.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.Util;
import com.investment.metal.infrastructure.persistence.entity.BanIp;
import com.investment.metal.infrastructure.persistence.repository.BanIpRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.infrastructure.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BlockedIpService extends AbstractService {

    public static final Integer ID_GLOBAL_USER = -1;

    private static final long YEARS_100_IP_BLOCKED = TimeUnit.DAYS.toMillis(365 * 100);

    @Autowired
    private BanIpRepository banIpRepository;

    @Autowired
    private HttpServletRequest request;

    public void blockIPForever(Integer userId, String ip, String reason) throws BusinessException {
        this.blockIP(userId, ip, YEARS_100_IP_BLOCKED, reason);
    }

    public void blockIP(Integer userId, String ip, long amountTime, String reason) throws BusinessException {
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
            banIp.setBlockedUntil(new Timestamp(System.currentTimeMillis() + amountTime));
            banIp.setReason(reason);
            this.banIpRepository.save(banIp);
        }
    }

    public void unblockIP(Integer userId, String ip) {
        this.banIpRepository.findByUserIdAndIp(userId, ip)
                .ifPresent(banIp -> banIpRepository.delete(banIp));
    }

    public void checkBlockedIP(Integer userId) throws BusinessException {
        final String ip = Util.getClientIpAddress(this.request);
        final BanIp banIp = this.getBanIp(userId, ip);
        if (banIp != null) {
            throw this.exceptionService
                    .createBuilder(MessageKey.BANED_IP)
                    .setArguments(ip, banIp.getReason())
                    .build();
        }
        this.checkBlockedIPGlobal();
    }

    public void checkBlockedIPGlobal() throws BusinessException {
        final String ip = Util.getClientIpAddress(this.request);
        final BanIp banIpGlobal = this.getBanIp(ID_GLOBAL_USER, ip);
        if (banIpGlobal != null) {
            throw this.exceptionService
                    .createBuilder(MessageKey.BANED_IP)
                    .setArguments(ip, banIpGlobal.getReason())
                    .build();
        }
    }

    private BanIp getBanIp(Integer userId, String ip) {
        Optional<BanIp> banIpOp = this.banIpRepository.findByUserIdAndIp(userId, ip);
        BanIp banIp = null;
        if (banIpOp.isPresent()) {
            BanIp ban = banIpOp.get();
            if (ban.getBlockedUntil().before(new Timestamp(System.currentTimeMillis()))) {
                this.banIpRepository.delete(ban);
            } else {
                banIp = ban;
            }
        }
        return banIp;
    }

}
