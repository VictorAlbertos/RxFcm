package victoralbertos.io.rxfcm.data.api;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by victor on 08/02/16.
 */
public interface ApiFcmServer {
    String URL_BASE = "https://fcm.googleapis.com";
    String API_KEY = "Authorization: key=AIzaSyCfbpy8aavUlXtnfe5yG43xG1HU6tjqQ-w";
    String CONTENT_TYPE = "Content-Type: application/json";

    @Headers({API_KEY, CONTENT_TYPE})
    @POST("/fcm/send") Observable<Response<FcmServerService.FcmResponseServer>> sendNotification(@Body FcmServerService.Payload payload);
}