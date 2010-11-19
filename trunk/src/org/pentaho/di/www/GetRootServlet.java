/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.logging.LogWriter;


public class GetRootServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/";
    
    private static LogWriter log = LogWriter.getInstance();
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getRequestURI().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), Messages.getString("GetRootServlet.RootRequested"));
        
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
        PrintWriter out = response.getWriter();
        
        out.println("<HTML>");
        out.println("<HEAD><TITLE>" + Messages.getString("GetRootServlet.KettleSlaveServer.Title")+ "</TITLE></HEAD>");
        out.println("<BODY>");
        out.println("<H2>"+ Messages.getString("GetRootServlet.SlaveServerMenu")+ "</H2>");

        out.println("<p>");
        out.println("<a href=\"/kettle/status\">"+ Messages.getString("GetRootServlet.ShowStatus")+"</a><br>");

        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");
    }

    public String toString()
    {
        return "Root Handler";
    }
}
