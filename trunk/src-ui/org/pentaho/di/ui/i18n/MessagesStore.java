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
package org.pentaho.di.ui.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.exception.KettleException;

/**
 * This class contains a messages store: for a certain Locale and for a certain
 * messages package, it keeps all the keys and values. This class can read and
 * write messages files...
 * 
 * @author matt
 * 
 */
public class MessagesStore extends ChangedFlag {
	private String locale;

	private String messagesPackage;

	private Map<String, String> messagesMap;
	
	private String filename;

	/**
	 * Create a new messages store
	 * @param locale
	 * @param messagesPackage
	 */
	public MessagesStore(String locale, String messagesPackage) {
		messagesMap = new Hashtable<String, String>();
		this.locale = locale;
		this.messagesPackage = messagesPackage;
	}
	
	
	public void read(List<String> directories) throws KettleException {
		try
		{
			String filename = getLoadFilename(directories);

			Properties properties = new Properties();
			FileInputStream fileInputStream = new FileInputStream(new File(filename));
			properties.load(fileInputStream);
			fileInputStream.close();
			
			// Put all the properties in our map...
			//
			for (Object key : properties.keySet()) {
				Object value = properties.get(key);
				messagesMap.put((String)key, (String)value);
			}
		}
		catch (Exception e) {
			throw new KettleException("Unable to read messages file for locale : '"+locale+"' and package '"+messagesPackage+"'", e);
		}
	}
	
	public void write() throws KettleException {
		if (filename==null) {
			throw new KettleException("Please specify a filename before saving messages store for package '"+messagesPackage+"' and locale '"+locale+"");
		}
		write(filename);
	}
	
	public void write(String filename) throws KettleException {
		try {
			Properties properties = new Properties();
			for (String key : messagesMap.keySet()) {
				properties.put(key, messagesMap.get(key));
			}
			FileOutputStream fileOutputStream = new FileOutputStream(new File(filename));
			String comment = "File generated by Pentaho Translator for package '"+messagesPackage+"' in locale '"+locale+"'"+Const.CR+Const.CR;
			properties.store(fileOutputStream, comment);
			fileOutputStream.close();
			setChanged(false);
		}
		catch(IOException e) {
			throw new KettleException("Unable to save messages properties file '"+filename+"'", e);
		}
	}
	
	/**
	 * Find a suitable filename for the specified locale and messages package.
	 * It tries to find the file in the specified directories in the order that they are specified.
	 * 
	 * @param directories the source directories to try and map the messages files against.
	 * @return the filename that was found.
	 */
	public String getLoadFilename(List<String> directories) throws FileNotFoundException {
		String localeUpperLower = locale.substring(0, 3).toLowerCase()+locale.substring(3).toUpperCase();
		
		String filename="messages_"+localeUpperLower+".properties";
		String path=messagesPackage.replace('.', '/');
		
		for (String directory : directories) {
			String attempt = directory+Const.FILE_SEPARATOR+path+Const.FILE_SEPARATOR+"messages"+Const.FILE_SEPARATOR+filename;
			if (new File(attempt).exists()) return attempt;
		}
		throw new FileNotFoundException("package '"+(path+Const.FILE_SEPARATOR+"messages"+Const.FILE_SEPARATOR+filename)+"' could not be found");
	}
	
	public String getSourceDirectory(List<String> directories) {
		String localeUpperLower = locale.substring(0, 3).toLowerCase()+locale.substring(3).toUpperCase();
		
		String filename="messages_"+localeUpperLower+".properties";
		String path=messagesPackage.replace('.', '/');
		
		for (String directory : directories) {
			String attempt = directory+Const.FILE_SEPARATOR+path+Const.FILE_SEPARATOR+"messages"+Const.FILE_SEPARATOR+filename;
			if (new File(attempt).exists()) return directory;
		}
		return null;
	}

	/**
	 * Find a suitable filename to save this information in the specified locale and messages package.
	 * It needs a source directory to save the package in
	 * 
	 * @param directory the source directory to save the messages file in.
	 * @return the filename that was generated.
	 */
	public String getSaveFilename(String directory) {
		String localeUpperLower = locale.substring(0, 3).toLowerCase()+locale.substring(3).toUpperCase();
		
		String filename="messages_"+localeUpperLower+".properties";
		String path=messagesPackage.replace('.', '/');
		
		return directory+Const.FILE_SEPARATOR+path+Const.FILE_SEPARATOR+"messages"+Const.FILE_SEPARATOR+filename;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale
	 *            the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the messagesPackage
	 */
	public String getMessagesPackage() {
		return messagesPackage;
	}

	/**
	 * @param messagesPackage
	 *            the messagesPackage to set
	 */
	public void setMessagesPackage(String messagesPackage) {
		this.messagesPackage = messagesPackage;
	}

	/**
	 * @return the map
	 */
	public Map<String, String> getMessagesMap() {
		return messagesMap;
	}

	/**
	 * @param messsagesMap
	 *            the map to set
	 */
	public void setMessagesMap(Map<String, String> messsagesMap) {
		this.messagesMap = messsagesMap;
	}


	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}
}
