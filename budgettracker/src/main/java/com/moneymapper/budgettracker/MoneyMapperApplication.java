package com.moneymapper.budgettracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MoneyMapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyMapperApplication.class, args);
	}

}
