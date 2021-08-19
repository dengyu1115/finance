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
import java.util.function.BiConsumer;

@Service
public class QuotaHttp {

    private static final String[] QUOTAS = new String[]{
            "收盘指数",
            "加权平均市盈率",
            "加权市盈率分位点",
            "资权益回报率",
            "净利润增长"
    };

    private static final List<BiConsumer<Quota, Double>> CS = new ArrayList<>();

    static {
        CS.add(Quota::setPrice);
        CS.add(Quota::setPeTtm);
        CS.add(Quota::setPeRatio);
        CS.add(Quota::setRoe);
        CS.add(Quota::setProfit);
    }


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
        JSONObject data = JSON.parseObject(body).getJSONObject("data");
        JSONObject sheet = data.getJSONObject("sheet_data");
        JSONArray dates = sheet.getJSONArray("row").getJSONObject(0).getJSONArray("data").getJSONArray(1);
        ArrayList<Quota> quotas = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            Quota q = new Quota();
            String date = dates.getString(i);
            q.setId(String.join(":", code, date));
            q.setCode(code);
            q.setDate(date);
            quotas.add(q);
        }
        List<Integer> idx = this.idx(data.getJSONObject("sheet_defn").getJSONObject("layout").getJSONObject("chart")
                .getJSONArray("column").getJSONArray(0));
        JSONArray columns = sheet.getJSONArray("meas_data");
        for (int i = 0; i < idx.size(); i++) {
            JSONArray array = columns.getJSONArray(i);
            BiConsumer<Quota, Double> consumer = CS.get(idx.get(i));
            for (int j = 0; j < quotas.size(); j++) {
                Quota q = quotas.get(j);
                consumer.accept(q, array.getDouble(j));
            }
        }

        return quotas;
    }

    private List<Integer> idx(JSONArray array) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            String name = array.getString(i).replace("1722730.M.index", "");
            idx.add((int) Double.parseDouble(name));
        }
        return idx;
    }

    private String convertReq(String code) {
        StringBuilder builder = new StringBuilder("{\"index\":[");
        for (int i = 0; i < QUOTAS.length; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append("[\"").append(code).append("\",")
                    .append("\"0.M.指数日行情_").append(QUOTAS[i]).append(".0\"").append("]");
        }
        return builder.append("]}").toString();
    }

}
