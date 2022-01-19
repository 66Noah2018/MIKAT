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
    private String nextItemId;
    private String caption;
    
    public ChartItem(String id, String type, String prevItemId, String nextItemId, String caption){
        this.id = id;
        this.type = type;
        this.prevItemId = prevItemId;
        this.nextItemId = nextItemId;
        this.caption = caption;
    }
    
    public String getId(){ return this.id; }
    public String getType() { return this.type; }
    public String getPrevItemId() { return this.prevItemId; }
    public String getNextItemId() { return this.nextItemId; }
    public String getCaption() { return this.caption; }
    
    public void setPrevItemId(String id) { this.prevItemId = id; }
    public void setNextItemid(String id) { this.nextItemId = id; }
    public void setCaption(String caption) { this.caption = caption; }
}
