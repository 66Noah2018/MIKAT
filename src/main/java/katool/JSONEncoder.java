package katool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*
 * Copyright (C) 2022 Amsterdam Universitair Medische Centra
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
