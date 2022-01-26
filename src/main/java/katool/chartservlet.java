/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package katool;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author RLvan
 */
public class chartservlet extends HttpServlet {

    private List<ChartItem> currentState = new ArrayList<>();
    private Deque<List<ChartItem>> undoStack = new LinkedList();
    private Deque<List<ChartItem>> redoStack = new LinkedList();
    private final Integer MAX_DEQUE_SIZE = 10;
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        switch(request.getParameter("function")){
            case "update": //no response, because this is merely a backend save. the changes are already processed at the frontend
                updateState(request);
                break;
            case "undo":
                Integer undoSize = undo();
                response.getWriter().write("{'state': JSONEncoder.encodeChart(currentState), 'size':" + undoSize + "}");
                break;
            case "redo":
                Integer redoSize = redo();
                response.getWriter().write("{'state': JSONEncoder.encodeChart(currentState), 'size':" + redoSize + "}");
                break;
            case "undoSize":
                response.getWriter().write("{'size':" + undoStack.size() + "}");
                break;                
            case "redoSize":
                response.getWriter().write("{'size':" + redoStack.size() + "}");
                break;
            case "localmap":
                String localMapping = getLocalMapping();
                response.getWriter().write(localMapping);
                break;
            case "standardmap":
                String standardizedMapping = getStandardizedMapping();
                response.getWriter().write(standardizedMapping);
                break;
            default:
                break;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void updateState(HttpServletRequest request) throws JsonProcessingException{
        //?function="update"&item="JSONString"
        String json = request.getParameter("item");
        ChartItem newItem = JSONDecoder.decodeItem(json);
        Integer itemIndex = findIndexOf(newItem);
        maintainMaxDequeSize("undo");
        undoStack.addFirst(currentState);
        currentState.set(itemIndex, newItem);
    }
    
    private Integer undo(){
        maintainMaxDequeSize("undo");
        redoStack.addFirst(currentState);
        currentState = undoStack.removeFirst();
        return undoStack.size();
    }
    
    private Integer redo(){
        maintainMaxDequeSize("redo");
        undoStack.addFirst(currentState);
        currentState = redoStack.removeFirst();
        return redoStack.size();
    }
    
    private void maintainMaxDequeSize(String dequeName)throws NullPointerException{
        switch (dequeName) {
            case "undo":
                if (undoStack.size() >= MAX_DEQUE_SIZE){
                    undoStack.removeLast();
                }
                break;
            case "redo":
                if (redoStack.size() >= MAX_DEQUE_SIZE){
                    redoStack.removeLast();
                }
                break;
            default:
                throw new NullPointerException("This deque does not exist: " + dequeName);
        }
    }
    
    private Integer findIndexOf(ChartItem item){
        Integer itemIndex = 0;
        String itemId = item.getId();
        for (Integer index = 0; index < currentState.size(); index++){
            if (itemId.equals(currentState.get(index).getId())){
                itemIndex = index;
                break;
            }
        }
        return itemIndex;
    }
    
    private String getLocalMapping(){
        return "{\"test1\":\"test1query\", \"test2\": \"test2query\", \"test3\":\"test3query\"}";
    }
    
    private String getStandardizedMapping(){
        return "{\"standardtest1\":\"test1\", \"standardtest2\":\"test2\", \"standardtest3\":\"test3\"}";
    }
}
