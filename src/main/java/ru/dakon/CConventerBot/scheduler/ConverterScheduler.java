package ru.dakon.CConventerBot.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.dakon.CConventerBot.service.ConverterService;

@Service
@RequiredArgsConstructor
public class ConverterScheduler {
    private final ConverterService converterService;

    @Scheduled(fixedDelay = 86400000)
    public void getPrice() {
        converterService.getUsdToRub();
    }
}
