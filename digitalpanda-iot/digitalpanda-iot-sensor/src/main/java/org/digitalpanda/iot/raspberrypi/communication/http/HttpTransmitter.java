package org.digitalpanda.iot.raspberrypi.communication.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.communication.MeasureTransmitter;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpTransmitter implements MeasureTransmitter {

    private final HttpClient httpClient;
    private final Gson gsonSerializer;
    private final String backendUrl;

    public HttpTransmitter(String backendUrl) throws Exception {
        this.backendUrl = backendUrl;
        this.gsonSerializer = (new GsonBuilder()).create();
        this.httpClient = new HttpClient();
        this.httpClient.start();
    }


    private void transmitToRestEndpoint(List<SensorMeasures> sensorData) {

    }

    @Override
    public void sendMeasures(List<SensorMeasures> sensorData) {
        try {
            String sensorDataJson = this.gsonSerializer.toJson(sensorData);
            ContentResponse response =
                    this.httpClient.POST(backendUrl)
                            .header(HttpHeader.CONTENT_TYPE, "application/json")
                            .content(new StringContentProvider(sensorDataJson))
                            .timeout(1000L, TimeUnit.MILLISECONDS)
                            .send();
            if(response.getStatus() != HttpStatus.Code.OK.getCode() ){
                System.out.println("response status code = " + response.getStatus());
                System.out.println("message = " + response.getContentAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
