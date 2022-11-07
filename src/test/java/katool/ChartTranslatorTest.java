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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import junit.framework.TestCase;
import static katool.Utils.determineOS;
import static katool.Utils.rootPath;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author RLvan
 */
public class ChartTranslatorTest extends TestCase {
    
    private static ServletTester tester;
    private static HttpTester request;
    private static HttpTester response;
    private final static String[] extensions = new String[] {"js"};
    private final static String fileName = "chartJS.js";

    @BeforeAll
    public static void setUpClass() throws Exception {
        tester = new ServletTester();
        tester.addServlet(katool.chartservlet.class, "/katool");
        tester.start();  
        
        request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        if (Utils.workingDir == null) { // set to fixed value
            request.setURI("/katool?function=setWorkingDirectory");
            request.setContent("C:\\Users\\RLvan\\OneDrive\\Documenten\\MI\\SRP\\Test files");
            request.generate();
        }
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        response = new HttpTester();
    }
    
    @AfterEach
    public void tearDown() {
    }

    @org.junit.jupiter.api.Test
    public void translateJsTestEmpty() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];return actions;\"}";
        setUpStart();       
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestNewProcedure() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('New procedure: Test');return actions;\"}";
        setUpSingleMedicalAction("newProcedure");
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestOrderLabs() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('Order labs: Test');return actions;\"}";
        setUpSingleMedicalAction("orderLabs");
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestNewPrescription() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('New prescription: Test');return actions;\"}";
        setUpSingleMedicalAction("newPrescription");
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestAddDiagnosis() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('Add diagnosis: Test');return actions;\"}";
        setUpSingleMedicalAction("addDiagnosis");
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestNewVaccination() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('New vaccination: Test');return actions;\"}";
        setUpSingleMedicalAction("newVaccination");
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestAddNotes() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('Add notes: Test');return actions;\"}";
        setUpSingleMedicalAction("addNotes");
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestVariableSingle() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [\"test1\"], \"code\": \"let actions = [];return actions;\"}";
        setUpVariableSingle();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestSingleIfElse() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [\"test1\"], \"code\": \"let actions = [];if (test1 >=10) {actions.push('Add diagnosis: testD1');} else {actions.push('Add notes: cElse');}return actions;\"}";
        setUpSingleIfElse();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    /* loop tests are disabled because Jetty marks an URI with spaces as invalid whereas the app can handle it. 
        code remains in case I have time/motivation to rewrite the code for adding chartitems to use JSON body and rewrite the tests */
//    @org.junit.jupiter.api.Test
//    public void translateJsTestSingleLoopFixedAction() throws IOException, Exception {
//        String expectedResponse = "chartJS(testPlural1) {let actions = [];testPlural1.forEach(element => {actions.push({type: addNotes, msg: test});});return actions;}";
//        setUpSingleLoopFixedAction();
//        String result = callTranslate();
//        
//        assertEquals(expectedResponse, result);
//    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestThreeActions() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [], \"code\": \"let actions = [];actions.push('Add notes: notes');actions.push('New vaccination: flu');actions.push('Add diagnosis: test');return actions;\"}";
        setUpThreeActions();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestConditionalInIf() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [\"test1\", \"test2\"], \"code\": \"let actions = [];if (test1 >=10) {if (test2 >=20) {actions.push('Add notes: notes');} else {actions.push('Add diagnosis: flu');}} else {actions.push('New vaccination: flu');}return actions;\"}";
        setUpConditionalInIf();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestConditionalInElse() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [\"test1\", \"test2\"], \"code\": \"let actions = [];if (test1 >=10) {actions.push('New vaccination: flu');} else {if (test2 >=20) {actions.push('Add notes: notes');} else {actions.push('Add diagnosis: flu');}}return actions;\"}";
        setUpConditionalInElse();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsTestConditionalsBothBranches() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [\"test1\", \"test2\", \"test3\"], \"code\": \"let actions = [];if (test1 >=10) {if (test2 >=20) {actions.push('Add notes: notes');} else {actions.push('Add diagnosis: flu');}} else {if (test3 >=30) {actions.push('New vaccination: flu');} else {actions.push('New vaccination: covid');}}return actions;\"}";
        setUpConditionalsBothBranches();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsCRPANA() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [\"CRP\", \"ANA\"], \"code\": \"let actions = [];if (CRP >=500) {if (ANA ===1) {actions.push('Add notes: ANAPos');} else {}} else {actions.push('Add notes: CRPnormal');}return actions;\"}";
        setUpCRPANATest();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsHypertensionScenario() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [\"Onderdruk\", \"Bovendruk\", \"Risico_HVZ\", \"Gebruikt_diuretica\"], \"code\": \""
                + "let actions = [];let hypertensie;if (Onderdruk > 90) {if (Bovendruk > 140) {hypertensie = true;} else {}} else {}if (hypertensie ===true) {if (Risico_HVZ >20) {if (Gebruikt_diuretica ===true) "
                + "{actions.push('New prescription: ACE-remmers');} else {actions.push('New prescription: Diuretica');}} else {}}return actions;\"}";
        setUpHypertensionScenario();
        String result = callTranslate();
        
        assertEquals(expectedResponse, result);
    }
    
    @org.junit.jupiter.api.Test
    public void translateJsHypertensionScenarioFixedState() throws IOException, Exception {
        String expectedResponse = "{\"parameters\": [\"Onderdruk\", \"Bovendruk\", \"Risico_HVZ\", \"Gebruikt_diuretica\"], \"code\": \""
            + "let actions = [];let hypertensie;if (Onderdruk > 90) {if (Bovendruk > 140) {hypertensie = true;} else {}} else {}if (hypertensie ===true) {if (Risico_HVZ >20) {if (Gebruikt_diuretica ===true) "
            + "{actions.push('New prescription: ACE-remmers');} else {actions.push('New prescription: Diuretica');}} else {}}return actions;\"}";
        String stateString = "[{\"id\":\"a3966\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":\"null\"},{\"id\":\"a9070\",\"type\":\"subroutine\",\"prevItemId\":\"a3966\",\"caption\":\"hypertensie\",\"condition\":\"null\"},{\"id\":\"a571\",\"type\":\"conditional\",\"prevItemId\":\"a9070\",\"caption\":\"hypertensie\",\"condition\":null},{\"id\":\"a1790\",\"type\":\"retrievedata\",\"prevItemId\":\"a571\",\"caption\":\"Risico_HVZ\",\"condition\":\"===true\"},{\"id\":\"a8919\",\"type\":\"conditional\",\"prevItemId\":\"a1790\",\"caption\":\"Risico_HVZ\",\"condition\":\"null\"},{\"id\":\"a4323\",\"type\":\"retrievedata\",\"prevItemId\":\"a8919\",\"caption\":\"Gebruikt_diuretica\",\"condition\":\">20\"},{\"id\":\"a6180\",\"type\":\"conditional\",\"prevItemId\":\"a4323\",\"caption\":\"Gebruikt_diuretica\",\"condition\":\"null\"},{\"id\":\"a2701\",\"type\":\"newPrescription\",\"prevItemId\":\"a6180\",\"caption\":\"ACE-remmers\",\"condition\":\"===true\"},{\"id\":\"a7276\",\"type\":\"newPrescription\",\"prevItemId\":\"a6180\",\"caption\":\"Diuretica\",\"condition\":\"null\"},{\"id\":\"a3064\",\"type\":\"end\",\"prevItemId\":\"a571\",\"caption\":\"Stop\",\"condition\":null}]";
        String endlines = "[\"a8919\", \"a7276\", \"a2701\"]";
        Pair<LinkedList<ChartItem>, ArrayList<String>> state = new Pair<>(JSONDecoder.decodeChart(stateString), new ArrayList<>(Arrays.asList(endlines.replace("[", "").replace("]", "").split(", "))));
        String result = ChartTranslator.translateToJS(state, "usability");
        
        assertEquals(expectedResponse, result);
    }
    
    // SetUp functions
    
    private void setUpStart() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem stop = new ChartItem("a2", "end", "a1", "End", null);
        
        updateState(start);
        updateState(stop);
    }  
    
    private void setUpSingleMedicalAction(String medicalAction) throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem medicalActionItem = new ChartItem("a2", medicalAction, "a1", "Test", null);
        ChartItem stop = new ChartItem("a3", "end", "a2", "End", null);
        
        updateState(start);
        updateState(medicalActionItem);
        updateState(stop);
    }
    
    private void setUpVariableSingle() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem variable = new ChartItem("a2", "retrievedata", "a1", "test1", null);
        ChartItem stop = new ChartItem("a3", "end", "a2", "End", null);
        
        updateState(start);
        updateState(variable);
        updateState(stop);
    }
    
    private void setUpSingleIfElse() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem variable = new ChartItem("a2", "retrievedata", "a1", "test1", null);
        ChartItem conditional = new ChartItem("a3", "conditional", "a2", "test1", null);
        ChartItem conditionalIf = new ChartItem("a4", "addDiagnosis", "a3", "testD1", ">=10");
        ChartItem conditionalElse = new ChartItem("a5", "addNotes", "a3", "cElse", null);
        ChartItem stop = new ChartItem("a6", "end", "a4", "End", null);
        
        updateState(start);
        updateState(variable);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalIf) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalElse) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        updateState(stop);
    }
    
//    private void setUpSingleLoopFixedAction() throws IOException, Exception {
//        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
//        ChartItem retrieve = new ChartItem("a2", "retrievedata", "a1", "testPlural1", null);
//        ChartItem loop = new ChartItem("a3", "loop", "a2", "testPlural1", null);
//        ChartItem loopAction = new ChartItem("a4", "addNotes", "a3", "test", null);
//        ChartItem loopEnd = new ChartItem("a5", "loop", "a4", "End for testPlural1", null);
//        ChartItem stop = new ChartItem("a6", "end", "a5", "End", null);
//        
//        updateState(start);
//        updateState(retrieve);
//        updateState(loop);
//        updateState(loopAction);
//        updateState(loopEnd);
//        updateState(stop);
//    }
    
    private void setUpThreeActions() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem action1 = new ChartItem("a2", "addNotes", "a1", "notes", null);
        ChartItem action2 = new ChartItem("a3", "newVaccination", "a2", "flu", null);
        ChartItem action3 = new ChartItem("a4", "addDiagnosis", "a3", "test", null);
        ChartItem stop = new ChartItem("a5", "end", "a4", "End", null);
        
        updateState(start);
        updateState(action1);
        updateState(action2);
        updateState(action3);
        updateState(stop);
    }
    
    private void setUpConditionalInIf() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem retrievedata = new ChartItem("a2", "retrievedata", "a1", "test1", null);
        ChartItem retrievedata2 = new ChartItem("a3", "retrievedata", "a2", "test2", null);
        ChartItem conditional = new ChartItem("a4", "conditional", "a3", "test1", null);
        ChartItem conditional2 = new ChartItem("a5", "conditional", "a4", "test2", ">=10");
        ChartItem c2action1 = new ChartItem("a6", "addNotes", "a5", "notes", ">=20");
        ChartItem c2action2 = new ChartItem("a7", "addDiagnosis", "a5", "flu", null);
        ChartItem c1action2 = new ChartItem("a8", "newVaccination", "a4", "flu", null);
        ChartItem stop = new ChartItem("a9", "end", "a8", "End", null);
        
        updateState(start);
        updateState(retrievedata);
        updateState(retrievedata2);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional2) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(c1action2) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional2) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(c2action1) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(c2action2) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        updateState(stop);
    }
    
    private void setUpConditionalInElse() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem retrievedata = new ChartItem("a2", "retrievedata", "a1", "test1", null);
        ChartItem retrievedata2 = new ChartItem("a3", "retrievedata", "a2", "test2", null);
        ChartItem conditional = new ChartItem("a4", "conditional", "a3", "test1", null);
        ChartItem c1action2 = new ChartItem("a5", "newVaccination", "a4", "flu", ">=10");
        ChartItem conditional2 = new ChartItem("a6", "conditional", "a4", "test2", null);
        ChartItem c2action1 = new ChartItem("a7", "addNotes", "a6", "notes", ">=20");
        ChartItem c2action2 = new ChartItem("a8", "addDiagnosis", "a6", "flu", null);
        ChartItem stop = new ChartItem("a9", "end", "a8", "End", null);
        
        updateState(start);
        updateState(retrievedata);
        updateState(retrievedata2);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(c1action2) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional2) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional2) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(c2action1) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(c2action2) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        updateState(stop);
    }
    
    private void setUpConditionalsBothBranches() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem retrievedata = new ChartItem("a2", "retrievedata", "a1", "test1", null);
        ChartItem retrievedata2 = new ChartItem("a3", "retrievedata", "a2", "test2", null);
        ChartItem retrievedata3 = new ChartItem("a4", "retrievedata", "a3", "test3", null);
        ChartItem conditional1 = new ChartItem("a5", "conditional", "a4", "test1", null);
        ChartItem conditional2 = new ChartItem("a6", "conditional", "a5", "test2", ">=10");
        ChartItem c2a1 = new ChartItem("a7", "addNotes", "a6", "notes", ">=20");
        ChartItem c2a2 = new ChartItem("a8", "addDiagnosis", "a6", "flu", null);
        ChartItem conditional3 = new ChartItem("a9", "conditional", "a5", "test3", null);
        ChartItem c3a1 = new ChartItem("a10", "newVaccination", "a9", "flu", ">=30");
        ChartItem c3a2 = new ChartItem("a11", "newVaccination", "a9", "covid", null);
        ChartItem stop = new ChartItem("a12", "end", "a11", "End", null);
        
        updateState(start);
        updateState(retrievedata);
        updateState(retrievedata2);
        updateState(retrievedata3);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional1) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional2) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional3) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional2) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(c2a1) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(c2a2) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditional3) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(c3a1) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(c3a2) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        updateState(stop);
    }
    
    public void setUpCRPANATest() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem retrieveCRP = new ChartItem("a2", "retrievedata", "a1", "CRP", null);
        ChartItem retrieveANA = new ChartItem("a3", "retrievedata", "a2", "ANA", null);
        ChartItem conditionalCRP = new ChartItem("a4", "conditional", "a3", "CRP", null);
        ChartItem conditionalANA = new ChartItem("a5", "conditional", "a4", "ANA", ">=500");
        ChartItem logPos = new ChartItem("a6", "addNotes", "a5", "ANAPos", "===1");
        ChartItem end = new ChartItem("a7", "end", "a5", "End", null);
        ChartItem logCRP = new ChartItem("a8", "addNotes", "a4", "CRPnormal", null);
        
        updateState(start);
        updateState(retrieveCRP);
        updateState(retrieveANA);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalCRP) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalANA) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(logCRP) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalANA) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(logPos) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(end) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
    }
    
    public void setUpHypertensionScenario() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem hypertensieSub = new ChartItem("a2", "subroutine", "a1", "hypertensie", null);
        ChartItem conditionalH = new ChartItem("a3", "conditional", "a2", "hypertensie", null);
        ChartItem retrieveRisico = new ChartItem("a4", "retrievedata", "a3", "Risico_HVZ", "===true");
        ChartItem end = new ChartItem("a99", "end", "a3", "Stop", null);
        ChartItem conditionalR = new ChartItem("a5", "conditional", "a4", "Risico_HVZ", null);
        ChartItem retrieveDiuretica = new ChartItem("a6", "retrievedata", "a5", "Gebruikt_diuretica", ">20");
        ChartItem conditionalD = new ChartItem("a7", "conditional", "a6", "Gebruikt_diuretica", null);
        ChartItem prescribeACE = new ChartItem("a8", "newPrescription", "a7", "ACE-remmers", "===true");
        ChartItem prescribeD = new ChartItem("a9", "newPrescription", "a7", "Diuretica", null);
        
        updateState(start);
        updateState(hypertensieSub);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalH) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(retrieveRisico) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(end) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalR) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(retrieveDiuretica) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalD) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(prescribeACE) + "&isMultipart=true");
        response.parse(tester.getResponses(request.generate()));
        request.setURI("/katool?function=update&" + chartItemToURLString(prescribeD) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
    }
    
    // Helper functions
    
    private String chartItemToURLString(ChartItem item) {
        return "id=" + item.getId() + 
                "&type=" + item.getType() + 
                "&prevItemId=" + item.getPrevItemId() + 
                "&caption="  + item.getCaption() + 
                "&condition=" + item.getCondition(); 
    }
        
    private void updateState(ChartItem item) throws IOException, Exception{
        request.setURI("/katool?function=update&" + chartItemToURLString(item));
        tester.getResponses(request.generate());
    }
    
    private String callTranslate() throws IOException, Exception{
        request.setURI("/katool?function=translateJS");
        response.parse(tester.getResponses(request.generate()));
        return response.getContent();
    }
    
    private String getTranslation() throws IOException {
        String pathToFile = null;
        if (Utils.workingDir != null) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(new File(Utils.workingDir.toString()), extensions, true);
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
        return new String(Files.readAllBytes(Paths.get(pathToFile)));
    }
}
