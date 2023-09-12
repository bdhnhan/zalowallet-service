package com.zalopay.zalowallet.data;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallBackResponse {
    private String transId;
    private String status;

    public static String generateJsonString(String transId, String status) {
        CallBackResponse callBackResponse = new CallBackResponse(transId, status);
        return new Gson().toJson(callBackResponse);
    }
}
