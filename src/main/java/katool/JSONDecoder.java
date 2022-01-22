package katool;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 *
 * @author rlvanbrummelen
 */
public class JSONDecoder {
    public static ChartItem decodeItem(String item) throws JsonProcessingException{
        ObjectMapper objectMapper = new ObjectMapper();
        ChartItem chartItem = objectMapper.readValue(item, ChartItem.class);

        return chartItem;
    }
    
    public static List<ChartItem> decodeChart(String encodedChart) throws JsonProcessingException{
        // assumption: well-formed JSON which contains ChartItems. Is JSONArray
        ObjectMapper objectMapper = new ObjectMapper();
        List<ChartItem> chart = objectMapper.readValue(encodedChart, new TypeReference<List<ChartItem>>(){});

        return chart;
    }
}
