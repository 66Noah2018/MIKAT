package katool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rlvanbrummelen
 */
public class JSONEncoder {
    public static String encodeItem(ChartItem item) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(item);
        
        return result;
    }

    public static String encodeChart(LinkedList<ChartItem> chart) throws JsonProcessingException {
        List<ChartItem> chartList = new ArrayList();
        chart.forEach(item -> {
            chartList.add(item);
        });
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(chartList);
        
        return result;
    }
}
