 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.addxml;

import java.util.List;
import java.util.Map;

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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;





/**
 * This class knows how to handle the MetaData for the XML output step
 * 
 * @since 14-jan-2006
 *
 */

public class AddXMLMeta extends BaseStepMeta  implements StepMetaInterface
{
    /** The base name of the output file */

    /** Flag: ommit the XML Header*/
    private  boolean omitXMLheader;

    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;

    /** The name value containing the resulting XML fragment */
    private String valueName;

    /** The name of the repeating row XML element */
    private String rootNode;

    /* THE FIELD SPECIFICATIONS ... */
    
    /** The output fields */
    private  XMLField outputFields[];


    public AddXMLMeta()
    {
        super(); // allocate BaseStepMeta
    }
    
    
    
    /**
     * @return Returns the zipped.
     */
    public boolean isOmitXMLheader()
    {
        return omitXMLheader;
    }



    /**
     * @param omitXMLheader The omit XML header flag to set.
     */
    public void setOmitXMLheader(boolean omitXMLheader)
    {
        this.omitXMLheader = omitXMLheader;
    }



    /**
     * @return Returns the outputFields.
     */
    public XMLField[] getOutputFields()
    {
        return outputFields;
    }
    
    /**
     * @param outputFields The outputFields to set.
     */
    public void setOutputFields(XMLField[] outputFields)
    {
        this.outputFields = outputFields;
    }
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);
    }

    public void allocate(int nrfields)
    {
        outputFields = new XMLField[nrfields];
    }
    
    public Object clone()
    {
        AddXMLMeta retval = (AddXMLMeta)super.clone();
        int nrfields=outputFields.length;
        
        retval.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            retval.outputFields[i] = (XMLField) outputFields[i].clone();
        }
        
        return retval;
    }
    
    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            encoding         = XMLHandler.getTagValue(stepnode, "encoding");
            valueName      = XMLHandler.getTagValue(stepnode, "valueName");
            rootNode    = XMLHandler.getTagValue(stepnode, "xml_repeat_element");

            omitXMLheader    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "omitXMLheader"));
            
            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nrfields= XMLHandler.countNodes(fields, "field");
    
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
            
                outputFields[i] = new XMLField();
                outputFields[i].setFieldName( XMLHandler.getTagValue(fnode, "name") );
                outputFields[i].setElementName( XMLHandler.getTagValue(fnode, "element") );
                outputFields[i].setType( XMLHandler.getTagValue(fnode, "type") );
                outputFields[i].setFormat( XMLHandler.getTagValue(fnode, "format") );
                outputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
                outputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
                outputFields[i].setGroupingSymbol( XMLHandler.getTagValue(fnode, "group") );
                outputFields[i].setNullString( XMLHandler.getTagValue(fnode, "nullif") );
                outputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
                outputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
                outputFields[i].setAttribute( "Y".equalsIgnoreCase( XMLHandler.getTagValue(fnode, "attribute") ) );
                outputFields[i].setAttributeParentName( XMLHandler.getTagValue(fnode, "attributeParentName") );
            }
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }

    public void setDefault()
    {
        omitXMLheader    = true;
        encoding         = Const.XML_ENCODING;
        
        valueName        = "xmlvaluename";
        rootNode         = "Row";


        int nrfields=0;
        
        allocate(nrfields);
                    
        for (int i=0;i<nrfields;i++)
        {
            outputFields[i] = new XMLField();

            outputFields[i].setFieldName( "field"+i );              
            outputFields[i].setElementName( "field"+i );              
            outputFields[i].setType( "Number" );
            outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" );
            outputFields[i].setCurrencySymbol( "" );
            outputFields[i].setDecimalSymbol( "," );
            outputFields[i].setGroupingSymbol(  "." );
            outputFields[i].setNullString( "" );
            outputFields[i].setLength( -1 );
            outputFields[i].setPrecision( -1 );
            outputFields[i].setAttribute( false );
            outputFields[i].setElementName( "field"+i ); 
        }
    }
    
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        
    	ValueMetaInterface v=new ValueMeta(this.getValueName(), ValueMetaInterface.TYPE_STRING);
        v.setOrigin(name);
        row.addValueMeta( v );
    }

    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        
        retval.append("    ").append(XMLHandler.addTagValue("encoding",  encoding));
        retval.append("    ").append(XMLHandler.addTagValue("valueName",  valueName));
        retval.append("    ").append(XMLHandler.addTagValue("xml_repeat_element",  rootNode));

        retval.append("    <file>").append(Const.CR);
        retval.append("      ").append(XMLHandler.addTagValue("omitXMLheader", omitXMLheader));
        retval.append("    </file>").append(Const.CR);
        retval.append("    <fields>").append(Const.CR);
        for (int i=0;i<outputFields.length;i++)
        {
            XMLField field = outputFields[i];
            
            if (field.getFieldName()!=null && field.getFieldName().length()!=0)
            {
                retval.append("      <field>").append(Const.CR);
                retval.append("        ").append(XMLHandler.addTagValue("name",      field.getFieldName()));
                retval.append("        ").append(XMLHandler.addTagValue("element",   field.getElementName()));
                retval.append("        ").append(XMLHandler.addTagValue("type",      field.getTypeDesc()));
                retval.append("        ").append(XMLHandler.addTagValue("format",    field.getFormat()));
                retval.append("        ").append(XMLHandler.addTagValue("currency",  field.getCurrencySymbol()));
                retval.append("        ").append(XMLHandler.addTagValue("decimal",   field.getDecimalSymbol()));
                retval.append("        ").append(XMLHandler.addTagValue("group",     field.getGroupingSymbol()));
                retval.append("        ").append(XMLHandler.addTagValue("nullif",    field.getNullString()));
                retval.append("        ").append(XMLHandler.addTagValue("length",    field.getLength()));
                retval.append("        ").append(XMLHandler.addTagValue("precision", field.getPrecision()));
                retval.append("        ").append(XMLHandler.addTagValue("attribute", field.isAttribute()));
                retval.append("        ").append(XMLHandler.addTagValue("attributeParentName",   field.getAttributeParentName()));
                retval.append("        </field>").append(Const.CR);
            }
        }
        retval.append("    </fields>"+Const.CR);

        return retval.toString();
    }
    
    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try
        {
            encoding        =      rep.getStepAttributeString (id_step, "encoding");
            valueName     =      rep.getStepAttributeString (id_step, "valueName");
            rootNode   =      rep.getStepAttributeString (id_step, "xml_repeat_element");
            
            omitXMLheader          =      rep.getStepAttributeBoolean(id_step, "omitXMLheader");
    
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");
            
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                outputFields[i] = new XMLField();

                outputFields[i].setFieldName(       rep.getStepAttributeString (id_step, i, "field_name") );
                outputFields[i].setElementName(     rep.getStepAttributeString (id_step, i, "field_element") );
                outputFields[i].setType(            rep.getStepAttributeString (id_step, i, "field_type") );
                outputFields[i].setFormat(          rep.getStepAttributeString (id_step, i, "field_format") );
                outputFields[i].setCurrencySymbol(  rep.getStepAttributeString (id_step, i, "field_currency") );
                outputFields[i].setDecimalSymbol(   rep.getStepAttributeString (id_step, i, "field_decimal") );
                outputFields[i].setGroupingSymbol(  rep.getStepAttributeString (id_step, i, "field_group") );
                outputFields[i].setNullString(      rep.getStepAttributeString (id_step, i, "field_nullif") );
                outputFields[i].setLength(     (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
                outputFields[i].setPrecision(  (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
                outputFields[i].setAttribute(       rep.getStepAttributeBoolean(id_step, i, "field_attribute") );
                outputFields[i].setAttributeParentName(   rep.getStepAttributeString(id_step, i, "field_attributeName") );
            }       
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "encoding",           encoding);
            rep.saveStepAttribute(id_transformation, id_step, "valueName",          valueName);
            rep.saveStepAttribute(id_transformation, id_step, "xml_repeat_element", rootNode);
            rep.saveStepAttribute(id_transformation, id_step, "omitXMLheader",        omitXMLheader);
            
            for (int i=0;i<outputFields.length;i++)
            {
                XMLField field = outputFields[i];
                
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getFieldName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_element",   field.getElementName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field.getTypeDesc());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    field.getFormat());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  field.getCurrencySymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   field.getDecimalSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     field.getGroupingSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    field.getNullString());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    field.getLength());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_attribute", field.isAttribute());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_attributeName", field.getAttributeParentName());
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {

    	CheckResult cr;
        // TODO - add checks for empty fieldnames 
        
        // Check output fields
        if (prev!=null && prev.size()>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddXMLMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
            remarks.add(cr);
            
            String  error_message="";
            boolean error_found=false;
            
            // Starting from selected fields in ...
            for (int i=0;i<outputFields.length;i++)
            {
                int idx = prev.indexOfValue(outputFields[i].getFieldName());
                if (idx<0)
                {
                    error_message+="\t\t"+outputFields[i].getFieldName()+Const.CR;
                    error_found=true;
                } 
            }
            if (error_found) 
            {
                error_message=Messages.getString("AddXMLMeta.CheckResult.FieldsNotFound", error_message);
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddXMLMeta.CheckResult.AllFieldsFound"), stepMeta);
                remarks.add(cr);
            }
        }
        
        // See if we have input streams leading to this step!
        if (input.length>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddXMLMeta.CheckResult.ExpectedInputOk"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AddXMLMeta.CheckResult.ExpectedInputError"), stepMeta);
            remarks.add(cr);
        }
        
        cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, Messages.getString("AddXMLMeta.CheckResult.FilesNotChecked"), stepMeta);
        remarks.add(cr);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new AddXML(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new AddXMLData();
    }



    public String getEncoding()
    {
        return encoding;
    }


    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }


    /**
     * @return Returns the rootNode.
     */
    public String getRootNode()
    {
        return rootNode;
    }

    /**
     * @param rootNode The root node to set.
     */
    public void setRootNode(String rootNode)
    {
        this.rootNode = rootNode;
    }

    
    public String getValueName() {
        return valueName;
    }


    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
    
}
