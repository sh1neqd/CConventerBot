package ru.dakon.CConventerBot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.dakon.CConventerBot.client.ExchangeClient;
import ru.dakon.CConventerBot.parser.Parser;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConverterService {
    private final Parser parser;
    private final JdbcTemplate jdbcTemplate;
    private final ExchangeClient exchangeClient;

    public void getUsdToRub() {
        log.info("Getting price from Api");
        String xmlFromApi = exchangeClient.getExchange();
        Double price = parser.parse(xmlFromApi);
        jdbcTemplate.update("UPDATE Price SET value=? WHERE id=?", price, 1);
    }

}
