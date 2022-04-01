/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import junit.framework.TestCase;
import static katool.Utils.determineOS;
import static katool.Utils.rootPath;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
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
    @org.junit.jupiter.api.Disabled //disabled because running these tests takes at least 20secs per tests
    public void translateJsTestEmpty() throws IOException, Exception {
        String expectedResponse = "function chartJS() {\rlet actions = [];\rreturn actions;\r}";
        
        setUpStart();       
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestNewProcedure() throws IOException, Exception{
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: newProcedure, msg: Test});\rreturn actions;\r}";
        
        setUpSingleMedicalAction("newProcedure");
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestOrderLabs() throws IOException, Exception{
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: orderLabs, msg: Test});\rreturn actions;\r}";
        
        setUpSingleMedicalAction("orderLabs");
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestNewPrescription() throws IOException, Exception{
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: newPrescription, msg: Test});\rreturn actions;\r}";
        
        setUpSingleMedicalAction("newPrescription");
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestAddDiagnosis() throws IOException, Exception{
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: addDiagnosis, msg: Test});\rreturn actions;\r}";
        
        setUpSingleMedicalAction("addDiagnosis");
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestNewVaccination() throws IOException, Exception{
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: newVaccination, msg: Test});\rreturn actions;\r}";
        
        setUpSingleMedicalAction("newVaccination");
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestAddNotes() throws IOException, Exception{
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: addNotes, msg: Test});\rreturn actions;\r}";
        
        setUpSingleMedicalAction("addNotes");
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestVariableSingle() throws IOException, Exception{
        String expectedResponse = "function chartJS(test1) {\rlet actions = [];\rreturn actions;\r}";
        
        setUpVariableSingle();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestSubroutine() throws IOException, Exception {
        String expectedResponse = "function chartJS() {\rlet actions = [];\r\rreturn actions;\r}";
        
        setUpSubroutine();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestSingleIfElse() throws IOException, Exception {
        String expectedResponse = "function chartJS(test1) {\rlet actions = [];\rif (test1 >=10) {\ractions.push({type: addDiagnosis, msg: testD1});\r} else {\ractions.push({type: addNotes, msg: cElse});\r}\rreturn actions;\r}";
        setUpSingleIfElse();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    /* loop tests are disabled because Jetty marks an URI with spaces as invalid whereas the app can handle it. 
        code remains in case I have time/motivation to rewrite the code for adding chartitems to use JSON body and rewrite the tests */
//    @org.junit.jupiter.api.Test
//    public void translateJsTestSingleLoopFixedAction() throws IOException, Exception {
//        String expectedResponse = "function chartJS(testPlural1) {\rlet actions = [];\rtestPlural1.forEach(element => {\ractions.push({type: addNotes, msg: test});\r});\rreturn actions;\r}";
//        setUpSingleLoopFixedAction();
//        callTranslate();
//        
//        assertEquals(expectedResponse, getTranslation());
//    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestThreeActions() throws IOException, Exception {
        String expectedResponse = "function chartJS() {\rlet actions = [];\ractions.push({type: addNotes, msg: notes});\ractions.push({type: newVaccination, msg: flu});\ractions.push({type: addDiagnosis, msg: test});\rreturn actions;\r}";
        setUpThreeActions();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestConditionalInIf() throws IOException, Exception {
        String expectedResponse = "function chartJS(test1, test2) {\rlet actions = [];\rif (test1 >=10) {\rif (test2 >=20) {\ractions.push({type: addNotes, msg: notes});\r} else {\ractions.push({type: addDiagnosis, msg: flu});\r}\r} else {\ractions.push({type: newVaccination, msg: flu});\r}\rreturn actions;\r}";
        setUpConditionalInIf();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestConditionalInElse() throws IOException, Exception {
        String expectedResponse = "function chartJS(test1, test2) {\rlet actions = [];\rif (test1 >=10) {\ractions.push({type: newVaccination, msg: flu});\r} else {\rif (test2 >=20) {\ractions.push({type: addNotes, msg: notes});\r} else {\ractions.push({type: addDiagnosis, msg: flu});\r}\r}\rreturn actions;\r}";
        setUpConditionalInElse();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsTestConditionalsBothBranches() throws IOException, Exception {
        String expectedResponse = "function chartJS(test1, test2, test3) {\rlet actions = [];\rif (test1 >=10) {\rif (test2 >=20) {\ractions.push({type: addNotes, msg: notes});\r} else {\ractions.push({type: addDiagnosis, msg: flu});\r}\r} else {\rif (test3 >=30) {\ractions.push({type: newVaccination, msg: flu});\r} else {\ractions.push({type: newVaccination, msg: covid});\r}\r}\rreturn actions;\r}";
        setUpConditionalsBothBranches();
        callTranslate();
        
        assertEquals(expectedResponse, getTranslation());
    }
    
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void translateJsCRPANA() throws IOException, Exception{
        String expectedResponse = "{\"parameters\": [\"CRP\", \"ANA\"], \"code\": \"let actions = [];if (CRP >=500) {if (ANA ===1) {actions.push({type: addNotes, msg: ANAPos});} else {}} else {actions.push({type: addNotes, msg: CRPnormal});}return actions;\"}";
        setUpCRPANATest();
        String result = callTranslate();
        
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
    
    private void setUpSubroutine() throws IOException, Exception {
        ChartItem start = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem subroutine = new ChartItem("a2", "subroutine", "a1", "Test1.json", null);
        ChartItem stop = new ChartItem("a3", "end", "a2", "End", null);
        
        updateState(start);
        updateState(subroutine);
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
//        System.out.println(new String(Files.readAllBytes(Paths.get(pathToFile))));
        return new String(Files.readAllBytes(Paths.get(pathToFile)));
    }
}
