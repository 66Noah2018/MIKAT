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
public class chartservletUndoRedoTest {
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

    
    // Test for undo
    @org.junit.jupiter.api.Test
    public void testUndo() throws IOException, Exception{
        String expectedResponse = "{\"state\":["
                + "{\"id\":\"a111\",\"type\":\"start\",\"prevItemId\":\"-1\",\"caption\":\"Start\",\"condition\":null},"
                + "{\"id\":\"a222\",\"type\":\"newProcedure\",\"prevItemId\":\"a111\",\"caption\":\"Procedure\",\"condition\":null},"
                + "{\"id\":\"a333\",\"type\":\"orderLabs\",\"prevItemId\":\"a222\",\"caption\":\"Labs\",\"condition\":null}"
                + "], \"endLines\":[], \"size\":2}";
        
        TestDataFactory.addFourElements(request, tester);
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
        
        TestDataFactory.addFourElements(request, tester);
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
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=undoSize");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
    // Test for redoSize
    @org.junit.jupiter.api.Test
    public void testRedoSize() throws IOException, Exception{
        String expectedResponse = "{\"size\":1}";
        
        TestDataFactory.addFourElements(request, tester);
        request.setURI("/katool?function=undo");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=redoSize");
        
        response.parse(tester.getResponses(request.generate()));
        assertEquals(expectedResponse, response.getContent());
    }
    
}
