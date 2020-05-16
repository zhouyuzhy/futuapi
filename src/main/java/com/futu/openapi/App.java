package com.futu.openapi;

import com.futu.openapi.api.FutuApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan("com.futu.openapi")
public class App
{
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public FutuApi genFutuApi()
	{
		return new FutuApi();
	}
}
