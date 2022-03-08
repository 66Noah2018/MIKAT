/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.testing.HttpTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
    
    public chartservletTest() {
    }

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
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() throws Exception {
    }
    
    @AfterEach
    public void tearDown() {
    }

    // Test for adding Start
    @org.junit.jupiter.api.Test
    public void testAddStart() throws IOException, Exception{
        String expectedResponse = "{\"state\":[{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null}], \"endLines\":[]}";
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        request.setVersion("HTTP/1.0");
        HttpTester response = new HttpTester();
        response.parse(tester.getResponses(request.generate()));
        
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for update (add multiple, insert)
    
    @org.junit.jupiter.api.Test
    public void testAddFourElements() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        addFourElements(request);
        HttpTester response = new HttpTester();
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
        
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        addFourElements(request);
        HttpTester response = new HttpTester();
        request.setURI("/katool?function=update&" + chartItemToURLString(mockInsertElement));
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
        
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        addFourElements(request);
        request.setURI("/katool?function=undo");
        
        HttpTester response = new HttpTester();
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Test for redo
    //TODO: why does this throw NoSuchElementException at chartservlet:511 removeFirst()?
    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.Disabled
    public void testRedo() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[], \"size\":0}";
        
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        addFourElements(request);
        request.setURI("/katool?function=undo");
        request.setURI("/katool?function=redo");
        
        HttpTester response = new HttpTester();
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for undoSize
    //TODO: write tests
    
    // Tests for redoSize
    //TODO: write tests
    
    //Tests for localmap and standardmap?
    
    // Tests for delete
    @org.junit.jupiter.api.Test
    public void testRemoveSecondOfFourElements() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a111\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        addFourElements(request);
        request.setURI("/katool?function=delete&id=a222");
        HttpTester response = new HttpTester();
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    //TODO: add tests for removing end when one element to end and when multiple lines to end
    
    // Tests for file?
    
    // Tests for open?
    
    // Tests for save?
    
    // Test for state
    //TODO: write test
    
    // Tests for getElement
    //TODO: write tests
    
    // Tests for endline
    //TODO: write tests
    
    // Tests for hasNext
    //TODO: write tests
    
    // Tests for getConditionalActions
    //TODO: write tests
    
    // helper functions
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
}
