package com.nature.finance.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nature.finance.model.Quota;
import org.springframework.stereotype.Service;
import unirest.HttpResponse;
import unirest.Unirest;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuotaHttp {

    private static final String[] QUOTAS = new String[]{
            "\"0.M.指数日行情_收盘指数.0\"",
            "\"0.M.指数日行情_加权平均市盈率.0\"",
            "\"0.M.指数日行情_加权市盈率分位点.0\"",
            "\"0.M.指数日行情_资权益回报率.0\"",
            "\"0.M.指数日行情_净利润增长.0\""
    };

    private static final String URI = "https://guorn.com/stock/query?_xsrf=2%7C8e11a520%7Cae454d04eaee4c5ddcc0cd7669b6e9b7%7C1628936376";
    private static final String USER = "2|1:0|10:1628338146|4:user|12:MTcyMjczMA==|d5bf85862cb5834abea42c146d0bcc16872422fedd1a1f75be4b40dbe1e70fb1";
    private static final String TOKEN = "2|1:0|10:1628338146|5:token|76:YmFmNzkwZGIwYmJkYjA5OTBiNDVlMTU5OWQ1MDgxZmM1Yzg5YTNlNzBjNzg1Mzg3Y2E4MTk2NmY=|a5ae673d74e45780dd50e9319373a0aa3f53d12eb37e26ea4a74144a7243d6c9";
    private static final String XSRF = "2|8e11a520|ae454d04eaee4c5ddcc0cd7669b6e9b7|1628936376";

    public List<Quota> listByCode(String code) {
        String req = this.convertReq(code);
        HttpResponse<String> response = Unirest.post(URI)
                .header("Cookie", "user=\"" + USER + "\"; token=\"" + TOKEN + "\"; _xsrf=" + XSRF + ";")
                .header("Content-Type", "application/json")
                .body(req)
                .asString();
        String body = response.getBody();
        return convertRes(code, body);
    }

    private ArrayList<Quota> convertRes(String code, String body) {
        JSONObject object = JSON.parseObject(body);
        JSONObject data = object.getJSONObject("data").getJSONObject("sheet_data");
        JSONArray columns = data.getJSONArray("meas_data");
        JSONArray dates = data.getJSONArray("row").getJSONObject(0).getJSONArray("data").getJSONArray(1);
        JSONArray profit = columns.getJSONArray(0);
        JSONArray peRatio = columns.getJSONArray(1);
        JSONArray roe = columns.getJSONArray(2);
        JSONArray price = columns.getJSONArray(3);
        JSONArray peTtm = columns.getJSONArray(4);
        ArrayList<Quota> quotas = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            Quota q = new Quota();
            String date = dates.getString(i);
            q.setId(String.join(":", code, date));
            q.setCode(code);
            q.setDate(date);
            q.setPrice(price.getDouble(i));
            q.setPeTtm(peTtm.getDouble(i));
            q.setPeRatio(peRatio.getDouble(i));
            q.setRoe(roe.getDouble(i));
            q.setProfit(profit.getDouble(i));
            quotas.add(q);
        }
        return quotas;
    }

    private String convertReq(String code) {
        StringBuilder builder = new StringBuilder("{\"index\":[");
        for (int i = 0; i < QUOTAS.length; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append("[\"").append(code).append("\",").append(QUOTAS[i]).append("]");
        }
        return builder.append("]}").toString();
    }

}
