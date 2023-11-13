package com.example.multithreadconcepts.processor;

import com.example.multithreadconcepts.model.Post;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
public class PostDataProcessor implements ApplicationRunner {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(PostDataProcessor.class);
    private static final String POST_API_URL = "https://jsonplaceholder.typicode.com/posts/";
    private static final String POST_UPDATE_SQL = "INSERT INTO POST (ID, USER_ID, TITLE, BODY) VALUES (?,?,?,?) ";
    private int BATCH_SIZE = 50;
    private CountDownLatch countDownLatch;
    private ExecutorService executorService;
    private List<Post> postList;

    @PostConstruct
    public void init(){
        countDownLatch = new CountDownLatch(BATCH_SIZE);
        executorService = Executors.newFixedThreadPool(5);
        postList = new ArrayList<>();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IntStream.range(1,51).forEach(i -> executorService.execute(task(i)));
        countDownLatch.await();  // main thread waits for the latch count to be zero before proceeding further
        LOGGER.info("Received data for 50 posts");
        batchUpdate();
        LOGGER.info("Posts data saved successfully");
    }

    private Runnable task(int id) {
        return () -> {
            Post post = restTemplate.getForObject(POST_API_URL + id, Post.class);
            postList.add(post);
            LOGGER.info("Received data for post id {}",id);
            countDownLatch.countDown();
        };
    }

    private BatchPreparedStatementSetter batchPreparedStatementSetter(){
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, postList.get(i).getId());
                ps.setInt(2, postList.get(i).getUserId());
                ps.setString(3, postList.get(i).getTitle());
                ps.setString(4, postList.get(i).getBody());
            }
            @Override
            public int getBatchSize() {
                return BATCH_SIZE;
            }
        };
    }

    private void batchUpdate(){
        try {
            jdbcTemplate.batchUpdate(POST_UPDATE_SQL,batchPreparedStatementSetter());
        } catch (Exception e) {
            LOGGER.error("Error in batch update {} ",e);
        }
    }

}
