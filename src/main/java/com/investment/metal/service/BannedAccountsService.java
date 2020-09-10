package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.Util;
import com.investment.metal.database.BanIp;
import com.investment.metal.database.BanIpRepository;
import com.investment.metal.database.BannedAccount;
import com.investment.metal.database.BannedRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Optional;

@Service
public class BannedAccountsService extends AbstractService {

    @Autowired
    private BannedRepository bannedRepository;

    @Autowired
    private BanIpRepository banIpRepository;

    @Autowired
    private HttpServletRequest request;

    public void banUser(long userId, long amountTime, String reason) {
        BannedAccount entity = new BannedAccount();
        entity.setUserId(userId);
        entity.setBannedUntil(new Timestamp(System.currentTimeMillis() + amountTime));
        entity.setReason(reason);
        this.bannedRepository.save(entity);
    }

    public void banIp(long userId, String ip) throws BusinessException{
        Optional<BanIp> op = this.banIpRepository.findByUserIdAndIp(userId, ip);
        if (op.isPresent()){
            throw this.exceptionService
                    .createBuilder(MessageKey.BANED_IP)
                    .setArguments(ip)
                    .build();
        }else {
            BanIp banIp = new BanIp();
            banIp.setUserId(userId);
            banIp.setIp(ip);
            this.banIpRepository.save(banIp);
        }
    }

    public void unbanIp(Long userId, String ip) {
        this.banIpRepository.findByUserIdAndIp(userId, ip)
                .ifPresent(banIp -> banIpRepository.delete(banIp));
    }

    public Optional<BannedAccount> getBanInfo(long userId) {
        Optional<BannedAccount> op = this.bannedRepository.findByUserId(userId);
        if (op.isPresent()) {
            BannedAccount entity = op.get();
            if (entity.getBannedUntil().getTime() < System.currentTimeMillis()) {
                this.bannedRepository.delete(entity);
                op = Optional.empty();
            }
        }
        return op;
    }

    public void checkBanned(long userId) throws BusinessException {
        final String ip = Util.getClientIpAddress(this.request);
        Optional<BanIp> banIp = this.banIpRepository.findByUserIdAndIp(userId, ip);
        if (banIp.isPresent()){
            throw this.exceptionService
                    .createBuilder(MessageKey.BANED_IP)
                    .setArguments(ip)
                    .build();
        }

        Optional<BannedAccount> bannedInfo = getBanInfo(userId);
        if (bannedInfo.isPresent()) {
            BannedAccount entity = bannedInfo.get();
            String endTime = entity.getBannedUntil().toString();
            String reason = entity.getReason();
            throw this.exceptionService
                    .createBuilder(MessageKey.BANNED_ACCOUNT_UNTIL)
                    .setArguments(endTime, reason)
                    .build();
        }
    }

}
