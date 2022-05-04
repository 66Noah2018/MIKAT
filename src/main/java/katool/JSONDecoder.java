package katool;

/*
 * Copyright (C) 2022 RLvanBrummelen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
