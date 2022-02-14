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

    private LinkedList<ChartItem> currentState = new LinkedList<>();
    private Deque<LinkedList<ChartItem>> undoStack = new LinkedList();
    private Deque<LinkedList<ChartItem>> redoStack = new LinkedList();
    private final Integer MAX_DEQUE_SIZE = 10;
    private String workingDir = "C:\\Users\\RLvan\\OneDrive\\Documenten\\MI\\SRP";
    private final String settingsFileName = "mikat_settings.json";
    private JsonNode settings = null; // {prevOpened:[{},{},{}]}
    private ArrayList<JsonNode> dependencies = null; // [{dependency: ..., fileLocation: ..., date: ...},{}]
    private ArrayList<Pair<String, JsonNode>> loadedDependencies = null;
    
    
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
            case "update": //no response, because this is merely a backend save. the changes are already processed at the frontend
                updateState(request);
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState) + "}");
                break;
            case "undo":
                Integer undoSize = undo();
                response.getWriter().write("{'state': " + JSONEncoder.encodeChart(currentState) + ", 'size':" + undoSize + "}");
                break;
            case "redo":
                Integer redoSize = redo();
                response.getWriter().write("{'state':" + JSONEncoder.encodeChart(currentState) + ", 'size':" + redoSize + "}");
                break;
            case "undoSize":
                response.getWriter().write("{'size':" + undoStack.size() + "}");
                break;                
            case "redoSize":
                response.getWriter().write("{'size':" + redoStack.size() + "}");
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
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState) + "}");
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
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState) + "}");
                break;
            case "getElement":
                ChartItem element = getElementById(request);
                response.getWriter().write("{\"chartItem\":" + JSONEncoder.encodeItem(element) + "}");
                break;
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

    private ChartItem getElementById(HttpServletRequest request){
        String id = request.getParameter("id");
        ChartItem element = null;
        for (ChartItem chartItem : currentState){
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
            currentState = JSONDecoder.decodeChart(project.get("state").toString());
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
        currentState.remove(currentState.get(itemIndex));
        currentState.stream().filter(item -> (item.getPrevItemId().equals(id))).forEachOrdered(item -> {
            if (item.getType().equals("conditional")){
                removeConditional(item.getId());
            } else {
                currentState.remove(item);
            }
        });
    }
    
    private void deleteItem(HttpServletRequest request) throws IOException{
        String id = request.getParameter("id");
        Integer itemIndex = findIndexOf(id);
        maintainMaxDequeSize("undo");
        undoStack.addFirst(currentState);
        ChartItem oldItem = currentState.get(itemIndex);
        if (oldItem.getType().equals("conditional")) { removeConditional(oldItem.getId()); }
        else { 
            currentState.remove(currentState.get(itemIndex));
        }
        
        try {
            ChartItem itemToBeAltered = currentState.get(itemIndex);
            itemToBeAltered.setPrevItemId(oldItem.getPrevItemId());
            currentState.set(itemIndex, itemToBeAltered);
        } catch (Exception e){}
    }
    
    private void updateState(HttpServletRequest request) throws IOException{
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String prevItemId = request.getParameter("prevItemId");
        String caption = request.getParameter("caption");
        String condition = request.getParameter("condition");
        String isMultipart = request.getParameter("isMultipart");
        String finalMultipart = request.getParameter("finalMultipart");
        
        if (isMultipart == null) { isMultipart = "false"; }
        if (finalMultipart == null) { finalMultipart = "false"; }
        
        Integer itemIndex = findIndexOf(id);
        Integer prevIdIndex = findPrevIdIndex(prevItemId);
        ChartItem newItem = new ChartItem(id, type, prevItemId, caption);
        
        if (condition != null){
            newItem = new ChartItem (id, type, prevItemId, caption, condition);
        }
        
        
        if ((isMultipart.equals("true") && finalMultipart.equals("true")) || (isMultipart.equals("false"))){ //allow for conditionals
            maintainMaxDequeSize("undo");
            undoStack.addFirst(currentState);
        }
        
        if (type.equals("start")) {
            clearAllStacks();
            currentState.add(newItem);
            return;
        }
        if (itemIndex > -1){ // item already exists, replace
            currentState.set(itemIndex, newItem);
        } else if (isMultipart.equals("true") && finalMultipart.equals("true")){ //item is last item in conditional
            currentState.add(prevIdIndex + 2, newItem);
        } else if (prevIdIndex == (currentState.size()-1)){ // prevItemId equals id of last item in currentState
            currentState.addLast(newItem);
        } else { // insert between existing items
            currentState.add(prevIdIndex + 1, newItem); // insert after prevId item
            if ((isMultipart.equals("false")) || (prevIdIndex == (currentState.size()-2))) {
                Integer nextItemIndex = nextElementIndex(id);
                if (nextItemIndex > 0) {
                    ChartItem nextItem = currentState.get(nextItemIndex); // we have to change the prevItemId of the next item
                nextItem.setPrevItemId(newItem.getId());
                currentState.set(nextItemIndex, nextItem);
                }
            }
        }
    }
    
    private Integer nextElementIndex(String id){
        Integer nextIndex = -1;
        for (Integer index = 0; index < currentState.size(); index++){
            if (id.equals(currentState.get(index).getPrevItemId())){
                nextIndex = index;
                break;
            }
        }
        return nextIndex;
    }
    
    private Integer undo(){
        maintainMaxDequeSize("undo");
        redoStack.addFirst(currentState);
        currentState = undoStack.removeFirst();
        return undoStack.size();
    }
    
    private Integer redo(){
        maintainMaxDequeSize("redo");
        undoStack.addFirst(currentState);
        currentState = redoStack.removeFirst();
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
        for (Integer index = 0; index < currentState.size(); index++){
            if (id.equals(currentState.get(index).getId())){
                itemIndex = index;
                break;
            }
        }
        return itemIndex;
    }
    
    private Integer findPrevIdIndex(String prevId){
        Integer prevItemIndex = -1;
        for (Integer index = 0; index < currentState.size(); index++){
            if (prevId.equals(currentState.get(index).getId())){
                prevItemIndex = index;
                break;
            }
        }
        return prevItemIndex;
    }
    
    private String getLocalMapping(){
        return "{\"test1\":\"test1query\", \"test2\": \"test2query\", \"test3\":\"test3query\"}";
    }
    
    private String getStandardizedMapping(){
        return "{\"standardtest1\":\"test1\", \"standardtest2\":\"test2\", \"standardtest3\":\"test3\"}";
    }
    
    private void clearAllStacks(){
        currentState.clear();
        undoStack.clear();
        redoStack.clear();
    }
}
