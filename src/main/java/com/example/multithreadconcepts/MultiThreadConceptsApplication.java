package com.example.multithreadconcepts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MultiThreadConceptsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiThreadConceptsApplication.class, args).close();
	}

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}
