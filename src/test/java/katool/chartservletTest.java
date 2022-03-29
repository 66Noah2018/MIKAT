/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import java.io.IOException;
import java.util.LinkedList;
import org.eclipse.jetty.testing.HttpTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.jetty.testing.ServletTester;

/**
 *
 * @author rlvanbrummelen
 */
public class chartservletTest {
    
    private static ServletTester tester;
    private static ChartItem mockStart;
    private static ChartItem mockElement1;
    private static ChartItem mockElement2;
    private static ChartItem mockEnd;
    private static ChartItem mockInsertElement;
    private static LinkedList<ChartItem> mockConditional;
    private static LinkedList<ChartItem> mockConditional2;
    private static ChartItem mockRetrieveData;
    private static ChartItem mockRetrieveData2;
    private static ChartItem mockRetrieveDataPlural;
    private static ChartItem mockRetrieveDataPlural2;
    private static ChartItem mockLoop;
    private static ChartItem mockLoopFirstAction;
    private static ChartItem mockLoop2;
    private static ChartItem mockLoop2FirstAction;
    private static HttpTester request;
    private static HttpTester response;

    @BeforeAll
    public static void setUpClass() throws Exception {
        tester = new ServletTester();
        tester.addServlet(katool.chartservlet.class, "/katool");
        tester.start();
        mockStart = new ChartItem("a111", "start", "-1", "Start", null);
        mockElement1 = new ChartItem("a222", "newProcedure", "a111", "Procedure", null);
        mockElement2 = new ChartItem("a333", "orderLabs", "a222", "Labs", null);
        mockEnd = new ChartItem("a0", "end", "a333", "End", null);
        mockInsertElement = new ChartItem("a444", "addNotes", "a222", "Notes", null);
        mockRetrieveData = new ChartItem("a2", "retrievedata", "a111", "Test1", null);
        mockRetrieveData2 = new ChartItem("a3", "retrievedata", "a1", "Test2", null);
        mockRetrieveDataPlural = new ChartItem("a4", "retrievedata", "a111", "Plural1", null);
        mockLoop = new ChartItem("a5", "loop", "a4", "Plural1", null);
        mockLoopFirstAction = new ChartItem("a6", "orderLabs", "a5", "Labs", null);
        mockRetrieveDataPlural2 = new ChartItem("a8", "retrievedata", "a6", "Plural2", null);
        mockLoop2 = new ChartItem("a9", "loop", "a8", "Plural2", null);
        mockLoop2FirstAction = new ChartItem("a30", "addNotes", "a9", "Notes", null);
        
        mockConditional = new LinkedList<>();
        mockConditional.add(new ChartItem("a1", "conditional", "a2", "Test1", null));
        mockConditional.add(new ChartItem("a10", "addNotes", "a1", "Notes", "<10"));
        mockConditional.add(mockRetrieveData2);
        
        mockConditional2 = new LinkedList<>();
        mockConditional2.add(new ChartItem("a11", "conditional", "a3", "Test2", null));
        mockConditional2.add(new ChartItem("a12", "newProcedure", "a11", "Procedure", ">=11"));
        mockConditional2.add(new ChartItem("a13", "orderLabs", "a11", "Labs", null));
        
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

    // Test for adding Start
    @org.junit.jupiter.api.Test
    public void testAddStart() throws IOException, Exception{
        String expectedResponse = "{\"state\":[{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null}], \"endLines\":[]}";
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        response.parse(tester.getResponses(request.generate()));
        
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for update (add multiple, insert, includes endline tests)
    
    @org.junit.jupiter.api.Test
    public void testAddFourElements() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        addFourElements(request);
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }

    @org.junit.jupiter.api.Test
    public void testInsertAfterSecondElement() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a444\",\"type\":\"addNotes\",\"prevItemId\":\"a222\",\"caption\":\"Notes\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a444\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
                
        addFourElements(request);
        request.setURI("/katool?function=update&" + chartItemToURLString(mockInsertElement));
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testAddSingleConditionalSingleEnd() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";

        addSingleConditionalSingleEnd(request);

        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testAddSingleConditionalMultipleEnd() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null},"
                + "{\"id\":\"a21\",\"type\":\"addDiagnosis\",\"prevItemId\":\"a3\",\"caption\":\"Diagnosis\",\"condition\":null}"
                + "], \"endLines\":[\"a21\"]}";

        addSingleConditionalMultipleEnd(request);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testAddDoubleConditionalSingleEnd() throws IOException, Exception {
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        addDoubleConditionalSingleEnd(request);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }  
    
    @org.junit.jupiter.api.Test
    public void testAddDoubleConditionalMultipleEnd() throws IOException, Exception {
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[\"a13\"]}";
        
        addDoubleConditionalMultipleEnd(request);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    } 
    
    // Test for undo
    @org.junit.jupiter.api.Test
    public void testUndo() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null}"
                + "], \"endLines\":[], \"size\":2}";
        
        addFourElements(request);
        request.setURI("/katool?function=undo");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Test for redo
    @org.junit.jupiter.api.Test
    public void testRedo() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[], \"size\":0}";
        
        addFourElements(request);
        request.setURI("/katool?function=undo");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=redo");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Test for undoSize
    @org.junit.jupiter.api.Test
    public void testUndoSize() throws IOException, Exception{
        String expectedResponse = "{\"size\":3}";
        
        addFourElements(request);
        request.setURI("/katool?function=undoSize");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Test for redoSize
    @org.junit.jupiter.api.Test
    public void testRedoSize() throws IOException, Exception{
        String expectedResponse = "{\"size\":1}";
        
        addFourElements(request);
        request.setURI("/katool?function=undo");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=redoSize");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    //Tests for localmap and standardmap?
    
    // Tests for delete
    
    @org.junit.jupiter.api.Test
    public void testRemoveSecondOfFourElements() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a111\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
                
        addFourElements(request);
        request.setURI("/katool?function=delete&id=a222");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testRemoveEnd() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        addFourElements(request);
        request.setURI("/katool?function=delete&id=a0");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testRemoveEndMultipleLines() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a21\",\"type\":\"addDiagnosis\",\"prevItemId\":\"a3\",\"caption\":\"Diagnosis\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        addSingleConditionalMultipleEnd(request);
        request.setURI("/katool?function=delete&id=a20");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
        
    }
    
    @org.junit.jupiter.api.Test
    public void testRemoveEndMultipleLinesDoubleConditional() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        addDoubleConditionalMultipleEnd(request);
        request.setURI("/katool?function=delete&id=a20");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for file?
    
    // Tests for open?
    
    // Tests for save?
    
    // Tests for state
    
    @org.junit.jupiter.api.Test
    public void testGetState() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        addFourElements(request);
        request.setURI("/katool?function=state");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetEmptyState() throws IOException, Exception{
        String expectedResponse = "{\"state\":[], \"endLines\":[]}";
        
        tester = new ServletTester(); //somehow the memory works now, so have to restart the servlet
        tester.addServlet(katool.chartservlet.class, "/katool");
        tester.start();
        
        request.setURI("/katool?function=state");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for getElement
    
    @org.junit.jupiter.api.Test
    public void testGetElementSimple() throws IOException, Exception{
        String expectedResponse = "{\"chartItem\":{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null}}";
        
        addFourElements(request);
        request.setURI("/katool?function=getElement&id=a333");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetElementComplex() throws IOException, Exception{
        String expectedResponse = "{\"chartItem\":{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null}}";
        
        addDoubleConditionalMultipleEnd(request);
        request.setURI("/katool?function=getElement&id=a3");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNonexistentElement() throws IOException, Exception{
        String expectedResponse = "{\"chartItem\":null}";
        
        addFourElements(request);
        request.setURI("/katool?function=getElement&id=a334");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for hasNext
    
    @org.junit.jupiter.api.Test
    public void testHasNextSimple() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":true}";
        
        addFourElements(request);
        request.setURI("/katool?function=hasNext&id=a333");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHasNextComplex() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":true}";
        
        addDoubleConditionalMultipleEnd(request);
        request.setURI("/katool?function=hasNext&id=a11");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHasNextFalse() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":false}";
        
        addFourElements(request);
        request.setURI("/katool?function=hasNext&id=a0");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHasNextNonexistentId() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":false}";
        
        addFourElements(request);
        request.setURI("/katool?function=hasNext&id=a01010101");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for getConditionalActions
    
    @org.junit.jupiter.api.Test
    public void testGetConditionalActionsSimple() throws IOException, Exception{
        String expectedResponse = "{\"items\":["
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null}"
                + "]}";
        
        addSingleConditionalSingleEnd(request);
        request.setURI("/katool?function=getConditionalActions&id=a1");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetConditionalActionsComplex() throws IOException, Exception{
        String expectedResponse = "{\"items\":["
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null}"
                + "]}";
        
        addDoubleConditionalSingleEnd(request);
        request.setURI("/katool?function=getConditionalActions&id=a1");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetConditionalActionsComplex2() throws IOException, Exception{
        String expectedResponse = "{\"items\":["
                + "{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null}"
                + "]}";
        
        addDoubleConditionalSingleEnd(request);
        request.setURI("/katool?function=getConditionalActions&id=a11");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for getNext
    
    @org.junit.jupiter.api.Test
    public void testGetNext() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null}}";
        
        addFourElements(request);
        request.setURI("/katool?function=getNext&id=a111");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNextComplex() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"}}";
        
        addDoubleConditionalSingleEnd(request);
        request.setURI("/katool?function=getNext&id=a11");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNextNoNext() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":null}";
        
        addDoubleConditionalSingleEnd(request);
        request.setURI("/katool?function=getNext&id=a20");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNextNonexistentId() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":null}";
        
        addDoubleConditionalSingleEnd(request);
        request.setURI("/katool?function=getNext&id=a2022");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for getClosestLoopStart
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartSingleLoopNoEnd() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"Plural1\"}";
        
        addSingleLoopNoEnd(request);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a6");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartDoubleLoopNoEndOuterLoop() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"Plural1\"}";
        
        addDoubleLoopNoEnd(request);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a6");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartDoubleLoopNoEndInnerLoop() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"Plural2\"}";
        
        addDoubleLoopNoEnd(request);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a30");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartSingleLoopExpectNull() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"null\"}";
        
        addSingleLoopNoEnd(request);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a111");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for loopHasEnd. Cannot test for hasEnd due to bad requests (End for ... causes 400 in Jetty, but works in UI)
    
    @org.junit.jupiter.api.Test
    public void testLoopHasEndSingleLoopNoEnd() throws IOException, Exception{
        String expectedResponse = "{\"hasEnd\":false}";
        
        addSingleLoopNoEnd(request);
        request.setURI("/katool?function=loopHasEnd&caption=Plural1");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testLoopHasEndDoubleLoopNoEndInner() throws IOException, Exception{
        String expectedResponse = "{\"hasEnd\":false}";
        
        addDoubleLoopNoEnd(request);
        request.setURI("/katool?function=loopHasEnd&caption=Plural2");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testLoopHasEndDoubleLoopNoEndOuter() throws IOException, Exception{
        String expectedResponse = "{\"hasEnd\":false}";
        
        addDoubleLoopNoEnd(request);
        request.setURI("/katool?function=loopHasEnd&caption=Plural1");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for directoryExists
    
    @org.junit.jupiter.api.Test
    public void testDirectoryExistsTrue() throws IOException, Exception{
        String expectedResponse = "{\"directoryExists\":true}";
        
        request.setURI("/katool?function=directoryExists");
        request.setContent("C:\\Users\\RLvan\\OneDrive\\Documenten\\MI\\SRP\\Test files");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }

    @org.junit.jupiter.api.Test
    public void testDirectoryExistsFalse() throws IOException, Exception{
        String expectedResponse = "{\"directoryExists\":false}";
        
        request.setURI("/katool?function=directoryExists");
        request.setContent("a\\bullshit\\directory");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Helper functions
    
    private String chartItemToURLString(ChartItem item) {
        return "id=" + item.getId() + 
                "&type=" + item.getType() + 
                "&prevItemId=" + item.getPrevItemId() + 
                "&caption="  + item.getCaption() + 
                "&condition=" + item.getCondition(); 
    }
    
    private void addFourElements(HttpTester request) throws IOException, Exception{
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockElement1));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockElement2));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockEnd));
        tester.getResponses(request.generate());
    }
    
    private void addSingleConditionalSingleEnd(HttpTester request) throws IOException, Exception{
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockRetrieveData));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional.get(1)) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(new ChartItem("a20", "end", "a10", "End", null)));
        tester.getResponses(request.generate());
    }
    
    private void addSingleConditionalMultipleEnd(HttpTester request) throws IOException, Exception{
        addSingleConditionalSingleEnd(request);
        
        request.setURI("/katool?function=update&" + chartItemToURLString(new ChartItem("a21", "addDiagnosis", "a3", "Diagnosis", null)));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=endline&id=a21");
        tester.getResponses(request.generate());
    }
    
    private void addDoubleConditionalSingleEnd(HttpTester request) throws IOException, Exception{
        addSingleConditionalSingleEnd(request);
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional2.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional2.get(1)) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional2.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
    }
    
    private void addDoubleConditionalMultipleEnd(HttpTester request) throws IOException, Exception{
        addDoubleConditionalSingleEnd(request);
        
        request.setURI("/katool?function=endline&id=a13");
        tester.getResponses(request.generate());
        
    }
    
    private void addSingleLoopNoEnd(HttpTester request) throws IOException, Exception {
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockRetrieveDataPlural));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoop));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoopFirstAction));
        tester.getResponses(request.generate());
    }
    
    private void addDoubleLoopNoEnd(HttpTester request) throws IOException, Exception{
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockRetrieveDataPlural));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoop));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoopFirstAction));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockRetrieveDataPlural2));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoop2));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoop2FirstAction));
        tester.getResponses(request.generate());
    }
}