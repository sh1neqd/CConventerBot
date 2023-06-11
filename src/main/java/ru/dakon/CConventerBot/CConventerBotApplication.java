package ru.dakon.CConventerBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@PropertySource("application.properties")
public class CConventerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CConventerBotApplication.class, args);
	}

}
