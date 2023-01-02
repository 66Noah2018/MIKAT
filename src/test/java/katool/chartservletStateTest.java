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
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author RLvan
 */
public class chartservletStateTest {
    private static ServletTester tester;
    private static HttpTester request;
    private static HttpTester response;
    
    @BeforeAll
    public static void setUpClass() throws Exception {}
    
    @AfterAll
    public static void tearDownClass() {}
    
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
    public void tearDown() {}
    
    // Tests for state
    
    @org.junit.jupiter.api.Test
    public void testGetState() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
        
        TestDataFactory.addFourElements(request, tester);
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
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=getElement&id=a333");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetElementComplex() throws IOException, Exception{
        String expectedResponse = "{\"chartItem\":{\"id\":\"a3\",\"type\":\"retrievedata\",\"prevItemId\":\"a1\",\"caption\":\"Test2\",\"condition\":null}}";
        
        TestDataFactory.addDoubleConditionalMultipleEnd(request, tester);
        request.setURI("/katool?function=getElement&id=a3");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNonexistentElement() throws IOException, Exception{
        String expectedResponse = "{\"chartItem\":null}";
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=getElement&id=a334");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for hasNext
    
    @org.junit.jupiter.api.Test
    public void testHasNextSimple() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":true}";
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=hasNext&id=a333");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHasNextComplex() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":true}";
        
        TestDataFactory.addDoubleConditionalMultipleEnd(request, tester);
        request.setURI("/katool?function=hasNext&id=a11");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHasNextFalse() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":false}";
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=hasNext&id=a0");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testHasNextNonexistentId() throws IOException, Exception{
        String expectedResponse = "{\"hasNext\":false}";
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=hasNext&id=a01010101");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for getNext
    
    @org.junit.jupiter.api.Test
    public void testGetNext() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null}}";
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=getNext&id=a111");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNextComplex() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":{\"id\":\"a12\",\"type\":\"newProcedure\",\"prevItemId\":\"a11\",\"caption\":\"Procedure\",\"condition\":\">=11\"}}";
        
        TestDataFactory.addDoubleConditionalSingleEnd(request, tester);
        request.setURI("/katool?function=getNext&id=a11");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNextNoNext() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":null}";
        
        TestDataFactory.addDoubleConditionalSingleEnd(request, tester);
        request.setURI("/katool?function=getNext&id=a20");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetNextNonexistentId() throws IOException, Exception{
        String expectedResponse = "{\"nextItem\":null}";
        
        TestDataFactory.addDoubleConditionalSingleEnd(request, tester);
        request.setURI("/katool?function=getNext&id=a2022");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
}
