package com.investment.metal.infrastructure.controller;

import com.investment.metal.application.service.MetalInvestmentApplicationService;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.application.dto.UserMetalInfoDto;
import com.investment.metal.domain.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for metal investment endpoints.
 * Follows Clean Architecture principles by delegating to application services.
 * Handles only HTTP concerns, not business logic.
 */
@Slf4j
@RestController
@RequestMapping("/api/investment")
@RequiredArgsConstructor
@Tag(name = "Metal Investment", description = "Metal investment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MetalInvestmentController {

    private final MetalInvestmentApplicationService investmentService;

    @PostMapping("/purchase")
    @Operation(
        summary = "Add metal purchase",
        description = "Add a new metal purchase for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Purchase added successfully",
            content = @Content(schema = @Schema(implementation = MetalPurchase.class))),
        @ApiResponse(responseCode = "400", description = "Invalid purchase data",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> addPurchase(
        @Parameter(description = "Metal symbol (e.g., GOLD, SILVER)", required = true)
        @RequestHeader("metalSymbol") String metalSymbol,
        @Parameter(description = "Amount of metal purchased", required = true)
        @RequestHeader("amount") BigDecimal amount,
        @Parameter(description = "Cost of the purchase", required = true)
        @RequestHeader("cost") BigDecimal cost,
        @Parameter(description = "User ID", required = true)
        @RequestHeader("userId") Integer userId) {
        
        try {
            MetalPurchase purchase = investmentService.addMetalPurchase(userId, metalSymbol, amount, cost);
            return ResponseEntity.ok(purchase);
            
        } catch (BusinessException e) {
            log.warn("Failed to add purchase for user: {}, reason: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error adding purchase for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }

    @GetMapping("/portfolio")
    @Operation(
        summary = "Get user portfolio",
        description = "Get user's metal investment portfolio information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserMetalInfoDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> getPortfolio(
        @Parameter(description = "User ID", required = true)
        @RequestHeader("userId") Integer userId) {
        
        try {
            List<UserMetalInfoDto> portfolio = investmentService.getUserMetalInfo(userId);
            return ResponseEntity.ok(portfolio);
            
        } catch (Exception e) {
            log.error("Unexpected error getting portfolio for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }

    @GetMapping("/portfolio/value")
    @Operation(
        summary = "Get total portfolio value",
        description = "Get total current value of user's portfolio"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio value retrieved successfully",
            content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> getTotalPortfolioValue(
        @Parameter(description = "User ID", required = true)
        @RequestHeader("userId") Integer userId) {
        
        try {
            BigDecimal totalValue = investmentService.calculateTotalPortfolioValue(userId);
            return ResponseEntity.ok(totalValue);
            
        } catch (Exception e) {
            log.error("Unexpected error calculating portfolio value for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }

    @GetMapping("/portfolio/profit")
    @Operation(
        summary = "Get total profit",
        description = "Get total profit/loss of user's portfolio"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total profit retrieved successfully",
            content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> getTotalProfit(
        @Parameter(description = "User ID", required = true)
        @RequestHeader("userId") Integer userId) {
        
        try {
            BigDecimal totalProfit = investmentService.calculateTotalProfit(userId);
            return ResponseEntity.ok(totalProfit);
            
        } catch (Exception e) {
            log.error("Unexpected error calculating total profit for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }

    @GetMapping("/portfolio/profitable")
    @Operation(
        summary = "Get profitable investments",
        description = "Get user's profitable metal investments"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profitable investments retrieved successfully",
            content = @Content(schema = @Schema(implementation = MetalPurchase.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> getProfitableInvestments(
        @Parameter(description = "User ID", required = true)
        @RequestHeader("userId") Integer userId) {
        
        try {
            List<MetalPurchase> profitableInvestments = investmentService.getProfitableInvestments(userId);
            return ResponseEntity.ok(profitableInvestments);
            
        } catch (Exception e) {
            log.error("Unexpected error getting profitable investments for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }
}
