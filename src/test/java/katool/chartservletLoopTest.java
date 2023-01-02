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
public class chartservletLoopTest {
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
    
    // Tests for getClosestLoopStart
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartSingleLoopNoEnd() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"Plural1\"}";
        
        TestDataFactory.addSingleLoopNoEnd(request, tester);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a6");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartDoubleLoopNoEndOuterLoop() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"Plural1\"}";
        
        TestDataFactory.addDoubleLoopNoEnd(request, tester);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a6");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartDoubleLoopNoEndInnerLoop() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"Plural2\"}";
        
        TestDataFactory.addDoubleLoopNoEnd(request, tester);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a30");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testGetClosestLoopStartSingleLoopExpectNull() throws IOException, Exception{
        String expectedResponse = "{\"caption\":\"null\"}";
        
        TestDataFactory.addSingleLoopNoEnd(request, tester);
        request.setURI("/katool?function=getClosestLoopStart&prevItemId=a111");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Tests for loopHasEnd. Cannot test for hasEnd due to bad requests (End for ... causes 400 in Jetty, but works in UI)
    
    @org.junit.jupiter.api.Test
    public void testLoopHasEndSingleLoopNoEnd() throws IOException, Exception{
        String expectedResponse = "{\"hasEnd\":false}";
        
        TestDataFactory.addSingleLoopNoEnd(request, tester);
        request.setURI("/katool?function=loopHasEnd&caption=Plural1");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testLoopHasEndDoubleLoopNoEndInner() throws IOException, Exception{
        String expectedResponse = "{\"hasEnd\":false}";
        
        TestDataFactory.addDoubleLoopNoEnd(request, tester);
        request.setURI("/katool?function=loopHasEnd&caption=Plural2");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    @org.junit.jupiter.api.Test
    public void testLoopHasEndDoubleLoopNoEndOuter() throws IOException, Exception{
        String expectedResponse = "{\"hasEnd\":false}";
        
        TestDataFactory.addDoubleLoopNoEnd(request, tester);
        request.setURI("/katool?function=loopHasEnd&caption=Plural1");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
}
