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
package org.pentaho.di.resource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.StringUtil;

public class ResourceReference {
	private ResourceHolderInterface resourceReferenceHolder;
	private List<ResourceEntry> entries;
  
	/**
	 * @param resourceReferenceHolder Where to put the resource references
	 * @param entries the resource entries list
	 */
	public ResourceReference(ResourceHolderInterface resourceReferenceHolder, List<ResourceEntry> entries) {
		super();
		this.resourceReferenceHolder = resourceReferenceHolder;
		this.entries = entries;
	}

	public ResourceReference(ResourceHolderInterface resourceReferenceHolder) {
		this.resourceReferenceHolder = resourceReferenceHolder;
		this.entries = new ArrayList<ResourceEntry>();
	}

	/**
	 * @return the resource reference holder
	 */
	public ResourceHolderInterface getReferenceHolder() {
		return resourceReferenceHolder;
	}

	/**
	 * @param resourceReferenceHolder
	 *            the resource reference holder to set
	 */
	public void setReferenceHolder(ResourceHolderInterface resourceReferenceHolder) {
		this.resourceReferenceHolder = resourceReferenceHolder;
	}

	/**
	 * @return the entries
	 */
	public List<ResourceEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries(List<ResourceEntry> entries) {
		this.entries = entries;
	}
	
  public String toXml() {
    return toXml(null, 0);
  }
  
  public String toXml(ResourceXmlPropertyEmitterInterface injector) {
    return toXml(injector, 0);
  }
  
  public String toXml(int indentLevel) {
    return toXml(null, indentLevel);
  }
  
  public String toXml(ResourceXmlPropertyEmitterInterface injector, int indentLevel) {
    StringBuffer buff = new StringBuffer();
    addXmlElementWithAttribute(buff, indentLevel, "ActionComponent", "type", resourceReferenceHolder.getHolderType()); //$NON-NLS-1$ //$NON-NLS-2$
    indentLevel++;
    addXmlElement(buff, indentLevel, "ComponentName", resourceReferenceHolder.getName()); //$NON-NLS-1$
    addXmlElement(buff, indentLevel, "ComponentId", resourceReferenceHolder.getTypeId()); //$NON-NLS-1$
    addXmlElement(buff, indentLevel, "ComponentResources"); //$NON-NLS-1$
    indentLevel++;
    for (ResourceEntry entry : this.getEntries()) {
      buff.append(entry.toXml(indentLevel));
    }
    indentLevel--;
    addXmlCloseElement(buff, indentLevel, "ComponentResources"); //$NON-NLS-1$
    if (injector != null) {
      addXmlElement(buff, indentLevel, "ComponentProperties"); //$NON-NLS-1$
      indentLevel++;
      buff.append(injector.getExtraResourceProperties(resourceReferenceHolder, indentLevel));
      indentLevel--;
      addXmlCloseElement(buff, indentLevel, "ComponentProperties"); //$NON-NLS-1$
    }
    indentLevel--;
    addXmlCloseElement(buff, indentLevel, "ActionComponent"); //$NON-NLS-1$
    return buff.toString();
  }

  public void addXmlElementWithAttribute(StringBuffer buff, int indentLevel, String elementName, String attrName, String attrValue) {
    buff.append(StringUtil.getIndent(indentLevel)).append("<").append(elementName).append(" ").append(attrName).append("='");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    buff.append(attrValue).append("'>").append(StringUtil.CRLF); //$NON-NLS-1$
  }
  
  public void addXmlCloseElement(StringBuffer buff, int indentLevel, String elementName) {
    buff.append(StringUtil.getIndent(indentLevel)).append("</").append(elementName).append(">").append(StringUtil.CRLF);//$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public void addXmlElement(StringBuffer buff, int indentLevel, String elementName) {
    buff.append(StringUtil.getIndent(indentLevel)).append("<").append(elementName).append(">").append(StringUtil.CRLF);//$NON-NLS-1$ //$NON-NLS-2$ 
  }
  
  public void addXmlElement(StringBuffer buff, int indentLevel, String elementName, String elementValue) {
    buff.append(StringUtil.getIndent(indentLevel)).append("<").append(elementName).append("><![CDATA[").append(elementValue).append("]]></").append(elementName).append(">").append(StringUtil.CRLF); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

}
