package ru.dakon.CConventerBot.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="usdtorub", url="${apilink}", configuration = FeignConfig.class)
public interface ExchangeClient {
    @GetMapping
    String getExchange();
}
