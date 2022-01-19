/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.tools.javac.util.Iterators;
import java.util.Iterator;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 *
 * @author rlvanbrummelen
 */
public class JSONDecoder {
    public static ChartItem decodeItem(JSONObject item){
        ChartItem chartItem = null;
        try{
            chartItem =  new ChartItem(item.getString("id"), item.getString("type"), item.getString("prevItemId"), item.getString("nextItemId"), item.getString("caption"));
        }
        catch (Exception e){
            System.out.println("Cannot convert item to ChartItem");
            e.printStackTrace();
        }
        return chartItem;
    }
    
    public static ChartItem[] decodeChart(JSONObject encodedChart){
        Iterator<String> keys = encodedChart.keys().reverse();
        ChartItem[] chart = new ChartItem[Iterators.size(keys)];
        while (keys.hasNext()){
            String key = keys.next();
            if (encodedChart.get(key) instanceOf JSONObject){
                chart.append(decodeItem(encodedChart.get(key)));
            }
        }
    }
}
