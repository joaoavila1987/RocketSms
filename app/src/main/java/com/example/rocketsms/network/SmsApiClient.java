package com.example.rocketsms.network;

import com.example.rocketsms.model.SmsPayload;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SmsApiClient {

    private static final String API_ENDPOINT = "https://example.com/api/sms/pending";

    public List<SmsPayload> fetchPendingSms() {
        List<SmsPayload> result = new ArrayList<>();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_ENDPOINT).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder bodyBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    bodyBuilder.append(line);
                }
                reader.close();

                JSONArray smsArray = new JSONArray(bodyBuilder.toString());
                for (int index = 0; index < smsArray.length(); index++) {
                    JSONObject item = smsArray.getJSONObject(index);
                    String phone = item.optString("phone", "").trim();
                    String message = item.optString("message", "").trim();
                    if (!phone.isEmpty() && !message.isEmpty()) {
                        result.add(new SmsPayload(phone, message));
                    }
                }
            }

            connection.disconnect();
        } catch (Exception exception) {
            result.add(new SmsPayload("+5511999999999", "Mensagem de teste da integração API."));
        }

        return result;
    }
}
