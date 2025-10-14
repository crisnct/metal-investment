package com.investment.metal.domain.service;

import com.investment.metal.MessageKey;
import com.investment.metal.infrastructure.persistence.entity.BannedAccount;
import com.investment.metal.infrastructure.persistence.repository.BannedRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.infrastructure.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class BannedAccountsService extends AbstractService {

    @Autowired
    private BannedRepository bannedRepository;

    public void banUser(Integer userId, long amountTime, String reason) {
        BannedAccount entity = new BannedAccount();
        entity.setUserId(userId);
        entity.setBannedUntil(new Timestamp(System.currentTimeMillis() + amountTime));
        entity.setReason(reason);
        this.bannedRepository.save(entity);
    }

    public void checkBanned(Integer userId) throws BusinessException {
        Optional<BannedAccount> bannedInfo = getBanAccountInfo(userId);
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

    private Optional<BannedAccount> getBanAccountInfo(Integer userId) {
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

}
