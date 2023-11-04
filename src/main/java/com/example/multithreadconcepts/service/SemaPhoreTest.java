package com.example.multithreadconcepts.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

@Service
public class SemaPhoreTest implements CommandLineRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(SemaPhoreTest.class);

    @Autowired
    private RestTemplate restTemplate;

    private Semaphore semaphore;

    private final String API_URL = "https://jsonplaceholder.typicode.com/posts/";

    private  ExecutorService executorService;

    @PostConstruct()
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
        semaphore = new Semaphore(2);
    }

    @Override
    public void run(String... args) throws Exception {
        IntStream.range(0,100).forEach(i -> executorService.execute(runnable(i)));
        executorService.shutdown();
    }

    public Runnable runnable(int i) {
        return () -> {
            try {
                semaphore.acquire(1); // get a permit
                LOGGER.info("Api result for post {} : {} ", i, restTemplate.getForObject(API_URL + i, String.class));
                Thread.sleep(5000);    // simulating a delay
                semaphore.release();         // release the permit
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }


}
