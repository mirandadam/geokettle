 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Samatar Hassan and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan.
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.httppost;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/*
 * Created on 15-jan-2009
 * 
 */

public class HTTPPOSTMeta extends BaseStepMeta implements StepMetaInterface
{
    /** URL / service to be called */
    private String  url;

    /** function arguments : fieldname*/
    private String  argumentField[];
    
    /** function query field : queryField*/
    private String  queryField[];
    

    /** IN / OUT / INOUT */
    private String  argumentParameter[];
    
    private String  queryParameter[];
    

    /** function result: new value name */
    private String  fieldName;
    
    private boolean urlInField;
    
    private String urlField;
    
    private String requestEntity;
    
    private String encoding;
    
    private boolean  postafile;

    public HTTPPOSTMeta()
    {
        super(); // allocate BaseStepMeta
    }

    public String getEncoding()
    {
    	return encoding;
    }
    
    public void setEncoding(String encoding)
    {
    	this.encoding=encoding;
    }
    
    /**
     * @return Returns the argument.
     */
    public String[] getArgumentField()
    {
        return argumentField;
    }
    
    /**
     * @param argument The argument to set.
     */
    public void setArgumentField(String[] argument)
    {
        this.argumentField = argument;
    }
    /**
     * @return Returns the argument.
     */
    public String[] getQueryField()
    {
        return queryField;
    }
    
    /**
     * @param queryfield The queryfield to set.
     */
    public void setQueryField(String[] queryfield)
    {
        this.queryField = queryfield;
    }
    
    


    /**
     * @return Returns the argumentDirection.
     */
    public String[] getArgumentParameter()
    {
        return argumentParameter;
    }

    /**
     * @param argumentDirection The argumentDirection to set.
     */
    public void setArgumentParameter(String[] argumentDirection)
    {
        this.argumentParameter = argumentDirection;
    }
    /**
     * @return Returns the queryParameter.
     */
    public String[] getQueryParameter()
    {
        return queryParameter;
    }
    /**
     * @param queryParameter The queryParameter to set.
     */
    public void setQueryParameter(String[] queryParameter)
    {
        this.queryParameter = queryParameter;
    }
    

    /**
     * @return Returns the procedure.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param procedure The procedure to set.
     */
    public void setUrl(String procedure)
    {
        this.url = procedure;
    }
    /**
     * @return Is the url coded in a field?
     */
	public boolean isUrlInField() {
		return urlInField;
	}
	public boolean isPostAFile() {
		return postafile;
	}
	public void setPostAFile(boolean postafile ) {
		this.postafile=postafile;
	}
	
	/**
     * @param urlInField Is the url coded in a field?
     */
	public void setUrlInField(boolean urlInField) {
		this.urlInField = urlInField;
	}
	/**
     * @return The field name that contains the url.
     */
	public String getUrlField() {
		return urlField;
	}
	
	/**
     * @param urlField name of the field that contains the url
     */
	public void setUrlField(String urlField) {
		this.urlField = urlField;
	}
	/**
     * @param requestEntity the requestEntity to set
     */
	public void setRequestEntity(String requestEntity) {
		this.requestEntity = requestEntity;
	}
	/**
     * @return requestEntity
     */
	public String getRequestEntity() {
		return requestEntity;
	}
	
    /**
     * @return Returns the resultName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param resultName The resultName to set.
     */
    public void setFieldName(String resultName)
    {
        this.fieldName = resultName;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode, databases);
    }

    public void allocate(int nrargs)
    {
        argumentField = new String[nrargs];
        argumentParameter = new String[nrargs];
    }
    public void allocateQuery(int nrqueryparams)
    {
        queryField = new String[nrqueryparams];
        queryParameter = new String[nrqueryparams];
    }

    public Object clone()
    {
        HTTPPOSTMeta retval = (HTTPPOSTMeta) super.clone();
       
        int nrargs = argumentField.length;
        retval.allocate(nrargs);
        for (int i = 0; i < nrargs; i++)
        {
            retval.argumentField[i] = argumentField[i];
            retval.argumentParameter[i] = argumentParameter[i];
        }
        
        int nrqueryparams = queryField.length;
        retval.allocateQuery(nrqueryparams);
        for (int i = 0; i < nrqueryparams; i++)
        {
            retval.queryField[i] = queryField[i];
            retval.queryParameter[i] = queryParameter[i];
        }

        return retval;
    }

    public void setDefault()
    {
        int i;
        int nrargs;
        nrargs = 0;
        allocate(nrargs);
        for (i = 0; i < nrargs; i++)
        {
            argumentField[i] = "arg" + i; //$NON-NLS-1$
            argumentParameter[i] = "arg"; //$NON-NLS-1$
        }
        
        int nrquery;
        nrquery = 0;
        allocateQuery(nrquery);
        for (i = 0; i < nrquery; i++)
        {
            queryField[i] = "query" + i; //$NON-NLS-1$
            queryParameter[i] = "query"; //$NON-NLS-1$
        }

        fieldName = "result"; //$NON-NLS-1$
        postafile=false;
    }
    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException    
    {
        if (!Const.isEmpty(fieldName))
        {
            ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(fieldName), ValueMeta.TYPE_STRING);
            inputRowMeta.addValueMeta(v);
        }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("    " + XMLHandler.addTagValue("postafile",          postafile));
        retval.append("    " + XMLHandler.addTagValue("encoding", encoding));
        retval.append("    " + XMLHandler.addTagValue("url", url)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("urlInField",  urlInField));
        retval.append("    " + XMLHandler.addTagValue("urlField",  urlField));
        retval.append("    " + XMLHandler.addTagValue("requestEntity",  requestEntity));
        
        retval.append("    <lookup>" + Const.CR); //$NON-NLS-1$

        for (int i = 0; i < argumentField.length; i++)
        {
            retval.append("      <arg>" + Const.CR); //$NON-NLS-1$
            retval.append("        " + XMLHandler.addTagValue("name", argumentField[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        " + XMLHandler.addTagValue("parameter", argumentParameter[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        </arg>" + Const.CR); //$NON-NLS-1$
        }
        for (int i = 0; i < queryField.length; i++)
        {
            retval.append("      <query>" + Const.CR); //$NON-NLS-1$
            retval.append("        " + XMLHandler.addTagValue("name", queryField[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        " + XMLHandler.addTagValue("parameter", queryParameter[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        </query>" + Const.CR); //$NON-NLS-1$
        }

        retval.append("      </lookup>" + Const.CR); //$NON-NLS-1$

        retval.append("    <result>" + Const.CR); //$NON-NLS-1$
        retval.append("      " + XMLHandler.addTagValue("name", fieldName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      </result>" + Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
    {
        try
        {
        	postafile  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "postafile"));
            encoding = XMLHandler.getTagValue(stepnode, "encoding"); //$NON-NLS-1$
            url = XMLHandler.getTagValue(stepnode, "url"); //$NON-NLS-1$
            urlInField="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "urlInField"));
            urlField       = XMLHandler.getTagValue(stepnode, "urlField");
            requestEntity       = XMLHandler.getTagValue(stepnode, "requestEntity");
            
            
            Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
            
            int nrargs = XMLHandler.countNodes(lookup, "arg"); //$NON-NLS-1$
            allocate(nrargs);
            for (int i = 0; i < nrargs; i++)
            {
                Node anode = XMLHandler.getSubNodeByNr(lookup, "arg", i); //$NON-NLS-1$
                argumentField[i] = XMLHandler.getTagValue(anode, "name"); //$NON-NLS-1$
                argumentParameter[i] = XMLHandler.getTagValue(anode, "parameter"); //$NON-NLS-1$
            }

            int nrquery = XMLHandler.countNodes(lookup, "query"); //$NON-NLS-1$
            allocateQuery(nrquery);

            for (int i = 0; i < nrquery; i++)
            {
                Node anode = XMLHandler.getSubNodeByNr(lookup, "query", i); //$NON-NLS-1$
                queryField[i] = XMLHandler.getTagValue(anode, "name"); //$NON-NLS-1$
                queryParameter[i] = XMLHandler.getTagValue(anode, "parameter"); //$NON-NLS-1$
            }
            
            fieldName = XMLHandler.getTagValue(stepnode, "result", "name"); // Optional, can be null //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("HTTPPOSTMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
        	postafile  = rep.getStepAttributeBoolean(id_step, "postafile");
        	encoding = rep.getStepAttributeString(id_step, "encoding"); //$NON-NLS-1$
            url = rep.getStepAttributeString(id_step, "url"); //$NON-NLS-1$
            urlInField =      rep.getStepAttributeBoolean (id_step, "urlInField");
            urlField	=	   rep.getStepAttributeString (id_step, "urlField");
            requestEntity	=	   rep.getStepAttributeString (id_step, "requestEntity");
            
            int nrargs = rep.countNrStepAttributes(id_step, "arg_name"); //$NON-NLS-1$
            allocate(nrargs);

            for (int i = 0; i < nrargs; i++)
            {
                argumentField[i] = rep.getStepAttributeString(id_step, i, "arg_name"); //$NON-NLS-1$
                argumentParameter[i] = rep.getStepAttributeString(id_step, i, "arg_parameter"); //$NON-NLS-1$
            }
            
            
            int nrquery = rep.countNrStepAttributes(id_step, "query_name"); //$NON-NLS-1$
            allocateQuery(nrquery);

            for (int i = 0; i < nrquery; i++)
            {
                queryField[i] = rep.getStepAttributeString(id_step, i, "query_name"); //$NON-NLS-1$
                queryParameter[i] = rep.getStepAttributeString(id_step, i, "query_parameter"); //$NON-NLS-1$
            }

            fieldName = rep.getStepAttributeString(id_step, "result_name"); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("HTTPPOSTMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
        	rep.saveStepAttribute(id_transformation, id_step, "postafile",          postafile);
        	rep.saveStepAttribute(id_transformation, id_step, "encoding", encoding); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "url", url); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "urlInField",   urlInField);
			rep.saveStepAttribute(id_transformation, id_step, "urlField",   urlField);
			rep.saveStepAttribute(id_transformation, id_step, "requestEntity",   requestEntity);
			
            for (int i = 0; i < argumentField.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_name", argumentField[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_parameter", argumentParameter[i]); //$NON-NLS-1$
            }
            for (int i = 0; i < queryField.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "query_name", queryField[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "query_parameter", queryParameter[i]); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "result_name", fieldName); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("HTTPPOSTMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("HTTPPOSTMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("HTTPPOSTMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        
        // check Url
        if(urlInField)
        {
        	if(Const.isEmpty(urlField))
        		cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("HTTPPOSTMeta.CheckResult.UrlfieldMissing"), stepMeta);	
        	else
        		cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("HTTPPOSTMeta.CheckResult.UrlfieldOk"), stepMeta);	
        	
        }else
        {
        	if(Const.isEmpty(url))
        		cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("HTTPPOSTMeta.CheckResult.UrlMissing"), stepMeta);
        	else
        		cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("HTTPPOSTMeta.CheckResult.UrlOk"), stepMeta);
        }
        remarks.add(cr);

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new HTTPPOST(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new HTTPPOSTData();
    }
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
