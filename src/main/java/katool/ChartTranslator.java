/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;

/**
 *
 * @author RLvan
 */
public class ChartTranslator {
    public static String translateToJS(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState) throws IOException{
        LinkedList<ChartItem> state = currentState.getValue0();
        ArrayList<String> variables = getVariablesForFunction(state);
        String functionJS = "let actions = [];";
        String endFunction = "return actions;";
        functionJS += processStateJS(state) + endFunction;
        System.out.println(JSONEncoder.encodeChart(state));
//        if (Utils.workingDir == null && Utils.defaultWorkingDirectory == null) { Utils.loadSettings(); }
//        FileWriter file;
//        String fileLocation = null;
//        if (Utils.workingDir == null) {
//            fileLocation = Utils.defaultWorkingDirectory + "\\" + "chartJS.js";
//            file = new FileWriter(fileLocation);
//        } else {
//            fileLocation = Utils.workingDir + "\\" + "chartJS.js";
//            file = new FileWriter(fileLocation);
//        }
//        file.write(functionJS);
//        file.close();
//        return fileLocation;
        System.out.println(functionJS);
        return "{\"parameters\": " + variables.toString() + ", \"code\": \"" + functionJS + "\"}";
    }
    
    public static void translateToArdenSyntax(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState){}
    
    private static String processStateJS(LinkedList<ChartItem> state) throws IOException {
        String code = "";
        ArrayList<String> conditionalIds = new ArrayList<>();
        ArrayList<String> lastElseIds = new ArrayList<>();
        for (ChartItem item : state) {
            if (conditionalIds.contains(item.getPrevItemId()) && (item.getCondition() == null || (item.getCondition()).equals("null"))) { 
                code += "} else {"; 
                lastElseIds.add(item.getId());
            }
            switch (item.getType()){
                case "start":
                    break;
                case "end":
                    break;
                case "subroutine":
                    Pair<Pair<LinkedList<ChartItem>, ArrayList<String>>, String> subroutineFileContent = getFileContent(item.getCaption());
                    String result = processStateJS(subroutineFileContent.getValue0().getValue0());
                    if (!result.equals("")) { code += result; }
                    break;
                case "conditional":
                    LinkedList<ChartItem> conditionalItems = Utils.conditionalItems(item.getId(), state);
                    code += "if (" + item.getCaption() + " " + conditionalItems.get(0).getCondition() + ") {";
                    conditionalIds.add(item.getId());
                    break;
                case "loop":
                    if (item.getCaption().startsWith("End for")) { code += "});"; }
                    else { code += item.getCaption() + ".forEach(element => {"; }
                    break;
                case "retrievedata":
                    break;
                case "newProcedure":
                case "orderLabs":
                case "newPrescription":
                case "addDiagnosis":
                case "newVaccination":
                case "addNotes":
                    String msg = getMessage(item.getType(), item.getCaption());
                    code += "actions.push('" + msg + "');";
                    break;
                case "questionMark":
                    break;
             }
            if (lastElseIds.contains(item.getPrevItemId())) { 
                lastElseIds.remove(item.getPrevItemId());
                lastElseIds.add(item.getId()); 
            }
            else { if (!lastElseIds.isEmpty() && !item.getType().equals("conditional")) {
                code += "}"; 
                lastElseIds.remove(item.getId());
            }}
         }
        return code;
    }
    
    private static String getMessage(String type, String caption){
        switch(type){
            case "newProcedure":
                return "New procedure: " + caption;
            case "orderLabs":
                return "Order labs: " + caption;
            case "newPrescription":
                return "New prescription: " + caption;
            case "addDiagnosis":
                return "Add diagnosis: " + caption;
            case "newVaccination":
                return "New vaccination: " + caption;
            case "addNotes":
                return "Add notes: " + caption;
            default:
                return null;
        }
    }
    
    private static Pair<Pair<LinkedList<ChartItem>, ArrayList<String>>, String> getFileContent(String fileName) throws IOException{
        String fileContent = Utils.findAndReadFile(fileName, Utils.workingDir.toString());
        String workingDir = null;
        String state = null;
        ArrayList<String> endLines = new ArrayList<>();
        Pattern workingDirPattern = Pattern.compile("\"workingDirectory\":\"(.+)\"");
        Pattern statePattern = Pattern.compile("\"state\":(.+),\"e");
        Pattern endLinesPattern = Pattern.compile("\"endLines\":(.+),\"w");
        Matcher workingDirMatcher = workingDirPattern.matcher(fileContent);
        Matcher stateMatcher = statePattern.matcher(fileContent);
        Matcher endLinesMatcher = endLinesPattern.matcher(fileContent);
        workingDirMatcher.find();
        stateMatcher.find();
        endLinesMatcher.find();
        try {
            workingDir = workingDirMatcher.group(1);
            state = stateMatcher.group(1);
            endLines = new ArrayList<>(Arrays.asList(endLinesMatcher.group(1).split(",")));
            
        } catch (Exception e) {
            throw new InvalidPropertiesFormatException("File format invalid");
        }
        Pair<LinkedList<ChartItem>, ArrayList<String>> currentState = new Pair<>(JSONDecoder.decodeChart(state), endLines);
        return new Pair<>(currentState, workingDir);
    }
    
    private static ArrayList<String> getVariablesForFunction(LinkedList<ChartItem> state) throws IOException{
        ArrayList<String> variables = new ArrayList<>();
        for (ChartItem item : state) {
            if (item.getType().equals("retrievedata")) { variables.add("\"" + item.getCaption() + "\""); }
            else if (item.getType().equals("subroutine")){
                LinkedList<ChartItem> subroutineState = getFileContent(item.getCaption()).getValue0().getValue0();
                ArrayList<String> variablesSubroutine = getVariablesForFunction(subroutineState);
                variables.addAll(variablesSubroutine);
            }
        }
        return variables;
    }
}
