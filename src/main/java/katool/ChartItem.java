package katool;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rlvanbrummelen
 */
public class ChartItem {
    private String id;
    private String type;
    private String prevItemId;
    private String caption;
    private String condition;
    
    public ChartItem(String id, String type, String prevItemId, String caption){
        this.id = id;
        this.type = type;
        this.prevItemId = prevItemId;
        this.caption = caption;
        this.condition = null;
    }
    
    public ChartItem(String id, String type, String prevItemId, String caption, String condition){
        this.id = id;
        this.type = type;
        this.prevItemId = prevItemId;
        this.caption = caption;
        this.condition = condition;
    }
    
    public String getId(){ return this.id; }
    public String getType() { return this.type; }
    public String getPrevItemId() { return this.prevItemId; }
    public String getCaption() { return this.caption; }
    public String getCondition() { return this.condition; }
    
    public void setPrevItemId(String id) { this.prevItemId = id; }
    public void setCaption(String caption) { this.caption = caption; }
    public void setCondition(String condition) { this.condition = condition; }
}
