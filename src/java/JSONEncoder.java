/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.json.simple.JSONObject;

/**
 *
 * @author rlvanbrummelen
 */
public class JSONEncoder {
    public static JSONObject encodeItem(ChartItem item) {
        JSONObject obj = new JSONObject();
        
        obj.put("id", item.getId());
        obj.put("type", item.getType());
        obj.put("prevItemId", item.getPrevItemId());
        obj.put("nextItemId", item.getNextItemId());
        obj.put("caption", item.getCaption());
        
        return obj;
    }

    public static JSONObject encodeChart(ChartItem[] chart) {
        JSONObject encodedChart = new JSONObject();
        
        for (ChartItem item : chart){
            encodedChart.put(item.getId(), encodeItem(item));
        }
        
        return encodedChart;
    }
}
