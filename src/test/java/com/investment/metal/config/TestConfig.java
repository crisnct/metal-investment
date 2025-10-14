package com.investment.metal.config;

import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.BannedAccountsService;
import com.investment.metal.infrastructure.service.BlockedIpService;
import com.investment.metal.infrastructure.service.CurrencyService;
import com.investment.metal.infrastructure.service.EmailService;
import com.investment.metal.infrastructure.service.LoginService;
import com.investment.metal.infrastructure.service.RevolutService;
import com.investment.metal.infrastructure.service.price.ExternalMetalPriceReader;
import com.investment.metal.application.service.AlertService;
import com.investment.metal.application.service.MetalPriceService;
import com.investment.metal.application.service.NotificationService;
import com.investment.metal.application.service.PurchaseService;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.mapper.DtoMapper;
import com.investment.metal.infrastructure.mapper.MetalPurchaseMapper;
import com.investment.metal.infrastructure.service.MessageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for unit tests.
 * Provides mock beans for all dependencies to enable isolated testing.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AccountService accountService() {
        return mock(AccountService.class);
    }

    @Bean
    @Primary
    public BannedAccountsService bannedAccountsService() {
        return mock(BannedAccountsService.class);
    }

    @Bean
    @Primary
    public BlockedIpService blockedIpService() {
        return mock(BlockedIpService.class);
    }

    @Bean
    @Primary
    public LoginService loginService() {
        return mock(LoginService.class);
    }

    @Bean
    @Primary
    public PurchaseService purchaseService() {
        return mock(PurchaseService.class);
    }

    @Bean
    @Primary
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }

    @Bean
    @Primary
    public MetalPriceService metalPriceService() {
        return mock(MetalPriceService.class);
    }

    @Bean
    @Primary
    public AlertService alertService() {
        return mock(AlertService.class);
    }

    @Bean
    @Primary
    public CurrencyService currencyService() {
        return mock(CurrencyService.class);
    }

    @Bean
    @Primary
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    @Primary
    public RevolutService revolutService() {
        return mock(RevolutService.class);
    }

    @Bean
    @Primary
    public ExternalMetalPriceReader externalMetalPriceReader() {
        return mock(ExternalMetalPriceReader.class);
    }

    @Bean
    @Primary
    public ExceptionService exceptionService() {
        return mock(ExceptionService.class);
    }

    @Bean
    @Primary
    public DtoMapper dtoMapper() {
        return mock(DtoMapper.class);
    }

    @Bean
    @Primary
    public MetalPurchaseMapper metalPurchaseMapper() {
        return mock(MetalPurchaseMapper.class);
    }

    @Bean
    @Primary
    public MessageService messageService() {
        return mock(MessageService.class);
    }

    // PasswordEncoder is provided by SecurityConfig
}
