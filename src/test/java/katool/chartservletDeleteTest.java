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
public class chartservletDeleteTest {
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
    
    @org.junit.jupiter.api.Test
    public void testRemoveSecondOfFourElements() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a111\",\"caption\":\"Labs\",\"condition\":null},"
                + "{\"id\":\"a0\",\"type\":\"end\",\"prevItemId\":\"a333\",\"caption\":\"End\",\"condition\":null}"
                + "], \"endLines\":[]}";
                
        TestDataFactory.addFourElements(request, tester);
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
        
        TestDataFactory.addFourElements(request, tester);
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
        
        TestDataFactory.addSingleConditionalMultipleEnd(request, tester);
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
        
        TestDataFactory.addDoubleConditionalMultipleEnd(request, tester);
        request.setURI("/katool?function=delete&id=a20");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
}
