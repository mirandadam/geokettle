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

public interface ResourceXmlPropertyEmitterInterface {

  /**
   * Allows injection of additional relevant properties in the
   * to-xml of the Resource Reference.
   * @param ref The Resource Reference Holder (a step, or a job entry)
   * @param indention If -1, then no indenting, otherwise, it's the indent level to indent the XML strings
   * @return String of injected XML
   */
  public String getExtraResourceProperties(ResourceHolderInterface ref, int indention);
  
}
