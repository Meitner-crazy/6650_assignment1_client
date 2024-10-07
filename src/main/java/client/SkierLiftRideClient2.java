package client;


import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import model.*;

public class SkierLiftRideClient2 {
  private static final int NUM_THREAD = 32;
  private static final int NUM_POSTS_THREAD =  1000;
  private static final int NUM_EVENTS = 200000; // 200K POST requests
  private static final String BASE_PATH = "http://localhost:8080/cs6650_lab2_war_exploded";//local path
  private static final int MAX_RETRIES = 1;
  private static BlockingQueue<SkierLiftRideEvent> eventQueue = new LinkedBlockingQueue<>(300000);
  private static AtomicInteger successfulRequests = new AtomicInteger(0);

  private static AtomicInteger unsuccessfulRequests = new AtomicInteger(0);
  private static SkierLiftRideEventList eventList = new SkierLiftRideEventList();

  private static List<Long> latencies = Collections.synchronizedList(new ArrayList<>()); //latencies
  public static void main(String[] args) throws InterruptedException, IOException {
    CountDownLatch latch = new CountDownLatch(NUM_THREAD);
    long startTime = System.currentTimeMillis();
    //generate 200000 liftRides in a single thread first
    new Thread(() -> {
      for (int i = 0; i < NUM_EVENTS; i++){
        try{
          SkierLiftRideEvent skierLiftRide = eventList.generator();
          eventQueue.add(skierLiftRide);
        }catch (Exception e){
          System.out.println("Caught exception while generating liftRides: " + e.getMessage());
        }

      }
    }).start();

    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);

    //32 * 1000 = 32000
    for (int i = 0; i < NUM_THREAD; i++) {
      executor.execute(() -> {
        try {
          for (int j = 0; j < NUM_POSTS_THREAD; j++) {
            SkierLiftRideEvent skierLiftRide = eventQueue.poll();
            if (skierLiftRide != null) {
              sendPostRequest2(skierLiftRide);
            }
          }
        } catch (Exception e) {
          System.out.println("Error: failed to send post requests. " + e.getMessage());
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();
    executor.shutdown();


    int remainingEvents = NUM_EVENTS - (NUM_THREAD * NUM_POSTS_THREAD);
    if (remainingEvents > 0) {
      int additionalThreads = remainingEvents / NUM_POSTS_THREAD;
      CountDownLatch extraLatch = new CountDownLatch(additionalThreads);
      ExecutorService additionalExecutor = Executors.newFixedThreadPool(additionalThreads);

      for (int i = 0; i < additionalThreads; i++) {
        additionalExecutor.execute(() -> {
          try {
            for (int j = 0; j < NUM_POSTS_THREAD; j++) {
              SkierLiftRideEvent skierLiftRide = eventQueue.poll();
              if (skierLiftRide != null) {
                sendPostRequest2(skierLiftRide);
              }
            }
          } catch (Exception e) {
            System.out.println("Error: failed to send additional post requests. " + e.getMessage());
          } finally {
            extraLatch.countDown();
          }
        });
      }

      extraLatch.await();
      additionalExecutor.shutdown();
    }
    long endTime = System.currentTimeMillis();
    long wallTime = endTime - startTime;
    long throughput = NUM_EVENTS/wallTime;
    System.out.println("Number of successful requests sent: " + successfulRequests);
    System.out.println("Number of unsuccessful requests sent: " + unsuccessfulRequests);
    System.out.println("Posts were sent! Time taken: " + wallTime + "ms");
    System.out.println("Total throughput is: " + throughput + " requests per second");

    System.out.println("--------Calculation Part-----------");
    calculateMetrics(wallTime);

  }

  private static void sendPostRequest2(SkierLiftRideEvent skierLiftRide) {

    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SkiersApi skiersApi = new SkiersApi(apiClient);
    int retries = 0;
    boolean completed = false;
    int responseCode = 0;

    long postStart = System.currentTimeMillis();

    while (retries < MAX_RETRIES && !completed) {
      try{
        ApiResponse<Void> response = skiersApi.writeNewLiftRideWithHttpInfo(skierLiftRide.getLiftRide(),skierLiftRide.getResortID(),skierLiftRide.getSeasonID(),skierLiftRide.getDayID(),skierLiftRide.getSkierID());
        completed = true;
      } catch (Exception e) {
        retries++;
        System.out.println("Error message: " + e.getMessage());
      }
    }
    if(completed){
      successfulRequests.getAndIncrement();
    }else{
      unsuccessfulRequests.getAndIncrement();
    }
    long postEnd = System.currentTimeMillis();
    long latency =postEnd-postStart;
    latencies.add(latency);

    try (FileWriter writer = new FileWriter("responses.csv", true)) {
      writer.append(String.format("%d,%s,%d,%d\n", postStart, "POST", latency, responseCode));
    } catch (IOException e) {
      System.out.println("Failed to write to CSV file: " + e.getMessage());
    }
    System.out.println("Latency for each request is : " + latency + " ms.");
  }

  private static void calculateMetrics(long wallTime) {
    long sum = latencies.stream().mapToLong(Long::longValue).sum();
    double mean = sum / (double) latencies.size();

    Collections.sort(latencies);
    double median = latencies.size() % 2 == 0 ?
        (latencies.get(latencies.size()/2) + latencies.get(latencies.size()/2 - 1)) / 2.0 :
        latencies.get(latencies.size()/2);

    double p99 = latencies.get((int)(latencies.size() * 0.99));
    long min = Collections.min(latencies);
    long max = Collections.max(latencies);
    double throughput = (double) NUM_EVENTS / wallTime;

    System.out.println("Mean response time: " + mean + " ms");
    System.out.println("Median response time: " + median + " ms");
    System.out.println("99th percentile response time: " + p99 + " ms");
    System.out.println("Min response time: " + min + " ms");
    System.out.println("Max response time: " + max + " ms");
    System.out.println("Throughput: " + throughput + " requests per second");
  }


}
