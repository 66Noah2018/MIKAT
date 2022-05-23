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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static katool.Utils.extensions;
import static katool.Utils.workingDir;
import org.apache.commons.io.FileUtils;
import org.apache.maven.surefire.shade.booter.org.apache.commons.lang3.StringUtils;
import org.javatuples.*;

/**
 *
 * @author RLvanBrummelen
 */
public class ChartTranslator {
    private static Boolean ardenHasReturns = false;
    private static Boolean ardenHasMedicalActions = false;
    public static String translateToJS(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState, String mlmname) throws IOException{
        LinkedList<ChartItem> state = currentState.getValue0();
        ArrayList<String> endlines = currentState.getValue1();
        String functionJS = "let actions = [];";
        String endFunction = "return actions;";
        Triplet<String, ArrayList<String>, ArrayList<String>> result = processStateJS(state, endlines, mlmname);
        for (int i = 0; i < result.getValue2().size(); i++) {
            functionJS += result.getValue2().get(i) + ";";
        }
        functionJS += result.getValue0() + endFunction;
        ArrayList<String> variables = result.getValue1();
        return "{\"parameters\": " + variables.toString() + ", \"code\": \"" + functionJS + "\"}";
    }
    
    public static String translateToArdenSyntax(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState, String projectProperties) throws IOException{
        Pattern pattern = Pattern.compile("\"mlmname\":\"(.+)\",\"ar");
        Matcher matcher = pattern.matcher(projectProperties);
        Boolean matchFound = matcher.find();
        String mlmname = "";
        String exportPath = "";
        if (matchFound) { 
            mlmname = matcher.group(1);
            exportPath = Paths.get(Utils.workingDir.toString(), mlmname + ".mlm").toString();
            File file = new File(exportPath);
            FileWriter writer = new FileWriter(file);
            writeMaintenanceAndLibrary(projectProperties, writer);
            writer.write("knowledge:\n");
            writer.write("\ttype: data_driven;;\n");
            writer.write("\tdata:\n");
            writeDataSlot(currentState, projectProperties, writer);
            writer.write("\t;;\n");
            writer.write("evoke:\n");
            writeTriggers(projectProperties, writer);
            writer.write(";;\n");
            writer.write("logic:\n");
            writeState(currentState, writer);
            writer.write("\tCONCLUDE true;\n");
            writer.write(";;\n");
            writer.write("action:\n");
            writeReturns(writer);
            writer.write(";;\n");
            writer.write("end:");
            
            writer.flush();
            writer.close();
        }
        return exportPath;
    }
    
    // TranslateJS helper functions
    private static Triplet<String, ArrayList<String>, ArrayList<String>> processStateJS(LinkedList<ChartItem> state, ArrayList<String> endlines, String mlmname) throws IOException {
        String code = "";
        ArrayList<String> letsToAdd = new ArrayList<>();
        ArrayList<String> conditionalIds = new ArrayList<>();
        ArrayList<String> lastElseIds = new ArrayList<>();
        ArrayList<String> variables = new ArrayList<>();
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
                    Pair<LinkedList<ChartItem>, ArrayList<String>> projectString = Utils.getDependency(item.getCaption());
                    if (projectString.getValue0().isEmpty()) { break; }
                    Triplet<String, ArrayList<String>, ArrayList<String>> result = processStateJS(projectString.getValue0(), projectString.getValue1(), item.getCaption());
                    if (!result.getValue0().equals("")) { code += result.getValue0(); }
                    if (!result.getValue1().isEmpty()) { variables.addAll(result.getValue1()); }
                    if (!result.getValue2().isEmpty()) { letsToAdd.addAll(result.getValue2()); }
                    break;
                case "conditional":
                    LinkedList<ChartItem> conditionalItems = Utils.conditionalItems(item.getId(), state);
                    String condition = prepCondition(conditionalItems.get(0).getCondition());
                    code += "if (" + item.getCaption() + " " + condition + ") {";
                    conditionalIds.add(item.getId());
                    break;
                case "loop":
                    if (item.getCaption().startsWith("End for")) { code += "});"; }
                    else { code += item.getCaption() + ".forEach(element => {"; }
                    break;
                case "retrievedata":
                    variables.add("\"" + item.getCaption() + "\"");
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
                case "returnValue":
                    String returnValue = item.getCaption();
                    try {
                        Float.parseFloat(returnValue);
                    } catch (NumberFormatException e) {
                        if (!returnValue.equals("true") && !returnValue.equals("false")) { returnValue = "\"" + returnValue + "\""; }
                    }
                    code += mlmname + " = " + returnValue + ";";
                    letsToAdd.add("let " + mlmname);
                    break;
            }
            if (lastElseIds.contains(item.getPrevItemId())) { 
                lastElseIds.remove(item.getPrevItemId());
                lastElseIds.add(item.getId()); 
            }
            else { if (!lastElseIds.isEmpty() && !item.getType().equals("conditional")) {
                code += "}"; 
                lastElseIds.remove(item.getId());
                conditionalIds.remove(conditionalIds.size() - 1);
            }}
        }
        for (int i = 0; i < conditionalIds.size(); i++) { code += "}"; }
        return new Triplet<String, ArrayList<String>, ArrayList<String>>(code, variables, letsToAdd);
    }
    
    private static String prepCondition (String condition) {
        String preppedCondition = "";
        Pattern conditionPattern = Pattern.compile("(===|<=|>=|<|>|!==|is-in|is-not-in) (.+)");
        Matcher conditionMatcher = conditionPattern.matcher(condition);
        Boolean found = conditionMatcher.find();
        if (found) {
            preppedCondition += conditionMatcher.group(1);
            String conditionPart = conditionMatcher.group(2);
            if (!StringUtils.isNumericSpace(conditionPart.strip()) && !conditionPart.strip().equals("true") && !conditionPart.strip().equals("false")) { preppedCondition += "\"" + conditionPart + "\""; }
            else { preppedCondition += conditionPart; }
            return preppedCondition;
        }
        return condition;
    }
    
    private static String prepConditionForArdenSyntax (String condition){
        String preppedCondition = "";
        Pattern conditionPattern = Pattern.compile("(===|<=|>=|<|>|!==|is-in|is-not-in) (.+)");
        Matcher conditionMatcher = conditionPattern.matcher(condition);
        Boolean found = conditionMatcher.find();
        if (found) {
            switch(conditionMatcher.group(1)){
                case "===":
                    preppedCondition += "= ";
                    break;
                case "<=":
                case ">=":
                case "<":
                case ">":
                    preppedCondition += conditionMatcher.group(1) + " ";
                    break;
                case "!==":
                    preppedCondition += "!= ";
                    break;
                case "is-in":
                    preppedCondition += "IS IN ";
                    break;
                case "is-not-in":
                    preppedCondition += "IS NOT IN ";
                    break;
                default: 
                    break;
            }
            String conditionPart = conditionMatcher.group(2);
            if (StringUtils.isNumericSpace(conditionPart.strip())) { preppedCondition = "AS NUMBER " + preppedCondition + conditionPart; }
            else if (conditionPart.strip().equals("true") || conditionPart.strip().equals("True") || conditionPart.strip().equals("TRUE")) { preppedCondition = "AS NUMBER " + preppedCondition + "1"; }
            else if (conditionPart.strip().equals("false") || conditionPart.strip().equals("False") || conditionPart.strip().equals("FALSE")) { preppedCondition = "AS NUMBER " + preppedCondition + "0"; }
            else { preppedCondition += "\"" + conditionPart + "\""; }
            return preppedCondition;
        }
        return condition;
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
    
    // TranslateAS helper functions
    private static void processSubroutine(String mlmname, String projectProperties, FileWriter writer) throws IOException{
        String fileLocation = null;
        Pattern dependenciesPattern = Pattern.compile("\"dependencies\":[^a-z](.+)[^a-z],\"st");
        Matcher dependenciesMatcher = dependenciesPattern.matcher(projectProperties);
        Boolean dependenciesMatch = dependenciesMatcher.find();
        if (dependenciesMatch) {
            String[] dependencies = dependenciesMatcher.group(1).split("[^a-z],[^a-z]\"");
            for (String dependency : dependencies) {
                if (dependency.contains("\"dependency\":\"" + mlmname)) {
                    Pattern fileLocationPattern = Pattern.compile("\"fileLocation\":\"(.+)\",\"date");
                    Matcher fileLocationMatcher = fileLocationPattern.matcher(dependency);
                    Boolean fileLocationMatch = fileLocationMatcher.find();
                    if (fileLocationMatch) {
                        fileLocation = fileLocationMatcher.group(1);
                    }
                }
            }
            if (fileLocation != null) {
                // always overwrite, because we cannot check for mappings changes
                String fileContent = new String(Files.readAllBytes(Paths.get(fileLocation)));
                Pattern statePattern = Pattern.compile("\"state\":(.+),\"e");
                Pattern endLinesPattern = Pattern.compile("\"endLines\":(.+),\"w");
                Matcher stateMatcher = statePattern.matcher(fileContent);
                Matcher endLinesMatcher = endLinesPattern.matcher(fileContent);
                Boolean stateMatch = stateMatcher.find();
                Boolean endLinesMatch = endLinesMatcher.find();
                if (stateMatch && endLinesMatch) {
                    Pair<LinkedList<ChartItem>, ArrayList<String>> subroutineState = new Pair<>(JSONDecoder.decodeChart(stateMatcher.group(1)), new ArrayList<>(Arrays.asList(endLinesMatcher.group(1).replace("[", "").replace("]", "").split(", "))));
                    translateToArdenSyntax(subroutineState, fileContent);
                }
                writer.write("\t\t" + mlmname + "_call:= MLM \'" + mlmname + "\';\n");
                writer.write("\t\t" + mlmname + ":= CALL " + mlmname + "_call;\n"); // only support for MLMs that do not take parameters!
            } // otherwise the file is corrupted as a dependency must have been removed manually
        } // if there are no dependencies, the file is corrupted and this code should never be run
    }
    
    private static void writeMaintenanceAndLibrary(String projectProperties, FileWriter writer) throws IOException{
        Pattern maintenancePattern = Pattern.compile("\"title\":\"(.*|.+)\",\"mlmname\":\"(.*|.+)\",\"arden\":\"(.*|.+)\",\"version\":\"(.*|.+)\",\"institution\":\"(.*|.+)\",\"author\":\"(.*|.+)\",\"specialist\":\"(.*|.+)\",\"date\":\"(.*|.+)\",\"validation\":\"(.*|.+)\"},\"li");
        Matcher maintenanceMatcher = maintenancePattern.matcher(projectProperties);
        Boolean maintenanceMatch = maintenanceMatcher.find();

        Pattern libraryPattern = Pattern.compile("\"purpose\":\"(.*|.+)\",\"explanation\":\"(.*|.+)\",\"keywords\":\"(.*|.+)\",\"citations\":\"(.*|.+)\",\"links\":\"(.*|.+)\"[^a-z],\"l"); // for some reason \} or \S are not recognized, therefore [^a-z]
        Matcher libraryMatcher = libraryPattern.matcher(projectProperties);
        Boolean libraryMatch = libraryMatcher.find();

        if (maintenanceMatch && libraryMatch){
            writer.write("maintenance:\n");
            writer.write("\ttitle: " + maintenanceMatcher.group(1) + ";;\n");
            writer.write("\tmlmname: " + maintenanceMatcher.group(2) + ";;\n");
            writer.write("\tarden: " + maintenanceMatcher.group(3) + ";;\n");
            writer.write("\tversion: " + maintenanceMatcher.group(4) + ";;\n");
            writer.write("\tinstitution: " + maintenanceMatcher.group(5) + ";;\n");
            writer.write("\tauthor: " + maintenanceMatcher.group(6) + ";;\n");
            writer.write("\tspecialist: " + maintenanceMatcher.group(7) + ";;\n");
            writer.write("\tdate: " + maintenanceMatcher.group(8) + ";;\n");
            writer.write("\tvalidation: " + maintenanceMatcher.group(9) + ";;\n");
            
            writer.write("library:\n");
            writer.write("\tpurpose: " + StringUtils.stripAccents(libraryMatcher.group(1)) + ";;\n");
            writer.write("\texplanation: " + StringUtils.stripAccents(libraryMatcher.group(2)) + ";;\n");
            writer.write("\tkeywords: " + libraryMatcher.group(3).replaceAll(",", "; ") + ";;\n");
            writer.write("\tcitations: " + libraryMatcher.group(4) + ";;\n");
            String linksString = "\tlinks:\n\t\t'" + (libraryMatcher.group(5) + ",").replaceAll(",", "';\n\t\t");
            linksString = linksString.substring(0, linksString.length()-1) + ";;\n";
            writer.write(linksString);
        }
    }
    
    private static void writeDataSlot(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState, String projectProperties, FileWriter writer) throws IOException{
        ArrayList<String> dataToRetrieve = new ArrayList<>();
        ArrayList<String> dataForLoops = new ArrayList<>();
        for (ChartItem item : currentState.getValue0()){
            if (item.getType().equals("retrievedata")) { dataToRetrieve.add(item.getCaption()); }
            if (item.getType().equals("loop") && !item.getCaption().startsWith("End for")) { dataForLoops.add(item.getCaption()); }
            if (item.getType().equals("subroutine")) { processSubroutine(item.getCaption(), projectProperties, writer); }
        }
        Pattern localMappingPattern = Pattern.compile("localMappingFile\":\"(.+)\",\"sta");
        Matcher localMappingMatcher = localMappingPattern.matcher(projectProperties);
        Boolean localMappingMatch = localMappingMatcher.find();
        
        if (localMappingMatch){
            Map<String, String> mappings = Utils.getLocalMappingDictionary(localMappingMatcher.group(1));
            for (String dataItem : dataToRetrieve) {
                if (mappings.containsKey(dataItem)) {
                    writer.write("\t\t" + dataItem + ":= READ FIRST {" + mappings.get(dataItem).replaceAll("\\\"", "'") + "};\n");
                }
            }
            for (String loopItem : dataForLoops) {
                if (mappings.containsKey(loopItem)) {
                    writer.write("\t\t" + loopItem + ":= READ FIRST {" + mappings.get(loopItem).replaceAll("\\\"", "'") + "};\n");
                }
            }
        }
        
        // data for triggers
        Pattern triggersPattern = Pattern.compile("\"triggers\":[^a-z](.+)[^a-z],\"dependencies");
        Matcher triggersMatcher = triggersPattern.matcher(projectProperties);
        Boolean triggersMatch = triggersMatcher.find();
        
        if (triggersMatch){
            String triggersString = triggersMatcher.group(1);
            String[] triggers = triggersString.substring(1, triggersString.length() - 1).split(",");
            for (String trigger : triggers) {
                String[] triggerItems = trigger.split(":");
                String key = triggerItems[0].replaceAll("\"", "");
                String value = triggerItems[1].replaceAll("\"", "");
                writer.write("\t\t" + key + ":= event {" + value + "};\n");
            }
        }
    }
    
    private static void writeTriggers(String projectProperties, FileWriter writer) throws IOException{
        Pattern triggersPattern = Pattern.compile("\"triggers\":[^a-z](.+)[^a-z],\"dependencies");
        Matcher triggersMatcher = triggersPattern.matcher(projectProperties);
        Boolean triggersMatch = triggersMatcher.find();
        
        if (triggersMatch){
            String triggersString = triggersMatcher.group(1);
            String[] triggers = triggersString.substring(1, triggersString.length() - 1).split(",");
            ArrayList<String> triggerNames = new ArrayList<>();
            for (String trigger : triggers) {
                String[] triggerItems = trigger.split(":");
                triggerNames.add(triggerItems[0].replaceAll("\"", ""));
            }
            
            String triggerNamesString = triggerNames.stream().map(Object::toString).collect(Collectors.joining(" OR "));
            writer.write("\t" + triggerNamesString + ";\n");
        }
    }
    
    private static void writeState(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState, FileWriter writer) throws IOException{
        ardenHasMedicalActions = false;
        ardenHasReturns = false;
        Integer nrOfTabs = 1;
        ArrayList<String> conditionalIds = new ArrayList<>();
        ArrayList<String> lastElseIds = new ArrayList<>();
        writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "medical_actions_list:= \"\";\n");
        writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "return_value:= NULL;\n");
        for (ChartItem item : currentState.getValue0()) {
            if (conditionalIds.contains(item.getPrevItemId()) && (item.getCondition() == null || (item.getCondition()).equals("null"))) { 
                if (currentState.getValue1().contains("\"" + item.getId() + "\"")) {
                    writer.write(String.join("", Collections.nCopies(nrOfTabs-1, "\t")) + "ELSE\n");
                    lastElseIds.add(item.getId());
                }
            }
            switch (item.getType()){
                case "conditional":
                    LinkedList<ChartItem> conditionalItems = Utils.conditionalItems(item.getId(), currentState.getValue0());
                    String condition = prepConditionForArdenSyntax(conditionalItems.get(0).getCondition());
                    writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "IF " + item.getCaption() + " " + condition + " THEN\n");
                    nrOfTabs++;
                    conditionalIds.add(item.getId());
                    break;
                case "loop":
                    if (item.getCaption().startsWith("End for")) { 
                        nrOfTabs--;
                        writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "ENDDO;\n"); 
                    }
                    else { 
                        writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "FOR x in " + item.getCaption() + " DO\n"); // it's not yet possible to interact with the set that is used for the loop
                        nrOfTabs++;
                    }
                    break;
                case "newProcedure":
                case "orderLabs":
                case "newPrescription":
                case "addDiagnosis":
                case "newVaccination":
                case "addNotes":
                    writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "medical_actions_list:= medical_actions_list || \";\" || \"" + getMessage(item.getType(), item.getCaption()) + "\";\n");
                    ardenHasMedicalActions = true;
                    break;
                case "returnValue":
                    String caption = item.getCaption();
                    if (caption.equals("true") || caption.equals("True") || caption.equals("TRUE")) { writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "return_value:= TRUE;\n"); }
                    else if (caption.equals("false") || caption.equals("False") || caption.equals("FALSE")) { writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "return_value:= FALSE;\n"); }
                    else {
                        try{
                            Double captionNum = Double.parseDouble(caption);
                            writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "return_value:= " + captionNum + ";\n");
                        } catch (NumberFormatException e) {
                            writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "return_value:= \"" + caption + "\";\n");
                        }
                    }
                    ardenHasReturns = true;
                    break;
                default:
                    break;
            }
            if (lastElseIds.contains(item.getPrevItemId())) { 
                lastElseIds.remove(item.getPrevItemId());
                lastElseIds.add(item.getId()); 
            }
            else { if (!lastElseIds.isEmpty() && !item.getType().equals("conditional")) {
                nrOfTabs--;
                writer.write(String.join("",  Collections.nCopies(nrOfTabs, "\t")) + "ENDIF;\n");
                lastElseIds.remove(item.getId());
                conditionalIds.remove(conditionalIds.size() - 1);
            }}
        }
        for (int i = 0; i < conditionalIds.size(); i++) { 
            nrOfTabs--;
            writer.write(String.join("", Collections.nCopies(nrOfTabs, "\t")) + "ENDIF;\n");
        }
    }
    
    private static void writeReturns(FileWriter writer) throws IOException{
        if (ardenHasMedicalActions) { writer.write("\twrite medical_actions_list;\n"); }
        if (ardenHasReturns) { writer.write("\treturn return_value;\n"); }
    }
}
