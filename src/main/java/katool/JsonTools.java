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
package katool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.javatuples.Pair;

/**
 *
 * @author RLvan
 */
public final class JsonTools {
    public static JsonNode addItem(JsonNode node, String key, String value){
        ObjectNode newNode = ((ObjectNode)node).put(key, value);
        return (JsonNode)newNode;
    }
    
    public static JsonNode removeItem(JsonNode node, String key){
        return ((ObjectNode)node).remove(key);
    }
    
    public static JsonNode updateItem(JsonNode node, String key, String newValue){
        JsonNode newItem = new TextNode(newValue);
        return ((ObjectNode)node).set(key, newItem);
    }    
    
    public static JsonNode createNode(List<Pair<String,String>> items){
        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        for (Pair<String, String> item: items){
            newNode = newNode.put(item.getValue0(), item.getValue1());
        }
        return (JsonNode) newNode;
    }
}
