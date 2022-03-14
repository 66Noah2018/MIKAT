/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
