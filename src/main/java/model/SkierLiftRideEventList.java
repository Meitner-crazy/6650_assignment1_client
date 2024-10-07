package model;

import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkierLiftRideEventList {
  private static final int MAX_SKIER_ID = 100000; //skierID - between 1 and 100000
  private static final int MAX_RESORT_ID = 10; //resortID - between 1 and 10
  private static final int MAX_LIFT_ID = 40; //liftID - between 1 and 40
  private static final String SEASON_ID = "2024";
  private static final String DAY_ID = "1";
  private static final int MAX_TIME = 360; //time - between 1 and 360
    List<SkierLiftRideEvent> skierLiftRideList = Collections.synchronizedList(new ArrayList<>());

  public SkierLiftRideEvent generator() {
      Random random = new Random();
//    List<SkierLiftRideEvent> skierLiftRideList = new ArrayList<>();
//      for(int i = 0; i < NUM_EVENTS; i++){
        Integer skierId = random.nextInt(MAX_SKIER_ID)+1;
        Integer liftId = random.nextInt(MAX_LIFT_ID)+1;
        Integer resortId = random.nextInt(MAX_RESORT_ID)+1;
        Integer time = random.nextInt(MAX_TIME)+1;
        LiftRide liftRide = new LiftRide();
        liftRide.setLiftID(liftId);
        liftRide.setTime(time);

//        SkierLiftRideEvent event = new SkierLiftRideEvent(liftId,time,resortId,SEASON_ID,DAY_ID,skierId);
        SkierLiftRideEvent event = new SkierLiftRideEvent(liftRide,resortId,SEASON_ID,DAY_ID,skierId);
        skierLiftRideList.add(event);
//      }
        return event;
//    skierLiftRideList.forEach(System.out::println); //print events for testing later

  }


}
