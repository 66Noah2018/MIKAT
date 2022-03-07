/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;

/**
 *
 * @author RLvan
 */
public class chartservlet extends HttpServlet {
    
    // !!!! change directory! ask for working dir!

    private Pair<LinkedList<ChartItem>, ArrayList<String>> currentState = new Pair(new LinkedList<>(), new ArrayList<>());
    private Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> undoStack = new LinkedList<>();
    private Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> redoStack = new LinkedList<>();
    private final Integer MAX_DEQUE_SIZE = 10;
    private String workingDir = "C:\\Users\\RLvan\\OneDrive\\Documenten\\MI\\SRP";
    private final String settingsFileName = "mikat_settings.json";
    private JsonNode settings = null; // {prevOpened:[{},{},{}]}
    private ArrayList<JsonNode> dependencies = null; // [{dependency: ..., fileLocation: ..., date: ...},{}]
    private ArrayList<Pair<String, JsonNode>> loadedDependencies = null;
    private String conditionalId = null;
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        switch(request.getParameter("function")){
            case "update":
                updateState(request);
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + "}");
                break;
            case "undo":
                Integer undoSize = undo();
                response.getWriter().write("{\"state\": " + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + ", \"size\":" + undoSize + "}");
                break;
            case "redo":
                Integer redoSize = redo();
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + ", \"size\":" + redoSize + "}");
                break;
            case "undoSize":
                response.getWriter().write("{\"size\":" + undoStack.size() + "}");
                break;                
            case "redoSize":
                response.getWriter().write("{\"size\":" + redoStack.size() + "}");
                break;
            case "localmap":
                String localMapping = getLocalMapping();
                response.getWriter().write(localMapping);
                break;
            case "standardmap":
                String standardizedMapping = getStandardizedMapping();
                response.getWriter().write(standardizedMapping);
                break;
            case "delete":
                deleteItem(request);
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + "}");
                break;
            case "file":
                String mlmname = selectFile(request);
                response.getWriter().write("{\"mlmname\":\"" + mlmname + "\"}");
                break;
            case "open":
                openProject(request);
                break;
            case "save":
                saveProject(request);
                break;
            case "state":
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + "}");
                break;
            case "getElement":
                ChartItem element = getElementById(request);
                response.getWriter().write("{\"chartItem\":" + JSONEncoder.encodeItem(element) + "}");
                break;
            case "endline":
                updateEndlineList(request);
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + "}");
                break;
            case "hasNext":
                Boolean result = itemHasNext(request);
                response.getWriter().write("{\"hasNext\":" + result + "}");
                break;
            case "getConditionalActions":
                LinkedList<ChartItem> nextItems = getConditionalItems(request);
                response.getWriter().write("{\"items\":" + JSONEncoder.encodeChart(nextItems) + "}");
            default:
                break;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private LinkedList<ChartItem> getConditionalItems(HttpServletRequest request){
        String id = request.getParameter("id");
        LinkedList<ChartItem> nextItems = new LinkedList<>();
        for (ChartItem element : currentState.getValue0()) {
            if (id.equals(element.getPrevItemId())) { nextItems.add(element); }
        }
        return nextItems;
    }
    
    private Boolean itemHasNext(HttpServletRequest request){
        String id = request.getParameter("id");
        Boolean nextItem = false;
        for (ChartItem element : currentState.getValue0()) {
            if (id.equals(element.getPrevItemId())) { nextItem = true; }
        }
        return nextItem;
    }
    
    private Boolean nextIsEnd(String id){
        ChartItem nextItem = null;
        for (ChartItem element : currentState.getValue0()) {
            if (id.equals(element.getPrevItemId())) { nextItem = element; }
        }
        if (nextItem != null) {
            return nextItem.getType().equals("end");
        } else { return false; }
    }
    
    private void updateEndlineList(HttpServletRequest request){
        String id = request.getParameter("id");
        if (!currentState.getValue1().contains(id)) {
            maintainMaxDequeSize("undo");
            undoStack.addFirst(deepCopyCurrentState(currentState));
            currentState.getValue1().add(id);
        }
    }
    
    private void removeEndline(String id) {
        if (currentState.getValue1().contains(id)){
            maintainMaxDequeSize("undo");
            undoStack.addFirst(deepCopyCurrentState(currentState));
            currentState.getValue1().remove(id);
        }
    }
    
    private ChartItem getElementById(HttpServletRequest request){
        String id = request.getParameter("id");
        ChartItem element = null;
        for (ChartItem chartItem : currentState.getValue0()){
            if (chartItem.getId().equals(id)) { 
                element = chartItem;
                break;
            }
        }
        return element;
    }
    
    private boolean checkFileValidity(JsonNode project){
        // check whether file was created by this program and contains the correct 'keys'
        JsonNode maintenance = project.get("maintenance");
        JsonNode library = project.get("library");
        JsonNode knowledge = project.get("knowledge");
        JsonNode projectDependencies = project.get("dependencies");
        if (maintenance == null || library == null || knowledge == null || projectDependencies == null) { return false; }
        if (!maintenance.isNull()){
            if (maintenance.hasNonNull("title") && 
                    maintenance.hasNonNull("mlmname") &&
                    maintenance.hasNonNull("arden") &&
                    maintenance.hasNonNull("version") &&
                    maintenance.hasNonNull("institution") &&
                    maintenance.hasNonNull("author") &&
                    maintenance.hasNonNull("specialist") &&
                    maintenance.hasNonNull("date") &&
                    maintenance.hasNonNull("validation")){
                if (!"production".equals(maintenance.get("validation").asText()) &&
                        !"research".equals(maintenance.get("validation").asText()) &&
                        !"testing".equals(maintenance.get("validation").asText()) &&
                        !"expired".equals(maintenance.get("validation").asText())) {
                    return false;
                } else { // if we get here the maintenance section is complete
                    if (!library.isNull()){
                        if (library.hasNonNull("purpose") &&
                                library.hasNonNull("explanation") &&
                                library.hasNonNull("keywords")){
                            if (!knowledge.isNull()) {
                                if (knowledge.hasNonNull("type") &&
                                        knowledge.hasNonNull("data") &&
                                        knowledge.hasNonNull("evoke") &&
                                        knowledge.hasNonNull("logic") &&
                                        knowledge.hasNonNull("action")){
                                    // we made it through the MLM required fields
                                    // from now on: fields that are required by MIKAT and mark this project as a MIKAT project
                                    if (!projectDependencies.isNull()) { // minimum is an empty JsonArray
                                        return true;
                                    } else { return false; }
                                } else { return false; }
                            } else { return false; }
                        } else { return false; }
                    } else { return false; }
                }
            } else { return false; }
        }
        return false;
    }
    
    private void saveProject(HttpServletRequest request){
        String projectFile = request.getParameter("fileLocation");
        
    }
    
    private void loadSettings() throws FileNotFoundException, JsonProcessingException{
        File root = new File("C:\\");
        File settingsFile = null;
        String settingsString = "";
        try{
            boolean recursive = true;
            Collection files = FileUtils.listFiles(root, null, recursive);
            for (Iterator iterator = files.iterator(); iterator.hasNext();){
                File file = (File) iterator.next();
                if (file.getName().equals(settingsFileName)){
                    settingsFile = file;
                }
            }
        } catch (Exception e) {
            // create new settings file
        }
        
        Scanner fileReader = new Scanner(settingsFile);
        while (fileReader.hasNextLine()){
            settingsString += fileReader.nextLine();   
        }
        ObjectMapper mapper = new ObjectMapper();
        settings = mapper.readTree(settingsString);
    }
    
    private void openProject(HttpServletRequest request) throws IOException{ //open project
        loadSettings();
        String fileName = request.getParameter("fileName");
        String path = null;
        File projectFile = null;
        String projectString = "";
        JsonNode project = null;
        if (!fileName.startsWith("C:\\")){
            File root = new File(workingDir);
            try{
                boolean recursive = true;
                Collection files = FileUtils.listFiles(root, null, recursive);
                for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                    File file = (File) iterator.next();
                    if (file.getName().equals(fileName)){
                        path = file.getAbsolutePath();
                        projectFile = file;
                    }
                } 
            }catch (Exception e){}
        } else {
            path = fileName;
            projectFile = new File(path); 
        }
        
        Scanner fileReader = new Scanner(projectFile);
        while (fileReader.hasNextLine()){
            projectString += fileReader.nextLine();   
        }
        ObjectMapper mapper = new ObjectMapper();
        project = mapper.readTree(projectString);
        if (checkFileValidity(project)){
            dependencies.add(project.get("dependencies"));
            settings = project.get("settings");
            currentState = currentState.setAt0(JSONDecoder.decodeChart(project.get("state").toString()));
        }
        
    }
    
    private String selectFile(HttpServletRequest request) throws IOException{
        String fileName = request.getParameter("name");
        File root = new File(workingDir);
        String path = null;
        File selectedFile = null;
        String selectedFileString = "";
        JsonNode selectedFileContents = null;
        String mlmname = null;
        try {
            boolean recursive = true;
            Collection files = FileUtils.listFiles(root, null, recursive);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName)){
                    path = file.getAbsolutePath();
                    selectedFile = file;
                    Scanner fileReader = new Scanner(selectedFile);
                    while (fileReader.hasNextLine()){
                        selectedFileString += fileReader.nextLine();
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    selectedFileContents = mapper.readTree(selectedFileString);
                    if (checkFileValidity(selectedFileContents)){
                        String title = selectedFileContents.at("/maintenance/title").asText();
                        mlmname = selectedFileContents.at("/maintenance/mlmname").asText();
                        List<Pair<String,String>> newItems = new ArrayList<>();
                        newItems.add(new Pair<>("dependency", title));
                        newItems.add(new Pair<>("fileLocation", path));
                        newItems.add(new Pair<>("date", new Date().toString()));
                        JsonNode newDependency = JsonTools.createNode(newItems);
                        dependencies.add(newDependency);
                        loadedDependencies.add(new Pair<>(title, selectedFileContents));
                    }
                }
            } 
        } catch (Exception e){}  
        return mlmname;
    }
    
    private void removeConditional(String id){
        Integer itemIndex = findIndexOf(id);
        currentState.getValue0().remove(currentState.getValue0().get(itemIndex));
        for (Iterator<ChartItem> iterator = currentState.getValue0().iterator(); iterator.hasNext(); ) {
            ChartItem item = iterator.next();
            if (item.getPrevItemId().equals(id)) { iterator.remove(); }
            removeEndline(item.getId());
        }
        removeEndline(id);
    }
    
    private void deleteItem(HttpServletRequest request) throws IOException{
        String id = request.getParameter("id");
        Integer itemIndex = findIndexOf(id);
        maintainMaxDequeSize("undo");
        undoStack.addFirst(deepCopyCurrentState(currentState));
        ChartItem oldItem = currentState.getValue0().get(itemIndex);
        if (oldItem.getType().equals("conditional")) { removeConditional(oldItem.getId()); }
        else { 
            currentState.getValue0().remove(oldItem);
        }
        
        Integer nextIndex = nextElementIndex(id);
        if (nextIndex > -1){
            ChartItem itemToBeAltered = currentState.getValue0().get(nextIndex);
            itemToBeAltered.setPrevItemId(oldItem.getPrevItemId());
            currentState.getValue0().set(nextIndex, itemToBeAltered);
        }
            
        if (oldItem.getType().equals("end")){
            currentState = new Pair(currentState.getValue0(), new ArrayList<>());
        } else {
           removeEndline(id); 
        }
    }
    
    private void updateState(HttpServletRequest request) throws IOException{
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String prevItemId = request.getParameter("prevItemId");
        String caption = request.getParameter("caption");
        String condition = request.getParameter("condition");
        String isMultipart = request.getParameter("isMultipart");
        String finalMultipart = request.getParameter("finalMultipart");
        String firstMultipart = request.getParameter("firstMultipart");

        if (currentState.getValue1().contains(prevItemId)){ //deal with endlines
            removeEndline(prevItemId);
            currentState.getValue1().add(id);
        }
        
        if (isMultipart == null) { isMultipart = "false"; }
        if (finalMultipart == null) { finalMultipart = "false"; }
        if (firstMultipart == null) { firstMultipart = "false"; }
        
        ChartItem newItem = new ChartItem(id, type, prevItemId, caption);
        if (condition != null && !condition.equals("null")) { newItem = new ChartItem(id, type, prevItemId, caption, condition); }
        
        if (nextIsEnd(prevItemId)){
            Integer endIndex = nextElementIndex(prevItemId);
            ChartItem endItem = currentState.getValue0().get(endIndex);
            endItem.setPrevItemId(id);
            currentState.getValue0().set(endIndex, endItem);
        }
        
        Integer index = findIndexOf(id);
        if (type.equals("start")) {
            clearAllStacks();
            currentState.getValue0().add(newItem);
        } else if (index > 0) { // replace
            maintainMaxDequeSize("undo");
            undoStack.addFirst(deepCopyCurrentState(currentState));
            currentState.getValue0().set(index, newItem);
        } else if (isMultipart.equals("true")){ // conditional
            if (firstMultipart.equals("true")) { 
                maintainMaxDequeSize("undo");
                undoStack.addFirst(deepCopyCurrentState(currentState));
                conditionalId = id;
            }
            
            Integer prevItemIndex = findPrevIdIndex(prevItemId);
            if (finalMultipart.equals("false")){ // add first two
                if (prevItemIndex >= currentState.getValue0().size() - 1){ currentState.getValue0().addLast(newItem); } 
                else { currentState.getValue0().add(prevItemIndex + 1, newItem); }
            } else { // add last and change previd of next item
                if (prevItemIndex >= currentState.getValue0().size() - 2){
                    currentState.getValue0().addLast(newItem);
                } else {
                    currentState.getValue0().add(prevItemIndex + 2, newItem);
                    Integer nextIndex = nextElementIndex(conditionalId);
                    ChartItem nextItem = currentState.getValue0().get(nextIndex);
                    nextItem.setPrevItemId(conditionalId);
                    currentState.getValue0().set(nextIndex, nextItem);
                }
            }
        } else { // standard
            maintainMaxDequeSize("undo");
            undoStack.addFirst(deepCopyCurrentState(currentState));
            
            Integer prevItemIndex = findPrevIdIndex(prevItemId);
            if (prevItemIndex >= currentState.getValue0().size() - 2){
                currentState.getValue0().addLast(newItem);
            } else {
                currentState.getValue0().add(prevItemIndex + 1, newItem);
                Integer nextIndex = nextElementIndex(prevItemId);
                ChartItem nextItem = currentState.getValue0().get(nextIndex);
                nextItem.setPrevItemId(id);
                currentState.getValue0().set(nextIndex, nextItem);
            }
        }
    }
    
    private Integer nextElementIndex(String id){
        Integer nextIndex = -1;
        for (Integer index = 0; index < currentState.getValue0().size(); index++){
            if (currentState.getValue0().get(index).getPrevItemId().equals(id)){
                nextIndex = index;
                break;
            }
        }
        return nextIndex;
    }
    
    private Integer undo() throws JsonProcessingException{
        maintainMaxDequeSize("undo");
        redoStack.addFirst(deepCopyCurrentState(currentState));
        currentState = deepCopyCurrentState(undoStack.removeFirst());
        return undoStack.size();
    }
    
    private Integer redo(){
        maintainMaxDequeSize("redo");
        undoStack.addFirst(deepCopyCurrentState(currentState));
        currentState = deepCopyCurrentState(redoStack.removeFirst());
        return redoStack.size();
    }
    
    private void maintainMaxDequeSize(String dequeName)throws NullPointerException{
        switch (dequeName) {
            case "undo":
                if (undoStack.size() >= MAX_DEQUE_SIZE){
                    undoStack.removeLast();
                }
                break;
            case "redo":
                if (redoStack.size() >= MAX_DEQUE_SIZE){
                    redoStack.removeLast();
                }
                break;
            default:
                throw new NullPointerException("This deque does not exist: " + dequeName);
        }
    }
    
    private Integer findIndexOf(String id){
        Integer itemIndex = -1; // -1 = does not exist
        for (Integer index = 0; index < currentState.getValue0().size(); index++){
            if (id.equals(currentState.getValue0().get(index).getId())){
                itemIndex = index;
                break;
            }
        }
        return itemIndex;
    }
    
    private Integer findPrevIdIndex(String prevId){
        Integer prevItemIndex = -1;
        for (Integer index = 0; index < currentState.getValue0().size(); index++){
            if (prevId.equals(currentState.getValue0().get(index).getId())){
                prevItemIndex = index;
                break;
            }
        }
        return prevItemIndex;
    }
    
    private String getLocalMapping(){
        return "{\"singulars\":{\"test1\":\"test1query\", \"test2\": \"test2query\", \"test3\":\"test3query\"}, \"plurals\": {\"plural1\":\"plural1query\", \"plural2\":\"plural2query\", \"plural3\":\"plural3query\"}}";
    }
    
    private String getStandardizedMapping(){
        return "{\"singulars\":{\"standardtest1\":\"test1\", \"standardtest2\":\"test2\", \"standardtest3\":\"test3\"}, \"plurals\": {\"standardplural1\":\"plural1\", \"standardplural2\":\"plural2\", \"standardplural3\":\"plural3\")}";
    }
    
    private void clearAllStacks(){
        currentState = new Pair(new LinkedList<>(), new ArrayList<>());
        undoStack.clear();
        redoStack.clear();
    }
    
    private String ALToString(ArrayList<String> input){
        if (input.size() > 0){
            String result = "[";
            for (String item : input) {
                result += "\"" + item + "\",";
            }
            result = result.substring(0, result.length() - 1);
            result += "]";
            return result;
        } else { return "[]"; }
    }
    
    private Pair<LinkedList<ChartItem>, ArrayList<String>> deepCopyCurrentState(Pair<LinkedList<ChartItem>, ArrayList<String>> state) {
        LinkedList<ChartItem> stateCopy = new LinkedList<>();
        ArrayList<String> endLinesCopy = new ArrayList<>();
        
        for (ChartItem item : state.getValue0()){
            ChartItem itemCopy = null;
            try {
                itemCopy = new ChartItem(item.getId(), item.getType(), item.getPrevItemId(), item.getCaption(), item.getCondition());
            } catch (Exception e) {
                itemCopy = new ChartItem(item.getId(), item.getType(), item.getPrevItemId(), item.getCaption());
            }
            stateCopy.addLast(itemCopy);
        }
        
        for (String line : state.getValue1()){
            endLinesCopy.add(line);
        }
        
        return new Pair(stateCopy, endLinesCopy);
    }
    
    private Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> deepCopyDeque(Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> deque){
        Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> dequeCopy = new LinkedList<>();
        for (Pair<LinkedList<ChartItem>, ArrayList<String>> item : deque){
            dequeCopy.addLast(deepCopyCurrentState(item));
        }
        return dequeCopy;
    }
}

