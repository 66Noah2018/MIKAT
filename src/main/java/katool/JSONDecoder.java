package katool;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rlvanbrummelen
 */
public class JSONDecoder {
    public static ChartItem decodeItem(String item){
//        ObjectMapper objectMapper = new ObjectMapper();
//        ChartItem chartItem = objectMapper.readValue(item, ChartItem.class);
        Pattern itemPattern = Pattern.compile(".+\"id\":\"(.+)\",\"type\":\"(.+)\",\"prevItemId\":\"(.+)\",\"caption\":\"(.+)\",\"condition\":(.+)}");
        Matcher itemMatcher = itemPattern.matcher(item);
        itemMatcher.find();

        return new ChartItem(itemMatcher.group(1), itemMatcher.group(2), itemMatcher.group(3), itemMatcher.group(4), itemMatcher.group(5).replace("\"", ""));
    }
    
    public static LinkedList<ChartItem> decodeChart(String encodedChart) throws JsonProcessingException{
        // assumption: well-formed JSON which contains ChartItems. Is JSONArray
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode chartList = (ArrayNode) objectMapper.readTree(encodedChart);
        LinkedList<ChartItem> chart = new LinkedList<>();
        chartList.forEach(item -> {
            chart.add(decodeItem(item.toString()));
        });
        return chart;
    }
}
