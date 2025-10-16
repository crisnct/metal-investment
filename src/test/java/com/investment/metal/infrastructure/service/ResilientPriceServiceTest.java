package com.investment.metal.infrastructure.service;

import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.repository.MetalPriceRepository;
import com.investment.metal.infrastructure.service.price.ExternalMetalPriceReader;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResilientPriceService.
 * Tests circuit breaker functionality and fallback mechanisms.
 */
@ExtendWith(MockitoExtension.class)
class ResilientPriceServiceTest {

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private ExternalMetalPriceReader priceReader;

    @Mock
    private MetalPriceRepository metalPriceRepository;

    @InjectMocks
    private ResilientPriceService resilientPriceService;

    @Mock
    private PriceFallbackService fallbackService;

    private MetalType testMetalType;

    private final double expectedPrice = 2000.0;

    @BeforeEach
    void setUp() {
        testMetalType = MetalType.GOLD;
    }

    @Test
    void testFetchPrice_Success() {
        // Given
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.executeSupplier(any())).thenReturn(expectedPrice);

        // When
        double result = resilientPriceService.fetchPrice(testMetalType);

        // Then
        assertEquals(expectedPrice, result);
        verify(circuitBreaker).executeSupplier(any());
    }

    @Test
    void testFetchPrice_CircuitBreakerOpen_FallbackToCache() {
        // Given
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        RuntimeException circuitBreakerException = new RuntimeException("Circuit breaker is open");
        when(circuitBreaker.executeSupplier(any())).thenThrow(circuitBreakerException);
        when(fallbackService.hasCachedPrice(testMetalType)).thenReturn(true);
        when(fallbackService.getCachedPrice(testMetalType)).thenReturn(1950.0);
        when(fallbackService.getCachedPriceAge(testMetalType)).thenReturn(30000L);

        // When
        double result = resilientPriceService.fetchPrice(testMetalType);

        // Then
        assertEquals(1950.0, result);
        verify(fallbackService).hasCachedPrice(testMetalType);
        verify(fallbackService).getCachedPrice(testMetalType);
    }

    @Test
    void testFetchPrice_CircuitBreakerOpen_NoFallbackAvailable() {
        // Given
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        RuntimeException circuitBreakerException = new RuntimeException("Circuit breaker is open");
        when(circuitBreaker.executeSupplier(any())).thenThrow(circuitBreakerException);
        when(fallbackService.hasCachedPrice(testMetalType)).thenReturn(false);

        // When
        double result = resilientPriceService.fetchPrice(testMetalType);

        // Then
        assertEquals(0.0, result);
        verify(fallbackService).hasCachedPrice(testMetalType);
    }

    @Test
    void testGetCurrencyType() {
        // Given
        when(priceReader.getCurrencyType()).thenReturn(CurrencyType.USD);

        // When
        CurrencyType result = resilientPriceService.getCurrencyType();

        // Then
        assertEquals(CurrencyType.USD, result);
        verify(priceReader).getCurrencyType();
    }

    @Test
    void testIsCircuitBreakerOpen_WhenOpen() {
        // Given
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);

        // When
        boolean result = resilientPriceService.isCircuitBreakerOpen();

        // Then
        assertTrue(result);
    }

    @Test
    void testIsCircuitBreakerOpen_WhenClosed() {
        // Given
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);

        // When
        boolean result = resilientPriceService.isCircuitBreakerOpen();

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCircuitBreakerState() {
        // Given
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.HALF_OPEN);

        // When
        CircuitBreaker.State result = resilientPriceService.getCircuitBreakerState();

        // Then
        assertEquals(CircuitBreaker.State.HALF_OPEN, result);
    }
}
