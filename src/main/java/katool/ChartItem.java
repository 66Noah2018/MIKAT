package katool;

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
