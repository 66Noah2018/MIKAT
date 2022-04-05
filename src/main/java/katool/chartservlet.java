/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    private Pair<LinkedList<ChartItem>, ArrayList<String>> currentState = new Pair(new LinkedList<>(), new ArrayList<>());
    private Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> undoStack = new LinkedList<>();
    private Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> redoStack = new LinkedList<>();
    private final Integer MAX_DEQUE_SIZE = 10;
    private String conditionalId = null;
    private String localMappingsFileLocation = null;
    private String standardizedMappingsFileLocation = null;
    private String localMapping = "{}";
    private String standardizedMapping = "{}";
    private String testCasesFileLocation = null;
    
    
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
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + currentState.getValue1().toString() + "}");
                break;
            case "undo":
                Integer undoSize = undo();
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + currentState.getValue1().toString() + ", \"size\":" + undoSize + "}");
                break;
            case "redo":
                Integer redoSize = redo();
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + currentState.getValue1().toString() + ", \"size\":" + redoSize + "}");
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
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + currentState.getValue1().toString() + "}");
                break;
            case "file":
                String mlmname = selectFile(request);
                response.getWriter().write("{\"mlmname\":\"" + mlmname + "\"}");
                break;
            case "open":
                String returnValue = openProject(request);
                response.getWriter().write("{\"response\":\"" + returnValue + "\"}");
                break;
            case "save":
                saveChanges();
                break;
            case "state":
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + currentState.getValue1().toString() + "}");
                break;
            case "getElement":
                ChartItem element = getElementById(request);
                response.getWriter().write("{\"chartItem\":" + JSONEncoder.encodeItem(element) + "}");
                break;
            case "endline":
                updateEndlineList(request);
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + currentState.getValue1().toString() + "}");
                break;
            case "hasNext":
                Boolean result = itemHasNext(request);
                response.getWriter().write("{\"hasNext\":" + result + "}");
                break;
            case "getConditionalActions":
                LinkedList<ChartItem> nextItems = getConditionalItems(request);
                if (nextItems.isEmpty()) { nextItems = null; }
                response.getWriter().write("{\"items\":" + JSONEncoder.encodeChart(nextItems) + "}");
                break;
            case "getNext":
                ChartItem nextItem = getNextItem(request);
                response.getWriter().write("{\"nextItem\":" + JSONEncoder.encodeItem(nextItem) + "}");
                break;
            case "getClosestLoopStart":
                String caption = getClosestLoopStart(request);
                response.getWriter().write("{\"caption\":\"" + caption + "\"}");
                break;
            case "loopHasEnd":
                Boolean hasEnd = loopHasEnd(request);
                response.getWriter().write("{\"hasEnd\":" + hasEnd + "}");
                break;
            case "updateLocalMapping":
                updateLocalMapping(request);
                break;
            case "updateStandardizedMapping":
                updateStandardizedMapping(request);
                break;
            case "setWorkingDirectory":
                setWorkingDirectory(request);
                break;
            case "saveProject":
                saveProject(request);
                break;
            case "directoryExists":
                Boolean exists = directoryExists(request);
                response.getWriter().write("{\"directoryExists\":" + exists + "}");
                break;
            case "getProjectProperties":
                String properties = Utils.readProjectFromFile();
                response.getWriter().write(properties);
                break;
            case "getPrevOpened":
                Utils.loadSettings();
                String prevOpenedString = Utils.prevOpened.toString();
                response.getWriter().write(prevOpenedString);
                break;
            case "hasProjectOpened":
                response.getWriter().write("{\"hasProjectOpened\":" + !Utils.currentPath.equals("") + "}");
                break;
            case "translateJS":
                String translation = ChartTranslator.translateToJS(currentState, Paths.get(Utils.currentPath).getFileName().toString());
                response.getWriter().write(translation);
                break;
            case "translateAS":
                ChartTranslator.translateToArdenSyntax(currentState);
                break;
            case "getDefaultWorkingDirectory":
                if (Utils.defaultWorkingDirectory == null) { Utils.loadSettings(); }
                response.getWriter().write("{\"defaultWorkingDirectory\":\"" + Utils.defaultWorkingDirectory.replace("\\", "\\\\") + "\"}");
                break;
            case "setDefaultWorkingDirectory":
                Utils.defaultWorkingDirectory = Utils.getBody(request).replace("\"", "");
                Utils.writeSettings();
                break;
            case "saveTestCases":
                saveTestCases(request);
                break;
            case "getTestCases":
                String testCases = getTestCases();
                response.getWriter().write("{\"testCases\":" + testCases + "}");
                break;
            case "getTestCasesFromFile":
                String testCasesFromFile = getTestCasesFromFile(request);
                response.getWriter().write("{\"testCases\":" + testCasesFromFile + "}");
                break;
            case "hasTestCases":
                Boolean hasTestCases = false;
                if (testCasesFileLocation != null) { hasTestCases = true; }
                response.getWriter().write("{\"hasTestCases\":" + hasTestCases + "}");
                break;
            case "getTestTableHeadings":
                String headings = getTestTableHeadings();
                response.getWriter().write(headings);
                break;
            case "setTestCasesFileLocation":
                String loaded = setTestCasesFileLocation(request);
                response.getWriter().write("{\"fileLoaded\":" + loaded + "}");
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
    
    // Servlet functions
    
    private String setTestCasesFileLocation(HttpServletRequest request) throws IOException{
        String fileName = Utils.getBody(request);
        String pathToFile = null;
        fileName = fileName.substring(1, fileName.length()-1);
            if (Utils.workingDir != null) {
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.workingDir.toString()), Utils.extensions, true);
                while (fileIterator.hasNext() && pathToFile == null) {
                    File file = fileIterator.next();
                    if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
                }
            }
            if (pathToFile == null) {
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.rootPath), Utils.extensions, true);
                while (fileIterator.hasNext() && pathToFile == null) {
                    File file = fileIterator.next();
                    if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
                }
            }
            if (pathToFile == null) { return "Invalid file, no path"; }
        testCasesFileLocation = pathToFile;
        return "loaded";
    }
    
    private String getTestTableHeadings() throws IOException{
        ArrayList<String> retrievedataElements = new ArrayList<>();
        ArrayList<String> medicalActions = new ArrayList<>();
        for (ChartItem item : currentState.getValue0()){
            switch (item.getType()){
                case "retrievedata":
                    retrievedataElements.add("\"" + item.getCaption() + "\"");
                    break;
                case "newProcedure":
                    medicalActions.add("\"New procedure: " + item.getCaption() + "\"");
                    break;
                case "orderLabs":
                    medicalActions.add("\"Order labs: " + item.getCaption() + "\"");
                    break;
                case "newPrescription":
                    medicalActions.add("\"New prescription: " + item.getCaption() + "\"");
                    break;
                case "addDiagnosis":
                    medicalActions.add("\"Add diagnosis: " + item.getCaption() + "\"");
                    break;
                case "newVaccination":
                    medicalActions.add("\"New vaccination: " + item.getCaption() + "\"");
                    break;
                case "addNotes":
                    medicalActions.add("\"Add notes: " + item.getCaption() + "\"");
                    break;
                case "subroutine":
                    Pair<LinkedList<ChartItem>, ArrayList<String>> subroutine = Utils.getDependency(item.getCaption());
                    ArrayList<String> result = getSubroutineHeadings(subroutine.getValue0());
                    retrievedataElements.addAll(result);
                default:
                    break;
            }
        }
        return "{\"retrievedata\":" + retrievedataElements.toString() + ",\"medicalActions\":" + medicalActions.toString() + "}";
    }
    
    public ArrayList<String> getSubroutineHeadings(LinkedList<ChartItem> state) throws JsonProcessingException, IOException {
        ArrayList<String> retrievedataElements = new ArrayList<>();
        for (ChartItem item : state){
            switch (item.getType()){
                case "retrievedata":
                    retrievedataElements.add("\"" + item.getCaption() + "\"");
                    break;
                case "subroutine":
                    LinkedList<ChartItem> subroutineState = Utils.getDependency(item.getCaption()).getValue0();
                    ArrayList<String> result = getSubroutineHeadings(subroutineState);
                    retrievedataElements.addAll(result);
                default:
                    break;
            }
        }
        return retrievedataElements;
    }
    
    private String getTestCasesFromFile(HttpServletRequest request) throws IOException{
        String fileName = request.getParameter("fileName");
        String pathToFile = null;
        fileName = fileName.substring(1, fileName.length()-1);
        if (Utils.workingDir != null) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.workingDir.toString()), Utils.extensions, true);
            while (fileIterator.hasNext() && pathToFile == null) {
                File file = fileIterator.next();
                if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
            }
        }
        if (pathToFile == null) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.rootPath), Utils.extensions, true);
            while (fileIterator.hasNext() && pathToFile == null) {
                File file = fileIterator.next();
                if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
            }
        }
        return new String(Files.readAllBytes(Paths.get(pathToFile)));
    }
    
    private String getTestCases() throws IOException{
        return new String(Files.readAllBytes(Paths.get(testCasesFileLocation)));
    }
    
    private void saveTestCases(HttpServletRequest request) throws IOException {
        String newFile = request.getParameter("newFile");
        String requestBody = Utils.getBody(request);
        if (testCasesFileLocation == null || newFile.equals("true")){ testCasesFileLocation = Utils.workingDir.toString() + "\\" + "testcases_" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".json"; }
        FileWriter file = new FileWriter(testCasesFileLocation);
        file.write(requestBody);
        file.close();
    }
    
    private Boolean directoryExists(HttpServletRequest request) throws IOException {
        String folder = Utils.getBody(request).replace("\\\\", "\\").replace("\"", "");
        Path path = Paths.get(folder);
        return Files.exists(path);
    }
    
    private void setWorkingDirectory(HttpServletRequest request) throws IOException {
        String folder = Utils.getBody(request).replace("\"", "");
        Utils.workingDir = Paths.get(folder);
        if (Utils.defaultWorkingDirectory == null) { 
            Utils.defaultWorkingDirectory = folder;
            Utils.writeSettings();
        }
    }
    
    private void saveProject(HttpServletRequest request) throws IOException{
        String isNew = request.getParameter("isNew");
        if (isNew.equals("true")) { clearAllStacks(); }
        Utils.determineOS();
        String testCasesFileLocationString = Paths.get(testCasesFileLocation).toString().replace("\\", "\\\\");
        String body = Utils.getBody(request).replace("\\\"", "\"");
        body = body.substring(0, body.length()-2);
        body += ",\"dependencies\":" + Utils.dependencies.toString() + ",\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ",\"endLines\":" + currentState.getValue1().toString() + 
                ",\"workingDirectory\":\"" + Utils.workingDir.toString().replace("\\", "\\\\") + "\"" + ",\"testCasesFileLocation\":\"" + testCasesFileLocationString + "\"}";
        Pattern pattern = Pattern.compile("\"mlmname\":\"(.+)\",\"ar");
        Matcher matcher = pattern.matcher(body);
        Boolean matchFound = matcher.find();
        String mlmname = "";
        if (matchFound) { 
            mlmname = matcher.group(1);
            String fileLocation = Utils.workingDir + "\\" + mlmname + ".json";
            Utils.currentPath = fileLocation;
            FileWriter file = new FileWriter(fileLocation);
            file.write(body);
            file.close();
            setMappingLocations(body);
            
            ObjectMapper mapper = new ObjectMapper();
            
            for (int i = 0; i < Utils.prevOpened.size(); i++){
                JsonNode node = Utils.prevOpened.get(i);
                if ((node.get("fileName").asText()).equals(mlmname)) { Utils.prevOpened.remove(i); }
            }

            Utils.updatePrevOpened(mlmname);
        }
    }
    
    private void updateLocalMapping(HttpServletRequest request) throws IOException{
        String mapping = Utils.getBody(request);
        String fileLocation = localMappingsFileLocation; //cannot assume this! need to compare to working dir to get absolute path
        FileWriter file = new FileWriter(fileLocation);
        file.write(mapping);
        file.close();
        localMapping = mapping;
    }
    
    private void updateStandardizedMapping(HttpServletRequest request) throws IOException{
        String mapping = Utils.getBody(request);
        String fileLocation = standardizedMappingsFileLocation;
        FileWriter file = new FileWriter(fileLocation);
        file.write(mapping);
        file.close();
        standardizedMapping = mapping;
    }
    
    private Boolean loopHasEnd(HttpServletRequest request) {
        Boolean hasEnd = false;
        String caption = request.getParameter("caption");
        for (ChartItem item : currentState.getValue0()){
            if (item.getCaption().equals("End for " + caption)) {
                hasEnd = true;
                break;
            }
        }
        return hasEnd;
    }
    
    private String getClosestLoopStart(HttpServletRequest request){
        String caption = null;
        String prevItemId = request.getParameter("prevItemId");
        for (ChartItem item : currentState.getValue0()){
            if (item.getType().equals("loop") && !item.getCaption().startsWith("End for")) { caption = item.getCaption(); }
            if (item.getId().equals(prevItemId)){ break; }
        }
        return caption;
    }
    
    private ChartItem getNextItem(HttpServletRequest request){
        String id = request.getParameter("id");
        ChartItem nextItem = null;
        for (ChartItem item : currentState.getValue0()) {
            if (item.getPrevItemId().equals(id)) {
                nextItem = item;
                break;
            }
        } 
        return nextItem;
    }
    
    private LinkedList<ChartItem> getConditionalItems(HttpServletRequest request){
        String id = request.getParameter("id");
        return Utils.conditionalItems(id, currentState.getValue0());
    }
    
    private Boolean itemHasNext(HttpServletRequest request){
        String id = request.getParameter("id");
        Boolean nextItem = false;
        for (ChartItem element : currentState.getValue0()) {
            if (id.equals(element.getPrevItemId())) { nextItem = true; }
        }
        return nextItem;
    }
    
    private void updateEndlineList(HttpServletRequest request){
        String id = request.getParameter("id");
        String isMultipart = request.getParameter("isMultipart");
        if (isMultipart == null) { isMultipart = "false"; }
        if (!id.startsWith("\"")) { id = "\"" + id + "\""; }
        if (!currentState.getValue1().contains(id)) {
            if (isMultipart.equals("false")) {
                maintainMaxDequeSize("undo");
                undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
            }
            currentState.getValue1().add(id);
        }
    }
    
    private ChartItem getElementById(HttpServletRequest request){
        String id = request.getParameter("id");
        ChartItem element = null;
        for (ChartItem chartItem : currentState.getValue0()){
            if ((chartItem.getId()).equals(id)) { 
                element = chartItem;
                break;
            }
        }
        return element;
    }
    
    private String openProject(HttpServletRequest request) throws IOException{
        clearAllStacks();
        Boolean determinationSuccess = Utils.determineOS();
        if (!determinationSuccess) { return "Unsupported OS"; }
        Utils.loadSettings();
        String fileName = Utils.getBody(request);
        String project = null;
        if (fileName.contains("\\")) { 
            fileName = fileName.replace("\\\\", "\\");
            fileName = fileName.substring(1, fileName.length()-1); }
        if (fileName.startsWith(Utils.rootPath)) { // fully specified path
            project = new String(Files.readAllBytes(Paths.get(fileName)));
            if (!Utils.checkFileValidity(project)) { return "Invalid file, not MIKAT"; }
            Utils.currentPath = fileName;
            fileName = Paths.get(fileName).getFileName().toString();
            
        } else { //find file and read
            String pathToFile = null;
            fileName = fileName.substring(1, fileName.length()-1);
            if (Utils.workingDir != null) {
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.workingDir.toString()), Utils.extensions, true);
                while (fileIterator.hasNext() && pathToFile == null) {
                    File file = fileIterator.next();
                    if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
                }
            }
            if (pathToFile == null) {
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.rootPath), Utils.extensions, true);
                while (fileIterator.hasNext() && pathToFile == null) {
                    File file = fileIterator.next();
                    if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
                }
            }
            if (pathToFile == null) { return "Invalid file, no path"; }
            project = new String(Files.readAllBytes(Paths.get(pathToFile)));
            if (!Utils.checkFileValidity(project)) { return "Invalid file, not MIKAT"; }
            Utils.currentPath = pathToFile;
        }
        
        Pattern workingDirPattern = Pattern.compile("\"workingDirectory\":\"(.+)\",\"t");
        Pattern statePattern = Pattern.compile("\"state\":(.+),\"e");
        Pattern endLinesPattern = Pattern.compile("\"endLines\":(.+),\"w");
        Pattern dependenciesPattern = Pattern.compile("\"dependencies\":(.+),\"s");
        Pattern testCasesPattern = Pattern.compile("\"testCasesFileLocation\":\"(.+)\"}");
        Pattern mlmNamePattern = Pattern.compile("\"mlmname\":\"(.+)\",\"ar");
        Matcher workingDirMatcher = workingDirPattern.matcher(project);
        Matcher stateMatcher = statePattern.matcher(project);
        Matcher endLinesMatcher = endLinesPattern.matcher(project);
        Matcher dependenciesMatcher = dependenciesPattern.matcher(project);
        Matcher testCasesMatcher = testCasesPattern.matcher(project);
        Matcher mlmNameMatcher = mlmNamePattern.matcher(project);
        
        Boolean workingDirMatch = workingDirMatcher.find();
        Boolean stateMatch = stateMatcher.find();
        Boolean endLinesMatch = endLinesMatcher.find();
        Boolean dependenciesMatch = dependenciesMatcher.find();
        Boolean testCasesMatch = testCasesMatcher.find();
        Boolean mlmNameMatch = mlmNameMatcher.find();
        
        if(!workingDirMatch || !stateMatch || !endLinesMatch || !dependenciesMatch || !testCasesMatch || !mlmNameMatch) { return "File is not MIKAT file"; }
        Utils.workingDir = Paths.get(workingDirMatcher.group(1));
        if ((endLinesMatcher.group(1)).equals("[]")) {
            currentState = new Pair<>(JSONDecoder.decodeChart(stateMatcher.group(1)), new ArrayList<>());
        } else {
            currentState = new Pair<>(JSONDecoder.decodeChart(stateMatcher.group(1)), new ArrayList<>(Arrays.asList(endLinesMatcher.group(1).replace("[", "").replace("]", "").split(", "))));
        }
        ObjectMapper mapper = new ObjectMapper();
        Utils.dependencies = (ArrayNode) mapper.readTree(dependenciesMatcher.group(1));
        testCasesFileLocation = testCasesMatcher.group(1);

        String mlmName = mlmNameMatcher.group(1);
        for (int i = 0; i < Utils.prevOpened.size(); i++){
            JsonNode node = Utils.prevOpened.get(i);
            if ((node.get("fileName").asText()).equals(mlmName)) { Utils.prevOpened.remove(i); }
        }
        
        Utils.updatePrevOpened(mlmName);
        setMappingLocations(project);
        return "File opened successfully";
    }
        
    private String selectFile(HttpServletRequest request) throws IOException{ // is this even used?
        String fileName = request.getParameter("name");
        File root = new File(Utils.workingDir.toString());
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
                    if (Utils.checkFileValidity(selectedFileString)){
                        ObjectMapper mapper = new ObjectMapper();
                        selectedFileContents = mapper.readTree(selectedFileString);
                        String title = selectedFileContents.at("/maintenance/title").asText();
                        mlmname = selectedFileContents.at("/maintenance/mlmname").asText();
                        Utils.addNewDependency(title, path, selectedFileContents);
                    }
                }
            } 
        } catch (Exception e){}  
        return mlmname;
    }
    
    private void deleteItem(HttpServletRequest request) throws IOException{
        String id = request.getParameter("id");
        Integer itemIndex = Utils.findIndexOf(id, currentState.getValue0());
        maintainMaxDequeSize("undo");
        undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
        ChartItem oldItem = currentState.getValue0().get(itemIndex);
        if (oldItem.getType().equals("conditional")) { removeConditional(oldItem.getId()); }
        else { 
            currentState.getValue0().remove(oldItem);
        }
        
        Integer nextIndex = Utils.nextElementIndex(id, currentState.getValue0());
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
        
        if (Utils.nextIsEnd(prevItemId, currentState.getValue0()) && isMultipart.equals("false")){
            Integer endIndex = Utils.nextElementIndex(prevItemId, currentState.getValue0());
            ChartItem endItem = currentState.getValue0().get(endIndex);
            endItem.setPrevItemId(id);
            currentState.getValue0().set(endIndex, endItem);
        }
        
        Integer index = Utils.findIndexOf(id, currentState.getValue0());
        if (type.equals("start")) {
            clearAllStacks();
            currentState.getValue0().add(newItem);
        } else if (index > 0) { // replace
            if (isMultipart.equals("false") || firstMultipart.equals("true")) {
                maintainMaxDequeSize("undo");
                undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
            }
            currentState.getValue0().set(index, newItem);
        } else if (isMultipart.equals("true")){ // conditional
            if (firstMultipart.equals("true")) { 
                maintainMaxDequeSize("undo");
                undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
                conditionalId = id;
            }
            
            Integer prevItemIndex = Utils.findPrevIdIndex(prevItemId, currentState.getValue0());
            if (finalMultipart.equals("false")){ // add first two
                if (prevItemIndex >= currentState.getValue0().size() - 1){ currentState.getValue0().addLast(newItem); } 
                else { currentState.getValue0().add(prevItemIndex + 1, newItem); }
            } else { // add last and change previd of next item
                if (prevItemIndex >= currentState.getValue0().size() - 1){
                    currentState.getValue0().addLast(newItem);
                } else {
                    currentState.getValue0().add(prevItemIndex + 2, newItem);
                    Integer nextIndex = Utils.nextElementIndex(prevItemId, currentState.getValue0());
                    ChartItem nextItem = currentState.getValue0().get(nextIndex);
                    nextItem.setPrevItemId(prevItemId);
                    currentState.getValue0().set(nextIndex, nextItem);
                }
            }
        } else { // standard
            maintainMaxDequeSize("undo");
            undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
            
            Integer prevItemIndex = Utils.findPrevIdIndex(prevItemId, currentState.getValue0());
            if (prevItemIndex >= currentState.getValue0().size() - 2){
                currentState.getValue0().addLast(newItem);
            } else {
                Integer nextIndex = Utils.nextElementIndex(prevItemId, currentState.getValue0()) + 1; // because we still have to insert
                currentState.getValue0().add(prevItemIndex + 1, newItem);
                ChartItem nextItem = currentState.getValue0().get(nextIndex);
                nextItem.setPrevItemId(id);
                currentState.getValue0().set(nextIndex, nextItem);
            }
        }
    }
    
    // Helper functions
    
    private void setMappingLocations(String body) throws IOException{
        localMappingsFileLocation = null;
        standardizedMappingsFileLocation = null;
        Pattern patternLocal = Pattern.compile("\"localMappingFile\":\"(.+)\",\"s");
        Pattern patternStandardized = Pattern.compile("\"standardizedMappingFile\":\"(.+)\",\"depen");
        Matcher matcherLocal = patternLocal.matcher(body);
        Matcher matcherStandardized = patternStandardized.matcher(body);
        Boolean matchFound = matcherLocal.find();
        if (matchFound) {
            Iterator<File> localFileIterator = FileUtils.iterateFiles(new File(Utils.workingDir.toString()), Utils.extensions, true);
            while(localFileIterator.hasNext() && localMappingsFileLocation == null) {
                File file = localFileIterator.next();
                if (file.getName().equals(matcherLocal.group(1))) { localMappingsFileLocation = file.getPath(); }
            }
            if (localMappingsFileLocation == null){
                Iterator<File> localFileIteratorC = FileUtils.iterateFiles(new File(Utils.rootPath), Utils.extensions, true);
                while(localFileIteratorC.hasNext() && localMappingsFileLocation == null) {
                    File file = localFileIteratorC.next();
                    if (file.getName().equals(matcherLocal.group(1))) { localMappingsFileLocation = file.getPath(); }
                }
            }
        }
        matchFound = matcherStandardized.find();
        if (matchFound) {
            Iterator<File> standardizedFileIterator = FileUtils.iterateFiles(new File(Utils.workingDir.toString()), Utils.extensions, true);
            while(standardizedFileIterator.hasNext() && standardizedMappingsFileLocation == null) {
                File file = standardizedFileIterator.next();
                if (file.getName().equals(matcherStandardized.group(1))) { standardizedMappingsFileLocation = file.getPath(); }
            }
            if (standardizedMappingsFileLocation == null){
                Iterator<File> standardizedFileIteratorC = FileUtils.iterateFiles(new File(Utils.rootPath), Utils.extensions, true);
                while(standardizedFileIteratorC.hasNext() && standardizedMappingsFileLocation == null) {
                    File file = standardizedFileIteratorC.next();
                    if (file.getName().equals(matcherStandardized.group(1))) { standardizedMappingsFileLocation = file.getPath(); }
                }
            }
        }
    }
    
    private void removeEndline(String id) {
        if (currentState.getValue1().contains(id)){
            maintainMaxDequeSize("undo");
            undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
            currentState.getValue1().remove(id);
        }
    }
    
    private void removeConditional(String id){
        Integer itemIndex = Utils.findIndexOf(id, currentState.getValue0());
        currentState.getValue0().remove(currentState.getValue0().get(itemIndex));
        for (Iterator<ChartItem> iterator = currentState.getValue0().iterator(); iterator.hasNext(); ) {
            ChartItem item = iterator.next();
            if (item.getPrevItemId().equals(id)) { iterator.remove(); }
            removeEndline(item.getId());
        }
        removeEndline(id);
    }
    
    private Integer undo() throws JsonProcessingException{
        maintainMaxDequeSize("undo");
        redoStack.addFirst(Utils.deepCopyCurrentState(currentState));
        currentState = Utils.deepCopyCurrentState(undoStack.removeFirst());
        return undoStack.size();
    }
    
    private Integer redo(){
        maintainMaxDequeSize("redo");
        undoStack.addFirst(Utils.deepCopyCurrentState(currentState));
        currentState = Utils.deepCopyCurrentState(redoStack.removeFirst());
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
        
    private String getLocalMapping() throws IOException{
        if (localMapping.equals("{}")) {
            localMapping = new String(Files.readAllBytes(Paths.get(localMappingsFileLocation)));
            if (localMapping.equals("") || !Utils.checkMappingFileValidity(localMapping)) { localMapping = "{}"; }
        }
        return localMapping;
    }
    
    private String getStandardizedMapping() throws IOException{
         if (standardizedMapping.equals("{}")) {
            String fileLocation = standardizedMappingsFileLocation;
            standardizedMapping = new String(Files.readAllBytes(Paths.get(fileLocation)));
            if (standardizedMapping.equals("") || !Utils.checkMappingFileValidity(standardizedMapping)) { standardizedMapping = "{}"; }
        }
        return standardizedMapping;
    }
    
    private void saveChanges() throws IOException {
        String currentProject = new String(Files.readAllBytes(Paths.get(Utils.currentPath)));
        String properties = currentProject.split(",\"dependencies\"")[0];
        properties += ",\"dependencies\":" + Utils.dependencies.toString() + ",\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ",\"endLines\":" + currentState.getValue1().toString() + 
                ",\"workingDirectory\":\"" + Utils.workingDir.toString().replace("\\", "\\\\") + "\"" + ",\"testCasesFileLocation\":\"" + testCasesFileLocation.replace("\\", "\\\\") + "\"}";
        Pattern pattern = Pattern.compile("\"mlmname\":\"(.+)\",\"ar");
        Matcher matcher = pattern.matcher(properties);
        Boolean matchFound = matcher.find();
        String mlmname = "";
        if (matchFound) { 
            mlmname = matcher.group(1);
            String fileLocation = Utils.workingDir + "\\" + mlmname + ".json";
            Utils.currentPath = fileLocation;
            FileWriter file = new FileWriter(fileLocation);
            file.write(properties);
            file.close();
            setMappingLocations(properties);
        }
    }
    
    private void clearAllStacks(){
        currentState = new Pair(new LinkedList<>(), new ArrayList<>());
        undoStack.clear();
        redoStack.clear();
    }
}

