package model;

import io.swagger.client.model.LiftRide;

public class SkierLiftRideEvent {

  LiftRide liftRide;
  Integer resortID; // between 1 and 10
  String seasonID; //2024
  String dayID; //1

  Integer skierID; //between 1 and 100000

  public SkierLiftRideEvent(LiftRide liftRide, Integer resortID, String seasonID, String dayID,
      Integer skierID) {
    this.liftRide = liftRide;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
  }

  public SkierLiftRideEvent() {
  }

  public LiftRide getLiftRide() {
    return liftRide;
  }

  public void setLiftRide(LiftRide liftRide) {
    this.liftRide = liftRide;
  }

  public Integer getResortID() {
    return resortID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }

  @Override
  public String toString() {
    return "SkierLiftRideEvent{" +
        "liftRide=" + liftRide +
        ", resortID=" + resortID +
        ", seasonID=" + seasonID +
        ", dayID=" + dayID +
        ", skierID=" + skierID +
        '}';
  }
}
