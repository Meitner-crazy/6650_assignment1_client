package client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import model.*;

public class SkierLiftRideClient {
  private static final int NUM_THREAD = 32;
  private static final int NUM_POSTS_THREAD =  1000;
  private static final int NUM_EVENTS = 200000; // 200K POST requests
  private static final String BASE_PATH = "http://localhost:8080/cs6650_lab2_war_exploded";//local path
  private static final int MAX_RETRIES = 1;
  private static BlockingQueue<SkierLiftRideEvent> eventQueue = new LinkedBlockingQueue<>(300000);
  private static AtomicInteger successfulRequests = new AtomicInteger(0);

  private static AtomicInteger unsuccessfulRequests = new AtomicInteger(0);
  private static SkierLiftRideEventList eventList = new SkierLiftRideEventList();
  public static void main(String[] args) throws InterruptedException {
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
              sendPostRequest(skierLiftRide);
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
              sendPostRequest(skierLiftRide);
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
  }

  /**
   * Success:
   *  The server will return an HTTP 201 response code for a successful POST operation
   * Error:
   *         '201': description: Write successful
   *         '400': description: Invalid inputs
   *         '404': description: Data not found
   *
   * @param skierLiftRide
   */
  private static void sendPostRequest(SkierLiftRideEvent skierLiftRide) {

    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SkiersApi skiersApi = new SkiersApi(apiClient);
    int retries = 0;
    boolean completed = false;

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
    }

}
