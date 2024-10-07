package ResortsSkier;
import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;

import java.io.File;
import java.util.*;

public class ResortsApiExample {

  public static void main(String[] args) {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://localhost:8081/cs6650_lab2_war_exploded");
    ResortsApi apiInstance = new ResortsApi(apiClient);

//    ResortsApi apiInstance = new ResortsApi();
//    apiInstance.getApiClient().setBasePath("http://35.88.231.218:8080");
    ResortIDSeasonsBody body = new ResortIDSeasonsBody(); // ResortIDSeasonsBody | Specify new Season value
    Integer resortID = 5; // Integer | ID of the resort of interest
    try {
//      System.out.println("Season added successfully!");

      apiInstance.addSeason(body, resortID);
//      System.out.println("Season added successfully!");

    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#addSeason -- test");
//      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
//      e.printStackTrace();
    }
  }
}