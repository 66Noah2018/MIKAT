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
    private static HttpTester request;
    private static HttpTester response;

    @BeforeAll
    public static void setUpClass() throws Exception {}
    
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

    
    //Tests for localmap and standardmap?    
    
    // Tests for file?
    
    // Tests for open?
    
    // Tests for save?
    
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

   }