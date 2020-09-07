package com.investment.metal.service;

import com.investment.metal.MetalType;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.stereotype.Service;

@Service
public interface ExternalMetalPriceService {

    double fetchPrice(MetalType metalType) throws BusinessException;

}
