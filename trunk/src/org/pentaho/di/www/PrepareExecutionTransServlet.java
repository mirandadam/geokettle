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
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;


public class PrepareExecutionTransServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/prepareExec";
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public PrepareExecutionTransServlet(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), Messages.getString("PrepareExecutionTransServlet.TransPrepareExecutionRequested"));
        

        String transName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        response.setStatus(HttpServletResponse.SC_OK);
        
        PrintWriter out = response.getWriter();
        if (useXML)
        {
            response.setContentType("text/xml");
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        }
        else
        {
        	
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD>");
            out.println("<TITLE>"+ Messages.getString("PrepareExecutionTransServlet.TransPrepareExecution") + "</TITLE>");
            out.println("<META http-equiv=\"Refresh\" content=\"2;url=/kettle/transStatus?name="+URLEncoder.encode(transName, "UTF-8")+"\">");
            out.println("</HEAD>");
            out.println("<BODY>");
        }
    
        try
        {
            Trans trans = transformationMap.getTransformation(transName);
            TransConfiguration transConfiguration = transformationMap.getConfiguration(transName);
            if (trans!=null && transConfiguration!=null)
            {
                TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
                // Set the appropriate logging, variables, arguments, replay date, ...
                // etc.
                log.setLogLevel(executionConfiguration.getLogLevel());
                trans.getTransMeta().setArguments(executionConfiguration.getArgumentStrings());
                trans.setReplayDate(executionConfiguration.getReplayDate());
                trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
                trans.injectVariables(executionConfiguration.getVariables());
                
                // Log to a String
                Log4jStringAppender appender = LogWriter.createStringAppender();
                log.addAppender(appender);
                transformationMap.addAppender(transName, appender);
                
                try {
                	trans.prepareExecution(null);

                    if (useXML)
                    {
                        out.println(WebResult.OK.getXML());
                    }
                    else
                    {
                    	
                        out.println("<H1>" + Messages.getString("PrepareExecutionTransServlet.TransPrepared",transName) + "</H1>");
                        out.println("<a href=\"/kettle/transStatus?name="+URLEncoder.encode(transName, "UTF-8")+"\">"+ Messages.getString("TransStatusServlet.BackToTransStatusPage") +"</a><p>");
                    }
                }
                catch (Exception e) {
                	
                    if (useXML)
                    {
                        out.println(new WebResult(WebResult.STRING_ERROR, Messages.getString("PrepareExecutionTransServlet.Error.TransInitFailed",Const.CR+appender.getBuffer().toString()+Const.CR+e.getLocalizedMessage())));
                        
                    }
                    else
                    {
                        out.println("<H1>" + Messages.getString("PrepareExecutionTransServlet.Log.TransNotInit",transName) + "</H1>");
                        
                        out.println("<pre>");
                        out.println(appender.getBuffer().toString());
                        out.println(e.getLocalizedMessage());
                        out.println("</pre>");
                        out.println("<a href=\"/kettle/transStatus?name="+URLEncoder.encode(transName, "UTF-8")+"\">" + Messages.getString("TransStatusServlet.BackToTransStatusPage") + "</a><p>");
                    }
                }
            }
            else
            {
                if (useXML)
                {
                    out.println(new WebResult(WebResult.STRING_ERROR, Messages.getString("TransStatusServlet.Log.CoundNotFindSpecTrans",transName)));
                }
                else
                {
                    out.println("<H1>" + Messages.getString("TransStatusServlet.Log.CoundNotFindTrans",transName) + "</H1>");
                    out.println("<a href=\"/kettle/status\">" + Messages.getString("TransStatusServlet.BackToStatusPage")+"</a><p>");
                }
            }
        }
        catch (Exception ex)
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, Messages.getString("PrepareExecutionTransServlet.Error.UnexpectedError",Const.CR+Const.getStackTracker(ex))));
                
            }
            else
            {
                out.println("<p>");
                out.println("<pre>");
                ex.printStackTrace(out);
                out.println("</pre>");
            }
        }

        if (!useXML)
        {
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }
    }

    public String toString()
    {
        return "Start transformation";
    }
}
