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
import java.util.LinkedList;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;

/**
 *
 * @author RLvan
 */
public class TestDataFactory {
    
    private static final ChartItem mockStart = new ChartItem("a111", "start", "-1", "Start", null);
    private static final ChartItem mockElement1 = new ChartItem("a222", "newProcedure", "a111", "Procedure", null);
    private static final ChartItem mockElement2 = new ChartItem("a333", "orderLabs", "a222", "Labs", null);
    private static final ChartItem mockEnd = new ChartItem("a0", "end", "a333", "End", null);
    private static final ChartItem mockRetrieveData = new ChartItem("a2", "retrievedata", "a111", "Test1", null);
    private static LinkedList<ChartItem> mockConditional;
    private static final ChartItem mockRetrieveData2 = new ChartItem("a3", "retrievedata", "a1", "Test2", null);
    private static LinkedList<ChartItem> mockConditional2;
    private static final ChartItem mockLoop = new ChartItem("a5", "loop", "a4", "Plural1", null);
    private static final ChartItem mockLoopFirstAction = new ChartItem("a6", "orderLabs", "a5", "Labs", null);
    private static final ChartItem mockRetrieveDataPlural2 = new ChartItem("a8", "retrievedata", "a6", "Plural2", null);
    private static final ChartItem mockLoop2 = new ChartItem("a9", "loop", "a8", "Plural2", null);
    private static final ChartItem mockLoop2FirstAction = new ChartItem("a30", "addNotes", "a9", "Notes", null);
    private static final ChartItem mockRetrieveDataPlural = new ChartItem("a4", "retrievedata", "a111", "Plural1", null);
    
    public static void addFourElements(HttpTester request, ServletTester tester) throws IOException, Exception{
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockElement1));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockElement2));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockEnd));
        tester.getResponses(request.generate());
    }
    
    public static void addSingleConditionalSingleEnd(HttpTester request, ServletTester tester) throws IOException, Exception{
        setUpMockConditional();
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
    
    public static void addSingleConditionalMultipleEnd(HttpTester request, ServletTester tester) throws IOException, Exception{
        addSingleConditionalSingleEnd(request, tester);
        
        request.setURI("/katool?function=update&" + chartItemToURLString(new ChartItem("a21", "addDiagnosis", "a3", "Diagnosis", null)));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=endline&id=\"a21\"");
        tester.getResponses(request.generate());
    }
    
    public static void addDoubleConditionalSingleEnd(HttpTester request, ServletTester tester) throws IOException, Exception{
        addSingleConditionalSingleEnd(request, tester);
        setUpMockConditional2();
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional2.get(0)) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional2.get(1)) + "&isMultipart=true");
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockConditional2.get(2)) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
    }
    
    public static void addDoubleConditionalMultipleEnd(HttpTester request, ServletTester tester) throws IOException, Exception{
        addDoubleConditionalSingleEnd(request, tester);
        
        request.setURI("/katool?function=endline&id=\"a13\"");
        tester.getResponses(request.generate());
    }
    
    public static void setUpTestHypertensionSituation(HttpTester request, ServletTester tester) throws IOException, Exception {
        ChartItem start2 = new ChartItem("a1", "start", "-1", "Start", null);
        ChartItem subroutineHypertensie = new ChartItem("a2", "subroutine", "a1", "Hypertensie", null);
        ChartItem conditionalHypertensie = new ChartItem("a3", "conditional", "a2", "Hypertensie", null);
        ChartItem retrieveRisico = new ChartItem("a4", "retrievedata", "a3", "Risico_HVZ", "===true");
        ChartItem end = new ChartItem("a5", "end", "a3", "Stop", null);
        ChartItem conditionalRisico = new ChartItem("a6", "conditional", "a4", "Risico_HVZ", null);
        ChartItem retrieveDiuretica = new ChartItem("a7", "retrievedata", "a6", "Gebruikt_diuretica", ">20");
        ChartItem conditionalDiuretica = new ChartItem("a8", "conditional", "a7", "Gebruikt_diuretica", null);
        ChartItem prescribeACE = new ChartItem("a9", "newPrescription", "a8", "ACE-remmers", "===true");
        ChartItem prescribeDiuretica = new ChartItem("a10", "newPrescription", "a8", "Diuretica", null);
        
        request.setURI("/katool?function=update&" + chartItemToURLString(start2));
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(subroutineHypertensie));
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalHypertensie) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(retrieveRisico) + "&isMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(end) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalRisico) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(retrieveDiuretica) + "&isMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=endline&id=a6");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalDiuretica) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(prescribeACE) + "&isMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(prescribeDiuretica) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=endline&id=a9");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=endline&id=a10");
        tester.getResponses(request.generate());
    }
    
    public static void setUpTestHypertensionSituationChangeFirst(HttpTester request, ServletTester tester) throws IOException, Exception{
        setUpTestHypertensionSituation(request, tester);
        ChartItem conditionalHypertensie = new ChartItem("a3", "conditional", "a2", "Hypertensie", null);
        ChartItem retrieveRisico = new ChartItem("a4", "retrievedata", "a3", "Risico_HVZ", "!==true");
        ChartItem end = new ChartItem("a5", "end", "a3", "Stop", null);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalHypertensie) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(retrieveRisico) + "&isMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(end) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
    }
    
    public static void setUpTestHypertensionSituationChangeSecond(HttpTester request, ServletTester tester) throws IOException, Exception{
        setUpTestHypertensionSituation(request, tester);
        ChartItem conditionalRisico = new ChartItem("a6", "conditional", "a4", "Risico_HVZ", null);
        ChartItem retrieveDiuretica = new ChartItem("a7", "retrievedata", "a6", "Gebruikt_diuretica", "<20");
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalRisico) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(retrieveDiuretica) + "&isMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=endline&id=a6");
        tester.getResponses(request.generate());
    }
    
    public static void setUpTestHypertensionSituationChangeThird(HttpTester request, ServletTester tester) throws IOException, Exception {
        setUpTestHypertensionSituation(request, tester);
        ChartItem conditionalDiuretica = new ChartItem("a8", "conditional", "a7", "Gebruikt_diuretica", null);
        ChartItem prescribeACE = new ChartItem("a9", "newPrescription", "a8", "ACE-remmers", "!==true");
        ChartItem prescribeDiuretica = new ChartItem("a10", "newPrescription", "a8", "Diuretica", null);
        request.setURI("/katool?function=update&" + chartItemToURLString(conditionalDiuretica) + "&isMultipart=true&firstMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(prescribeACE) + "&isMultipart=true");
        tester.getResponses(request.generate());
        request.setURI("/katool?function=update&" + chartItemToURLString(prescribeDiuretica) + "&isMultipart=true&finalMultipart=true");
        tester.getResponses(request.generate());
    }

    public static void addSingleLoopNoEnd(HttpTester request, ServletTester tester) throws IOException, Exception {
        request.setURI("/katool?function=update&" + chartItemToURLString(mockStart));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockRetrieveDataPlural));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoop));
        tester.getResponses(request.generate());
        
        request.setURI("/katool?function=update&" + chartItemToURLString(mockLoopFirstAction));
        tester.getResponses(request.generate());
    }
    
    public static void addDoubleLoopNoEnd(HttpTester request, ServletTester tester) throws IOException, Exception{
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
    
    public static String chartItemToURLString(ChartItem item) {
        return "id=" + item.getId() + 
                "&type=" + item.getType() + 
                "&prevItemId=" + item.getPrevItemId() + 
                "&caption="  + item.getCaption() + 
                "&condition=" + item.getCondition(); 
    }
    
    private static void setUpMockConditional(){
        mockConditional = new LinkedList<>();
        mockConditional.add(new ChartItem("a1", "conditional", "a2", "Test1", null));
        mockConditional.add(new ChartItem("a10", "addNotes", "a1", "Notes", "<10"));
        mockConditional.add(mockRetrieveData2);
    }
    
    private static void setUpMockConditional2(){
        mockConditional2 = new LinkedList<>();
        mockConditional2.add(new ChartItem("a11", "conditional", "a3", "Test2", null));
        mockConditional2.add(new ChartItem("a12", "newProcedure", "a11", "Procedure", ">=11"));
        mockConditional2.add(new ChartItem("a13", "orderLabs", "a11", "Labs", null));
    }
}
