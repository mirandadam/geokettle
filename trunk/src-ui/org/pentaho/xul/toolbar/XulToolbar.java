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
package org.pentaho.xul.toolbar;

import org.pentaho.xul.XulItem;

public interface XulToolbar extends XulItem {

	public boolean handleMenuEvent( String id );

	public void setEnableById( String id, boolean enabled );
	
	public void setHintById( String id, String text );
	
	public void setTextById( String id, String text );
	
	public void addMenuListener(String id, Object listener, String methodName);

	public int getMode();
	
	public void setMode(int mode);
	
	public XulToolbarButton getButtonById(String id);

	public String[] getMenuItemIds();
	
	/**
	 * @return the last native item to allow you to change it if it's a separator
	 * TODO: this is somewhat of a hack
	 */
	public Object getLastNativeItem();
}
