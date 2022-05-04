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
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.shade.booter.org.apache.commons.lang3.SystemUtils;
import org.javatuples.Pair;

/**
 *
 * @author RLvan
 */
public class Utils {
    
    public final static String[] extensions = new String[]{"json","csv"};
    public static String rootPath = "";
    public static String programFilesPath = "";
    public static Path workingDir = null;
    public static String currentPath = "";
    public final static String settingsFileName = "mikat_settings.json";
    public static ArrayNode prevOpened = new ObjectMapper().createArrayNode();
    public static ArrayNode dependencies = new ObjectMapper().createArrayNode(); // [{dependency: ..., fileLocation: ..., date: ...},{}]
    private static ArrayList<Pair<String, JsonNode>> loadedDependencies = new ArrayList<>();
    public static String defaultWorkingDirectory = null;
    private static String settings = null;
    private static ArrayList<JsonNode> subroutineDependencies = new ArrayList<>();
    
    public static String findAndReadFile(String fileName, String workingDir) throws IOException {
        String pathToFile = null;
        if (workingDir != null) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(new File(workingDir), extensions, true);
            while (fileIterator.hasNext() && pathToFile == null) {
                File file = fileIterator.next();
                if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
            }
        }
        if (defaultWorkingDirectory == null) {
            loadSettings();
            Iterator<File> fileIterator = FileUtils.iterateFiles(new File(defaultWorkingDirectory), extensions, true);
            while (fileIterator.hasNext() && pathToFile == null) {
                File file = fileIterator.next();
                if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
            }
        }
        if (pathToFile == null) {
            if (rootPath.equals("")) { determineOS(); }
            Iterator<File> fileIterator = FileUtils.iterateFiles(new File(rootPath), extensions, true);
            while (fileIterator.hasNext() && pathToFile == null) {
                File file = fileIterator.next();
                if (file.getName().equals(fileName)) { pathToFile = file.getPath(); }
            }
        }
        if (pathToFile == null) { return "Invalid file, no path"; }
        String project = new String(Files.readAllBytes(Paths.get(pathToFile)));
        if (!checkFileValidity(project)) { return "Invalid file, not MIKAT"; }
        currentPath = pathToFile;
        return project;
    }
       
    public static Boolean determineOS(){
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
    
    public static Boolean checkFileValidity(String projectString) throws JsonProcessingException{
        if (projectString.startsWith("\"")) { projectString = projectString.substring(1, projectString.length()); }
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
                                            return !standardizedMappingFile.isNull();
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
    
    public static String readProjectFromFile() throws IOException{
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
    
    public static Boolean nextIsEnd(String id, LinkedList<ChartItem> state){
        ChartItem nextItem = null;
        for (ChartItem element : state) {
            if (id.equals(element.getPrevItemId())) { nextItem = element; }
        }
        if (nextItem != null) {
            return nextItem.getType().equals("end");
        } else { return false; }
    }
    
    public static Boolean checkMappingFileValidity(String file) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fileContent = mapper.readTree(file);
        if (fileContent.get("singulars").isNull() || fileContent.get("plurals").isNull()) {
            return false;
        } return true;
    }
    
    public static void updatePrevOpened(String fileName) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode projectFile = mapper.createObjectNode();
        projectFile.put("fileName", fileName);
        projectFile.put("path", currentPath);
        projectFile.put("date", new Date().toString());
        prevOpened.insert(0, projectFile);
        if (prevOpened.size() > 5) { prevOpened.remove(5); }
        writeSettings();
    }
    
    public static void writeSettings() throws IOException {
        String settingsString = "{";
        settingsString += "\"prevOpened\":" + prevOpened.toPrettyString() + ",\"defaultWorkingDirectory\":\"" + defaultWorkingDirectory.replace("\\", "\\\\") + "\"}";
        FileWriter settingsFile = new FileWriter(programFilesPath + "//" + settingsFileName);
        settingsFile.write(settingsString);
        settingsFile.close();
    }
    
    public static void addNewDependency(String dependency, String fileLocation, JsonNode dependencyContents) {
        List<Pair<String,String>> newItems = new ArrayList<>();
        newItems.add(new Pair<>("dependency", dependency));
        newItems.add(new Pair<>("fileLocation", fileLocation));
        newItems.add(new Pair<>("date", new Date().toString()));
        JsonNode newDependency = JsonTools.createNode(newItems);
        dependencies.add(newDependency);
        loadedDependencies.add(new Pair<>(dependency, dependencyContents));
    }
    
    public static Pair<LinkedList<ChartItem>, ArrayList<String>> getDependency(String dependency) throws IOException {
        String fileLocation = null;
        for (JsonNode dependencyNode : dependencies) {
            if ((dependencyNode.get("dependency").asText()).equals(dependency)) {
                fileLocation = dependencyNode.get("fileLocation").asText();
                break;
            }
        }
        if (fileLocation == null) {
            for (JsonNode dependencyNode : subroutineDependencies) {
                if ((dependencyNode.get("dependency").asText()).equals(dependency)) {
                    fileLocation = dependencyNode.get("fileLocation").asText();
                    break;
                }
            }
        }
        String dependencyContent = "";
        if (fileLocation != null) { dependencyContent = new String(Files.readAllBytes(Paths.get(fileLocation))); }
        else { dependencyContent = findAndReadFile(dependency + ".json", null); }
        
        Pattern statePattern = Pattern.compile("\"state\":(.+),\"e");
        Pattern dependenciesPattern = Pattern.compile("\"dependencies\":(.+),\"s");
        Pattern endLinesPattern = Pattern.compile("\"endLines\":(.+),\"w");
        Matcher stateMatcher = statePattern.matcher(dependencyContent);
        Matcher dependenciesMatcher = dependenciesPattern.matcher(dependencyContent);
        Matcher endLinesMatcher = endLinesPattern.matcher(dependencyContent);
        Boolean stateMatch = stateMatcher.find();
        Boolean dependenciesMatch = dependenciesMatcher.find();
        Boolean endLinesMatch = endLinesMatcher.find();
        if (dependenciesMatch) {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode dependenciesNode = (ArrayNode) mapper.readTree(dependenciesMatcher.group(1));
            for (JsonNode node : dependenciesNode) { subroutineDependencies.add(node); }
        }
        if (stateMatch && endLinesMatch) { return new Pair<>(JSONDecoder.decodeChart(stateMatcher.group(1)), new ArrayList<>(Arrays.asList(endLinesMatcher.group(1).replace("[", "").replace("]", "").split(", ")))); }
        else { return new Pair<>(new LinkedList<>(), new ArrayList<>()); }
    }
    
    public static Integer nextElementIndex(String id, LinkedList<ChartItem> state){
        Integer nextIndex = -1;
        for (Integer index = 0; index < state.size(); index++){
            if ((state.get(index).getPrevItemId()).equals(id)){
                nextIndex = index;
                break;
            }
        }
        return nextIndex;
    }
    
    public static ChartItem getNextElement(String id, LinkedList<ChartItem> state) {
        return state.get(nextElementIndex(id, state));
    }
    
    public static Integer findIndexOf(String id, LinkedList<ChartItem> state){
        Integer itemIndex = -1; // -1 = does not exist
        for (Integer index = 0; index < state.size(); index++){
            if (id.equals(state.get(index).getId())){
                itemIndex = index;
                break;
            }
        }
        return itemIndex;
    }
    
    public static Integer findPrevIdIndex(String prevId, LinkedList<ChartItem> state){
        Integer prevItemIndex = -1;
        for (Integer index = 0; index < state.size(); index++){
            if (prevId.equals(state.get(index).getId())){
                prevItemIndex = index;
                break;
            }
        }
        return prevItemIndex;
    }
    
    public static String ALToString(ArrayList<String> input){
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
    
    public static Pair<LinkedList<ChartItem>, ArrayList<String>> deepCopyCurrentState(Pair<LinkedList<ChartItem>, ArrayList<String>> state) {
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
    
    public static Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> deepCopyDeque(Deque<Pair<LinkedList<ChartItem>, ArrayList<String>>> deque){
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
    
    public static void loadSettings() throws IOException{
        if (settings == null || defaultWorkingDirectory == null){
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
            if (fileLocation == null) { writeSettings(); }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode settingsNode = mapper.readTree(settings);
            prevOpened = (ArrayNode) settingsNode.get("prevOpened");
            defaultWorkingDirectory = settingsNode.get("defaultWorkingDirectory").asText();
        }
    }
    
    public static LinkedList<ChartItem> conditionalItems(String id, LinkedList<ChartItem> state){
        LinkedList<ChartItem> nextItems = new LinkedList<>();
        for (ChartItem element : state) {
            if (id.equals(element.getPrevItemId())) { nextItems.add(element); }
        }
        return nextItems;
    }
}
