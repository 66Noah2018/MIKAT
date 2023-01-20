/*
 * Copyright (C) 2023 Amsterdam Universitair Medische Centra
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
import org.eclipse.jetty.testing.HttpTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.jetty.testing.ServletTester;

/**
 *
 * @author RLvan
 */
public class chartservletUpdateTest {
    private static ServletTester tester;
    private static ChartItem mockStart;
    private static ChartItem mockInsertElement;
    private static LinkedList<ChartItem> mockConditional;
    private static LinkedList<ChartItem> mockConditional2;
    private static ChartItem mockRetrieveData2;
    private static HttpTester request;
    private static HttpTester response;

    @BeforeAll
    public static void setUpClass() throws Exception {
        mockStart = new ChartItem("a111", "start", "-1", "Start", null);
        mockInsertElement = new ChartItem("a444", "addNotes", "a222", "Notes", null);
        mockRetrieveData2 = new ChartItem("a3", "retrievedata", "a1", "Test2", null);
        
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
        tester = new ServletTester();
        tester.addServlet(katool.chartservlet.class, "/katool");
        tester.start();
        request = new HttpTester();
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        
        response = new HttpTester();
    }
    
    @AfterEach
    public void tearDown() {
    }
    
    // Tests for Start
    @org.junit.jupiter.api.Test
    public void testAddStart() throws IOException, Exception{
        String expectedResponse = "{\"state\":[{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null}], \"endLines\":[]}";
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockStart));
        response.parse(tester.getResponses(request.generate()));
        
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testTryToRemoveStart() throws IOException, Exception {
        String expectedResponse = "{\"state\":[{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null}], \"endLines\":[]}";
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        request.setURI("/katool?function=delete&id=" + mockStart.getId());
        response.parse(tester.getResponses(request.generate()));
        
        assertEquals(expectedResponse, response.getContent());
    }

    // Tests for update
    
    @org.junit.jupiter.api.Test
    public void testAddFourElements() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        TestDataFactory.addFourElements(request, tester);
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
                
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockInsertElement));
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

        TestDataFactory.addSingleConditionalSingleEnd(request, tester);

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

        TestDataFactory.addSingleConditionalMultipleEnd(request, tester);
        
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
        
        TestDataFactory.addDoubleConditionalSingleEnd(request, tester);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }  
    
    @org.junit.jupiter.api.Test
    public void testAddDoubleConditionalSingleEndAlterFirstConditional() throws IOException, Exception {
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"returnValue\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"===10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        TestDataFactory.addDoubleConditionalSingleEnd(request, tester);
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(new ChartItem("a10", "returnValue", "a1", "Notes", "===10")) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testAddDoubleConditionalSingleEndAlterSecondConditional() throws IOException, Exception {
       String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"returnValue\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\"===11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        TestDataFactory.addDoubleConditionalSingleEnd(request, tester);
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(new ChartItem("a12", "returnValue", "a11", "Procedure", "===11")) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        
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
        
        TestDataFactory.addDoubleConditionalMultipleEnd(request, tester);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    } 
    
    @org.junit.jupiter.api.Test
    public void testAddDoubleConditionalMultipleEndAlterFirstConditional() throws IOException, Exception {
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"returnValue\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"===10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[\"a13\"]}";
        
        TestDataFactory.addDoubleConditionalMultipleEnd(request, tester);
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(new ChartItem("a10", "returnValue", "a1", "Notes", "===10")) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testAddDoubleConditionalMultipleEndAlterSecondConditional() throws IOException, Exception {
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"retrievedata\",\"prevItemId\":\"a111\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a1\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Test1\",\"condition\":null},"
                + "{\"id\":\"a10\",\"type\":\"addNotes\",\"prevItemId\":\"a1\",\"caption\":\"Notes\",\"condition\":\"<10\"},"
                + "{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a11\",\"type\":\"conditional\",\"prevItemId\":\"a3\",\"caption\":\"Test2\",\"condition\":null},"
                + "{\"id\":\"a12\",\"type\":\"returnValue\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\"===11\"},"
                + "{\"id\":\"a13\",\"type\":\"orderLabs\",\"prevItemId\":\"a11\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a20\",\"type\":\"end\",\"prevItemId\":\"a10\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[\"a13\"]}";
        
        TestDataFactory.addDoubleConditionalMultipleEnd(request, tester);
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(new ChartItem("a12", "returnValue", "a11", "Procedure", "===11")) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + TestDataFactory.chartItemToURLString(mockConditional2.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHypertensionSituation() throws IOException, Exception {
        String expectedResponse = "{\"state\":[{\"id\":\"a1\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"subroutine\",\"prevItemId\":\"a1\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a3\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a4\",\"type\":\"retrievedata\",\"prevItemId\":\"a3\",\"caption\":\"Risico_HVZ\",\"condition\":\"===true\"},"
                + "{\"id\":\"a6\",\"type\":\"conditional\",\"prevItemId\":\"a4\",\"caption\":\"Risico_HVZ\",\"condition\":null},"
                + "{\"id\":\"a7\",\"type\":\"retrievedata\",\"prevItemId\":\"a6\",\"caption\":\"Gebruikt_diuretica\",\"condition\":\">20\"},"
                + "{\"id\":\"a8\",\"type\":\"conditional\",\"prevItemId\":\"a7\",\"caption\":\"Gebruikt_diuretica\",\"condition\":null},"
                + "{\"id\":\"a9\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"ACE-remmers\",\"condition\":\"===true\"},"
                + "{\"id\":\"a10\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"Diuretica\",\"condition\":null},"
                + "{\"id\":\"a5\",\"type\":\"end\",\"prevItemId\":\"a3\",\"caption\":\"Stop\",\"condition\":null}], \"endLines\":"
                + "[\"a6\", \"a9\", \"a10\"]}";
        
        TestDataFactory.setUpTestHypertensionSituation(request, tester);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());               
    }
    
    @org.junit.jupiter.api.Test
    public void testHypertensionSituationChangeFirstConditional() throws IOException, Exception {
        String expectedResponse = "{\"state\":[{\"id\":\"a1\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"subroutine\",\"prevItemId\":\"a1\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a3\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a4\",\"type\":\"retrievedata\",\"prevItemId\":\"a3\",\"caption\":\"Risico_HVZ\",\"condition\":\"!==true\"},"
                + "{\"id\":\"a6\",\"type\":\"conditional\",\"prevItemId\":\"a4\",\"caption\":\"Risico_HVZ\",\"condition\":null},"
                + "{\"id\":\"a7\",\"type\":\"retrievedata\",\"prevItemId\":\"a6\",\"caption\":\"Gebruikt_diuretica\",\"condition\":\">20\"},"
                + "{\"id\":\"a8\",\"type\":\"conditional\",\"prevItemId\":\"a7\",\"caption\":\"Gebruikt_diuretica\",\"condition\":null},"
                + "{\"id\":\"a9\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"ACE-remmers\",\"condition\":\"===true\"},"
                + "{\"id\":\"a10\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"Diuretica\",\"condition\":null},"
                + "{\"id\":\"a5\",\"type\":\"end\",\"prevItemId\":\"a3\",\"caption\":\"Stop\",\"condition\":null}], \"endLines\":"
                + "[\"a6\", \"a9\", \"a10\"]}";
        
        TestDataFactory.setUpTestHypertensionSituationChangeFirst(request, tester);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());               
    }
    
    @org.junit.jupiter.api.Test
    public void testHypertensionSituationChangeSecondConditional() throws IOException, Exception {
        String expectedResponse = "{\"state\":[{\"id\":\"a1\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"subroutine\",\"prevItemId\":\"a1\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a3\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a4\",\"type\":\"retrievedata\",\"prevItemId\":\"a3\",\"caption\":\"Risico_HVZ\",\"condition\":\"===true\"},"
                + "{\"id\":\"a6\",\"type\":\"conditional\",\"prevItemId\":\"a4\",\"caption\":\"Risico_HVZ\",\"condition\":null},"
                + "{\"id\":\"a7\",\"type\":\"retrievedata\",\"prevItemId\":\"a6\",\"caption\":\"Gebruikt_diuretica\",\"condition\":\"<20\"},"
                + "{\"id\":\"a8\",\"type\":\"conditional\",\"prevItemId\":\"a7\",\"caption\":\"Gebruikt_diuretica\",\"condition\":null},"
                + "{\"id\":\"a9\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"ACE-remmers\",\"condition\":\"===true\"},"
                + "{\"id\":\"a10\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"Diuretica\",\"condition\":null},"
                + "{\"id\":\"a5\",\"type\":\"end\",\"prevItemId\":\"a3\",\"caption\":\"Stop\",\"condition\":null}], \"endLines\":"
                + "[\"a9\", \"a10\", \"a6\"]}";
        
        TestDataFactory.setUpTestHypertensionSituationChangeSecond(request, tester);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent()); 
    }
    
    @org.junit.jupiter.api.Test
    public void testHypertensionSituationChangeThirdConditional() throws IOException, Exception {
        String expectedResponse = "{\"state\":[{\"id\":\"a1\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a2\",\"type\":\"subroutine\",\"prevItemId\":\"a1\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a3\",\"type\":\"conditional\",\"prevItemId\":\"a2\",\"caption\":\"Hypertensie\",\"condition\":null},"
                + "{\"id\":\"a4\",\"type\":\"retrievedata\",\"prevItemId\":\"a3\",\"caption\":\"Risico_HVZ\",\"condition\":\"===true\"},"
                + "{\"id\":\"a6\",\"type\":\"conditional\",\"prevItemId\":\"a4\",\"caption\":\"Risico_HVZ\",\"condition\":null},"
                + "{\"id\":\"a7\",\"type\":\"retrievedata\",\"prevItemId\":\"a6\",\"caption\":\"Gebruikt_diuretica\",\"condition\":\">20\"},"
                + "{\"id\":\"a8\",\"type\":\"conditional\",\"prevItemId\":\"a7\",\"caption\":\"Gebruikt_diuretica\",\"condition\":null},"
                + "{\"id\":\"a9\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"ACE-remmers\",\"condition\":\"!==true\"},"
                + "{\"id\":\"a10\",\"type\":\"newPrescription\",\"prevItemId\":\"a8\",\"caption\":\"Diuretica\",\"condition\":null},"
                + "{\"id\":\"a5\",\"type\":\"end\",\"prevItemId\":\"a3\",\"caption\":\"Stop\",\"condition\":null}], \"endLines\":"
                + "[\"a6\", \"a9\", \"a10\"]}";
        
        TestDataFactory.setUpTestHypertensionSituationChangeThird(request, tester);
        
        request.setURI("/katool?function=state");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent()); 
    }
    
}