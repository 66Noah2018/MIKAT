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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.shade.booter.org.apache.commons.lang3.SystemUtils;
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
    private final String settingsFileName = "mikat_settings.json";
    private ArrayNode prevOpened = new ObjectMapper().createArrayNode();
    private ArrayNode dependencies = new ObjectMapper().createArrayNode(); // [{dependency: ..., fileLocation: ..., date: ...},{}]
    private ArrayList<Pair<String, JsonNode>> loadedDependencies = new ArrayList<>();
    private String conditionalId = null;
    private String localMappingsFileLocation = null;
    private String standardizedMappingsFileLocation = null;
    private Path workingDir = null;
    private String localMapping = "{}";
    private String standardizedMapping = "{}";
    private String currentPath = "";
    private String programFilesPath = "";
    private String rootPath = "";
    private String settings = null;
    private final String[] extensions = new String[]{"json"};
    
    
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
                response.getWriter().write("{\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ", \"endLines\":" + ALToString(currentState.getValue1()) + ", \"size\":" + undoSize + "}");
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
                String returnValue = openProject(request);
                response.getWriter().write("{\"response\":\"" + returnValue + "\"}");
                break;
            case "save":
                saveChanges();
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
                String properties = readProjectFromFile();
                response.getWriter().write(properties);
                break;
            case "getPrevOpened":
                loadSettings();
                String prevOpenedString = prevOpened.toString();
                response.getWriter().write(prevOpenedString);
                break;
            case "hasProjectOpened":
                response.getWriter().write("{\"hasProjectOpened\":" + !currentPath.equals("") + "}");
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

    private String readProjectFromFile() throws IOException{
        if (!currentPath.equals("")) {
            String file =  new String(Files.readAllBytes(Paths.get(currentPath)));
            String fileContent = file.substring(1, file.length());
            if (checkFileValidity(file)){
                return fileContent;
            } else {
                return "file invalid";
            }
        } else {
            return "null";
        }
    }
    
    private Boolean directoryExists(HttpServletRequest request) throws IOException {
        String folder = getBody(request).replace("\\\\", "\\").replace("\"", "");
        Path path = Paths.get(folder);
        return Files.exists(path);
    }
    
    private void setWorkingDirectory(HttpServletRequest request) throws IOException {
        String folder = getBody(request).replace("\"", "");
        workingDir = Paths.get(folder);
    }
    
    private void saveProject(HttpServletRequest request) throws IOException{
        determineOS();
        String body = getBody(request).replace("\\\"", "\"");
        body = body.substring(0, body.length()-2);
        body += ",\"dependencies\":" + dependencies.toString() + ",\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ",\"endLines\":" + currentState.getValue1().toString() + ",\"workingDirectory\":\"" + workingDir.toString().replace("\\", "\\\\") + "\"}";
        Pattern pattern = Pattern.compile("\"mlmname\":\"(.+)\",\"ar");
        Matcher matcher = pattern.matcher(body);
        Boolean matchFound = matcher.find();
        String mlmname = "";
        if (matchFound) { 
            mlmname = matcher.group(1);
            String fileLocation = workingDir + "\\" + mlmname + ".json";
            currentPath = fileLocation;
            FileWriter file = new FileWriter(fileLocation);
            file.write(body);
            file.close();
            setMappingLocations(body);
            
            ObjectMapper mapper = new ObjectMapper();
            
            for (int i = 0; i < prevOpened.size(); i++){
                JsonNode node = prevOpened.get(i);
                if ((node.get("fileName").asText()).equals(mlmname)) { prevOpened.remove(i); }
            }

            updatePrevOpened(mlmname);
            
            
        }
    }
    
    private void setMappingLocations(String body) throws IOException{
        localMappingsFileLocation = null;
        standardizedMappingsFileLocation = null;
        Pattern patternLocal = Pattern.compile("\"localMappingFile\":\"(.+)\",\"s");
        Pattern patternStandardized = Pattern.compile("\"standardizedMappingFile\":\"(.+)\",\"d");
        Matcher matcherLocal = patternLocal.matcher(body);
        Matcher matcherStandardized = patternStandardized.matcher(body);
        Boolean matchFound = matcherLocal.find();
        if (matchFound) {
            Iterator<File> localFileIterator = FileUtils.iterateFiles(new File(workingDir.toString()), extensions, true);
            while(localFileIterator.hasNext() && localMappingsFileLocation == null) {
                File file = localFileIterator.next();
                if (file.getName().equals(matcherLocal.group(1))) { localMappingsFileLocation = file.getPath(); }
            }
            if (localMappingsFileLocation == null){
                Iterator<File> localFileIteratorC = FileUtils.iterateFiles(new File(rootPath), extensions, true);
                while(localFileIteratorC.hasNext() && localMappingsFileLocation == null) {
                    File file = localFileIteratorC.next();
                    if (file.getName().equals(matcherLocal.group(1))) { localMappingsFileLocation = file.getPath(); }
                }
            }
        }
        matchFound = matcherStandardized.find();
        if (matchFound) {
            Iterator<File> standardizedFileIterator = FileUtils.iterateFiles(new File(workingDir.toString()), extensions, true);
            while(standardizedFileIterator.hasNext() && standardizedMappingsFileLocation == null) {
                File file = standardizedFileIterator.next();
                if (file.getName().equals(matcherStandardized.group(1))) { standardizedMappingsFileLocation = file.getPath(); }
            }
            if (standardizedMappingsFileLocation == null){
                Iterator<File> standardizedFileIteratorC = FileUtils.iterateFiles(new File(rootPath), extensions, true);
                while(standardizedFileIteratorC.hasNext() && standardizedMappingsFileLocation == null) {
                    File file = standardizedFileIteratorC.next();
                    if (file.getName().equals(matcherStandardized.group(1))) { standardizedMappingsFileLocation = file.getPath(); }
                }
            }
        }
    }
    
    private Boolean determineOS(){
        Boolean OSDetermined = true;
        if (programFilesPath.equals("") || rootPath.equals("")){
            if (SystemUtils.IS_OS_WINDOWS){
                rootPath = "C:\\";
                programFilesPath = "C:\\Program Files";
            } else if (SystemUtils.IS_OS_MAC){
                rootPath = "/";
                programFilesPath = "/Applications";
            } else if (SystemUtils.IS_OS_LINUX){
                rootPath = "/";
                programFilesPath = "/opt";
            } else {
                OSDetermined = false;
            }
        }
        if (workingDir == null) { workingDir = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getPath()); }
        return OSDetermined;
    }
    
    private void updateLocalMapping(HttpServletRequest request) throws IOException{
        String mapping = getBody(request);
        String fileLocation = localMappingsFileLocation; //cannot assume this! need to compare to working dir to get absolute path
        FileWriter file = new FileWriter(fileLocation);
        file.write(mapping);
        file.close();
        localMapping = mapping;
    }
    
    private void updateStandardizedMapping(HttpServletRequest request) throws IOException{
        String mapping = getBody(request);
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
    
    private boolean checkFileValidity(String projectString) throws JsonProcessingException{
        projectString = projectString.substring(1, projectString.length());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode project = mapper.readTree(projectString);
        // check whether file was created by this program and contains the correct 'keys'
        JsonNode maintenance = project.get("maintenance");
        JsonNode library = project.get("library");
        JsonNode projectDependencies = project.get("dependencies");
        JsonNode state = project.get("state");
        JsonNode endLines = project.get("endLines");
        JsonNode localMappingFile = project.get("localMappingFile");
        JsonNode standardizedMappingFile = project.get("standardizedMappingFile");
        if (maintenance == null || library == null || projectDependencies == null || state == null || endLines == null || localMappingFile == null || standardizedMappingFile == null) { return false; }
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
                if (!"Production".equals(maintenance.get("validation").asText()) &&
                        !"Research".equals(maintenance.get("validation").asText()) &&
                        !"Testing".equals(maintenance.get("validation").asText()) &&
                        !"Expired".equals(maintenance.get("validation").asText())) {
                    return false;
                } else { // if we get here the maintenance section is complete
                    if (!library.isNull()){
                        if (library.hasNonNull("purpose") &&
                                library.hasNonNull("explanation") &&
                                library.hasNonNull("keywords")){
                            // we made it through the MLM required fields
                            // from now on: fields that are required by MIKAT and mark this project as a MIKAT project
                            if (!projectDependencies.isNull()) { // minimum is an empty JsonArray
                                if (!state.isNull()){
                                    if (!endLines.isNull()){
                                        if (!localMappingFile.isNull()){
                                            if (!standardizedMappingFile.isNull()){
                                                return true;
                                            } else { return false; }
                                        } else {return false; }
                                    }else { return false; }
                                }else { return false; }
                            } else { return false; }
                        } else { return false; }
                    } else { return false; }
                }
            } else { return false; }
        } else { return false; }
    }
    
    private Boolean checkMappingFileValidity(String file) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fileContent = mapper.readTree(file);
        if (fileContent.get("singulars").isNull() || fileContent.get("plurals").isNull()) {
            return false;
        } return true;
    }
    
    private void loadSettings() throws IOException{
        if (settings == null){
            if (rootPath == "") {
                determineOS();
            }
            String fileLocation = programFilesPath + "\\" + settingsFileName;
            Path path = Paths.get(fileLocation);
            if (Files.exists(path)) {
                settings = new String(Files.readAllBytes(path));
            } else {
                Iterator<File> localFileIteratorC = FileUtils.iterateFiles(new File(rootPath), extensions, true);
                while(localFileIteratorC.hasNext() && fileLocation == null) {
                    File file = localFileIteratorC.next();
                    if (file.getName().equals(settingsFileName)) { fileLocation = file.getPath(); }
                }
                settings = new String(Files.readAllBytes(Paths.get(fileLocation)));
            }
            ObjectMapper mapper = new ObjectMapper();
            prevOpened = (ArrayNode) mapper.readTree(settings).get("prevOpened");
        }
    }
    
    private String openProject(HttpServletRequest request) throws IOException{ //open project
        Boolean determinationSuccess = determineOS();
        if (!determinationSuccess) { return "Unsupported OS"; }
        loadSettings();
        String fileName = getBody(request);
        String project = null;
        if (fileName.contains("\\")) { 
            fileName = fileName.replace("\\\\", "\\");
            fileName = fileName.substring(1, fileName.length()-1); }
        if (fileName.startsWith(rootPath)) { // fully specified path
            project = new String(Files.readAllBytes(Paths.get(fileName)));
            if (!checkFileValidity(project)) { return "Invalid file, not MIKAT"; }
            currentPath = fileName;
            fileName = fileName.split("/")[fileName.split("/").length-1];
            
        } else { //find file and read
            String pathToFile = null;
            fileName = fileName.substring(1, fileName.length()-1);
            if (workingDir != null) {
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File(workingDir.toString()), extensions, true);
                while (fileIterator.hasNext() && pathToFile == null) {
                    File file = fileIterator.next();
                    if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
                }
            }
            if (pathToFile == null) {
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File(rootPath), extensions, true);
                while (fileIterator.hasNext() && pathToFile == null) {
                    File file = fileIterator.next();
                    if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
                }
            }
            if (pathToFile == null) { return "Invalid file, no path"; }
            project = new String(Files.readAllBytes(Paths.get(pathToFile)));
            if (!checkFileValidity(project)) { return "Invalid file, not MIKAT"; }
            currentPath = pathToFile;
        }
        
        
        Pattern workingDirPattern = Pattern.compile("\"workingDirectory\":\"(.+)\"");
        Pattern statePattern = Pattern.compile("\"state\":(.+),\"e");
        Pattern endLinesPattern = Pattern.compile("\"endLines\":(.+),\"w");
        Pattern dependenciesPattern = Pattern.compile("\"dependencies\":(.+),\"s");
        Matcher workingDirMatcher = workingDirPattern.matcher(project);
        Matcher stateMatcher = statePattern.matcher(project);
        Matcher endLinesMatcher = endLinesPattern.matcher(project);
        Matcher dependenciesMatcher = dependenciesPattern.matcher(project);
        
        Boolean workingDirMatch = workingDirMatcher.find();
        Boolean stateMatch = stateMatcher.find();
        Boolean endLinesMatch = endLinesMatcher.find();
        Boolean dependenciesMatch = dependenciesMatcher.find();
        
        if(!workingDirMatch || !stateMatch || !endLinesMatch || !dependenciesMatch) { return "File is not MIKAT file"; }
        workingDir = Paths.get(workingDirMatcher.group(1));
        currentState = new Pair<>(JSONDecoder.decodeChart(stateMatcher.group(1)), new ArrayList<>(Arrays.asList(endLinesMatcher.group(1).split(","))));
        ObjectMapper mapper = new ObjectMapper();
        dependencies = (ArrayNode) mapper.readTree(dependenciesMatcher.group(1));

        fileName = fileName.split("/")[fileName.split("/").length-1];
        for (int i = 0; i < prevOpened.size(); i++){
            JsonNode node = prevOpened.get(i);
            if ((node.get("fileName").asText()).equals(fileName)) { prevOpened.remove(i); }
        }
        
        updatePrevOpened(fileName);
        setMappingLocations(project);
        return "File opened successfully";
    }
    
    private void updatePrevOpened(String fileName) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode projectFile = mapper.createObjectNode();
        projectFile.put("fileName", fileName);
        projectFile.put("path", currentPath);
        projectFile.put("date", new Date().toString());
        prevOpened.add(projectFile);
        writeSettings();
    }
    
    private void writeSettings() throws IOException {
        String settingsString = "{";
        settingsString += "\"prevOpened\":" + prevOpened.toPrettyString() + "}";
        FileWriter settingsFile = new FileWriter(programFilesPath + "//" + settingsFileName);
        settingsFile.write(settingsString);
        settingsFile.close();
    }
    
    private String selectFile(HttpServletRequest request) throws IOException{
        String fileName = request.getParameter("name");
        File root = new File(workingDir.toString());
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
                    if (checkFileValidity(selectedFileString)){
                        ObjectMapper mapper = new ObjectMapper();
                        selectedFileContents = mapper.readTree(selectedFileString);
                        String title = selectedFileContents.at("/maintenance/title").asText();
                        mlmname = selectedFileContents.at("/maintenance/mlmname").asText();
                        addNewDependency(title, path, selectedFileContents);
                    }
                }
            } 
        } catch (Exception e){}  
        return mlmname;
    }
    
    private void addNewDependency(String dependency, String fileLocation, JsonNode dependencyContents) {
        List<Pair<String,String>> newItems = new ArrayList<>();
        newItems.add(new Pair<>("dependency", dependency));
        newItems.add(new Pair<>("fileLocation", fileLocation));
        newItems.add(new Pair<>("date", new Date().toString()));
        JsonNode newDependency = JsonTools.createNode(newItems);
        dependencies.add(newDependency);
        loadedDependencies.add(new Pair<>(dependency, dependencyContents));
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
                Integer nextIndex = nextElementIndex(prevItemId) + 1; // because we still have to insert
                currentState.getValue0().add(prevItemIndex + 1, newItem);
                ChartItem nextItem = currentState.getValue0().get(nextIndex);
                nextItem.setPrevItemId(id);
                currentState.getValue0().set(nextIndex, nextItem);
            }
        }
    }
    
    private Integer nextElementIndex(String id){
        Integer nextIndex = -1;
        for (Integer index = 0; index < currentState.getValue0().size(); index++){
            if ((currentState.getValue0().get(index).getPrevItemId()).equals(id)){
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
    
    private String getLocalMapping() throws IOException{
        if (localMapping.equals("{}")) {
            localMapping = new String(Files.readAllBytes(Paths.get(localMappingsFileLocation)));
            if (localMapping.equals("") || !checkMappingFileValidity(localMapping)) { localMapping = "{}"; }
        }
        return localMapping;
    }
    
    private String getStandardizedMapping() throws IOException{
         if (standardizedMapping.equals("{}")) {
            String fileLocation = standardizedMappingsFileLocation;
            standardizedMapping = new String(Files.readAllBytes(Paths.get(fileLocation)));
            if (standardizedMapping.equals("") || !checkMappingFileValidity(standardizedMapping)) { standardizedMapping = "{}"; }
        }
        return standardizedMapping;
    }
    
    private void saveChanges() throws IOException {
        String currentProject = new String(Files.readAllBytes(Paths.get(currentPath)));
        String properties = currentProject.split(",\"dependencies\"")[0];
        properties += ",\"dependencies\":" + dependencies.toString() + ",\"state\":" + JSONEncoder.encodeChart(currentState.getValue0()) + ",\"endLines\":" + currentState.getValue1().toString() + ",\"workingDirectory\":\"" + workingDir.toString().replace("\\", "\\\\") + "\"}";
        Pattern pattern = Pattern.compile("\"mlmname\":\"(.+)\",\"ar");
        Matcher matcher = pattern.matcher(properties);
        Boolean matchFound = matcher.find();
        String mlmname = "";
        if (matchFound) { 
            mlmname = matcher.group(1);
            String fileLocation = workingDir + "\\" + mlmname + ".json";
            currentPath = fileLocation;
            FileWriter file = new FileWriter(fileLocation);
            file.write(properties);
            file.close();
            setMappingLocations(properties);
        }
    }
    // helper functions
    
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
    
    public static String getBody(HttpServletRequest request) throws IOException {
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }
}

