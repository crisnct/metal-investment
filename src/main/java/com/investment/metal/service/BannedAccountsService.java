package com.investment.metal.service;

import com.investment.metal.database.BannedAccount;
import com.investment.metal.database.BannedRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.CustomErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class BannedAccountsService {

    @Autowired
    private BannedRepository bannedRepository;

    public void banUser(long userId, long amountTime, String reason) {
        BannedAccount entity = new BannedAccount();
        entity.setUserId(userId);
        entity.setBannedUntil(new Timestamp(System.currentTimeMillis() + amountTime));
        entity.setReason(reason);
        this.bannedRepository.save(entity);
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
        Optional<BannedAccount> bannedInfo = getBanInfo(userId);
        if (bannedInfo.isPresent()) {
            BannedAccount bannedAccount = bannedInfo.get();
            String message = "This account is banned until " + bannedAccount.getBannedUntil().toString() + ". Reason: " + bannedAccount.getReason();
            throw new BusinessException(CustomErrorCodes.USER_RETRIEVE, message);
        }
    }
}
