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
package katool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.surefire.shade.booter.org.apache.commons.lang3.StringUtils;
import org.javatuples.*;

/**
 *
 * @author RLvanBrummelen
 */
public class ChartTranslator {
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
    
    public static void translateToArdenSyntax(Pair<LinkedList<ChartItem>, ArrayList<String>> currentState){}
    
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
        Pattern conditionPattern = Pattern.compile("/(===|<=|>=|<|>|!==|is-in|is-not-in) (.+)/");
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
    
//    private static Pair<Pair<LinkedList<ChartItem>, ArrayList<String>>, String> getFileContent(String fileName) throws IOException{
//        String fileContent = Utils.findAndReadFile(fileName, Utils.workingDir.toString());
//        String workingDir = null;
//        String state = null;
//        ArrayList<String> endLines = new ArrayList<>();
//        Pattern workingDirPattern = Pattern.compile("\"workingDirectory\":\"(.+)\"");
//        Pattern statePattern = Pattern.compile("\"state\":(.+),\"e");
//        Pattern endLinesPattern = Pattern.compile("\"endLines\":(.+),\"w");
//        Matcher workingDirMatcher = workingDirPattern.matcher(fileContent);
//        Matcher stateMatcher = statePattern.matcher(fileContent);
//        Matcher endLinesMatcher = endLinesPattern.matcher(fileContent);
//        workingDirMatcher.find();
//        stateMatcher.find();
//        endLinesMatcher.find();
//        try {
//            workingDir = workingDirMatcher.group(1);
//            state = stateMatcher.group(1);
//            endLines = new ArrayList<>(Arrays.asList(endLinesMatcher.group(1).replace("[", "").replace("]", "").split(", ")));
//            
//        } catch (Exception e) {
//            throw new InvalidPropertiesFormatException("File format invalid");
//        }
//        Pair<LinkedList<ChartItem>, ArrayList<String>> currentState = new Pair<>(JSONDecoder.decodeChart(state), endLines);
//        return new Pair<>(currentState, workingDir);
//    }
}
