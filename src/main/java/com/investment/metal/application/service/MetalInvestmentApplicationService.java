package com.investment.metal.application.service;

import com.investment.metal.application.repository.MetalPurchaseRepository;
import com.investment.metal.application.repository.UserRepository;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.MetalInvestmentDomainService;
import com.investment.metal.dto.UserMetalInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for metal investment use cases.
 * Orchestrates domain services and handles application-level concerns.
 * Follows Clean Architecture principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MetalInvestmentApplicationService {

    private final MetalInvestmentDomainService metalInvestmentDomainService;
    private final MetalPurchaseRepository metalPurchaseRepository;
    private final MetalPriceService metalPriceService;
    private final UserRepository userRepository;

    /**
     * Add metal purchase for user
     */
    public MetalPurchase addMetalPurchase(Integer userId, String metalSymbol, 
                                        BigDecimal amount, BigDecimal cost) {
        log.info("Adding metal purchase for user: {}, metal: {}, amount: {}", 
                userId, metalSymbol, amount);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.canPerformActions()) {
            throw new RuntimeException("User cannot perform actions");
        }
        
        MetalPurchase purchase = MetalPurchase.builder()
            .userId(userId)
            .metalType(metalInvestmentDomainService.getMetalType(metalSymbol))
            .amount(amount)
            .cost(cost)
            .build();
        
        if (!purchase.isValid()) {
            throw new RuntimeException("Invalid purchase data");
        }
        
        MetalPurchase savedPurchase = metalPurchaseRepository.save(purchase);
        
        log.info("Metal purchase added successfully for user: {}", userId);
        return savedPurchase;
    }

    /**
     * Get user's metal investment information
     */
    @Transactional(readOnly = true)
    public List<UserMetalInfoDto> getUserMetalInfo(Integer userId) {
        log.info("Getting metal info for user: {}", userId);
        
        List<MetalPurchase> purchases = metalPurchaseRepository.findByUserId(userId);
        
        return purchases.stream()
            .map(purchase -> {
                BigDecimal currentPrice = metalPriceService.getCurrentPrice(purchase.getMetalType());
                BigDecimal currentValue = purchase.calculateCurrentValue(currentPrice);
                BigDecimal profit = purchase.calculateProfit(currentPrice);
                
                return UserMetalInfoDto.builder()
                    .metalSymbol(purchase.getMetalType().getSymbol())
                    .amountPurchased(purchase.getAmount().doubleValue())
                    .costPurchased(purchase.getCost().doubleValue())
                    .costNow(currentValue.doubleValue())
                    .profit(profit.doubleValue())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Calculate total portfolio value for user
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPortfolioValue(Integer userId) {
        log.info("Calculating total portfolio value for user: {}", userId);
        
        List<MetalPurchase> purchases = metalPurchaseRepository.findByUserId(userId);
        
        return purchases.stream()
            .map(purchase -> {
                BigDecimal currentPrice = metalPriceService.getCurrentPrice(purchase.getMetalType());
                return purchase.calculateCurrentValue(currentPrice);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total profit for user
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalProfit(Integer userId) {
        log.info("Calculating total profit for user: {}", userId);
        
        List<MetalPurchase> purchases = metalPurchaseRepository.findByUserId(userId);
        
        return purchases.stream()
            .map(purchase -> {
                BigDecimal currentPrice = metalPriceService.getCurrentPrice(purchase.getMetalType());
                return purchase.calculateProfit(currentPrice);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get profitable investments for user
     */
    @Transactional(readOnly = true)
    public List<MetalPurchase> getProfitableInvestments(Integer userId) {
        log.info("Getting profitable investments for user: {}", userId);
        
        List<MetalPurchase> purchases = metalPurchaseRepository.findByUserId(userId);
        
        return purchases.stream()
            .filter(purchase -> {
                BigDecimal currentPrice = metalPriceService.getCurrentPrice(purchase.getMetalType());
                return purchase.isProfitable(currentPrice);
            })
            .collect(Collectors.toList());
    }
}
