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
/**
 * 
 */
package org.pentaho.di.core.dnd;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * This class contains code to help you drag data from one 
 * part of a GUI to another by using XML as an intermediate step.
 *  
 * @author matt
 * @since 2006-04-16
 *
 */
public class DragAndDropContainer implements XMLInterface
{
	public static final int TYPE_STEP                = 1;
	public static final int TYPE_BASE_STEP_TYPE      = 2;
	public static final int TYPE_DATABASE_CONNECTION = 3;
	public static final int TYPE_TRANS_HOP           = 4;
	public static final int TYPE_TEXT                = 5;
    public static final int TYPE_JOB_ENTRY           = 6;
    public static final int TYPE_BASE_JOB_ENTRY      = 7;
    public static final int TYPE_PHYSICAL_TABLE      = 8;
    public static final int TYPE_PHYSICAL_COLUMN     = 9;
    public static final int TYPE_BUSINESS_VIEW       = 10;
    public static final int TYPE_BUSINESS_TABLE      = 11;
    public static final int TYPE_BUSINESS_COLUMN     = 12;
    public static final int TYPE_RELATIONSHIP        = 13;
    public static final int TYPE_BUSINESS_MODEL      = 14;
    
    
	private static final String typeCodes[] = { 
        "", 
        "Step", 
        "BaseStep", 
        "DatabaseConnection", 
        "TransHop", 
        "Text", 
        "Jobentry", 
        "BaseJobentry",
        "PhysicalTable", 
        "PhysicalColumn", 
        "BusinessView", 
        "BusinessTable", 
        "BusinessColumn", 
        "Relationship",
        "Business Model"
    };
	
	private int type;
	private String data;
	
	/** 
	 * Create a new DragAndDropContainer
	 * 
	 * @param type The type of drag&drop to perform
	 * @param data The data in the form of a String
	 */
	public DragAndDropContainer(int type, String data)
	{
		this.type = type;
		this.data = data;
	}

	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public String getData()
	{
		return data;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
	
	public String getTypeCode()
	{
		if (type<=0 || type>=typeCodes.length) return null;
		
		return typeCodes[type];
	}
	
	public static final int getType(String typeCode)
	{
		for (int i=1;i<typeCodes.length;i++)
		{
			if (typeCodes[i].equals(typeCode)) return i;
		}
		return 0;
	}
	
	public String getXML()
	{
		try
		{
			StringBuffer xml = new StringBuffer(100);
			
			xml.append(XMLHandler.getXMLHeader()); // UFT-8 XML header
			
			xml.append("<DragAndDrop>").append(Const.CR);
			xml.append("  ").append(XMLHandler.addTagValue("DragType", getTypeCode()));
			xml.append("  ").append(XMLHandler.addTagValue("Data", 
			    new String( Base64.encodeBase64(data.getBytes(Const.XML_ENCODING)))));
			xml.append("</DragAndDrop>").append(Const.CR);

      return xml.toString();
		}
		catch(UnsupportedEncodingException e)
		{
			throw new RuntimeException("Unable to encode String in encoding ["+Const.XML_ENCODING+"]", e);
		}
	} 

	/**
	 * Construct a Drag and drop container from an XML String
	 * @param xml The XML string to convert from
	 */
	public DragAndDropContainer(String xml) throws KettleXMLException
	{
		try
		{
			Document doc = XMLHandler.loadXMLString(xml);
			Node dnd = XMLHandler.getSubNode(doc, "DragAndDrop"); 
			
			type = getType( XMLHandler.getTagValue(dnd, "DragType") );
      data = new String( Base64.decodeBase64( XMLHandler.getTagValue(dnd, "Data").getBytes() ), Const.XML_ENCODING );
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unexpected error parsing Drag & Drop XML fragment: "+xml, e);
		}
	}
}
