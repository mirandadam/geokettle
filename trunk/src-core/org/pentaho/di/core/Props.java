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

 


package org.pentaho.di.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.SortedFileOutputStream;


/**
 * We use Props to store all kinds of user interactive information such as the selected colors, fonts, positions of windows, etc.
 * 
 * @author Matt
 * @since 15-12-2003
 *
 */
public class Props implements Cloneable
{
	protected static Props props;
	
	public static final String STRING_FONT_FIXED_NAME  = "FontFixedName";
	public static final String STRING_FONT_FIXED_SIZE  = "FontFixedSize";
	public static final String STRING_FONT_FIXED_STYLE = "FontFixedStyle";

	public static final String STRING_FONT_DEFAULT_NAME  = "FontDefaultName";
	public static final String STRING_FONT_DEFAULT_SIZE  = "FontDefaultSize";
	public static final String STRING_FONT_DEFAULT_STYLE = "FontDefaultStyle";

	public static final String STRING_FONT_GRAPH_NAME  = "FontGraphName";
	public static final String STRING_FONT_GRAPH_SIZE  = "FontGraphSize";
	public static final String STRING_FONT_GRAPH_STYLE = "FontGraphStyle";

	public static final String STRING_FONT_GRID_NAME  = "FontGridName";
	public static final String STRING_FONT_GRID_SIZE  = "FontGridSize";
	public static final String STRING_FONT_GRID_STYLE = "FontGridStyle";

	public static final String STRING_FONT_NOTE_NAME  = "FontNoteName";
	public static final String STRING_FONT_NOTE_SIZE  = "FontNoteSize";
	public static final String STRING_FONT_NOTE_STYLE = "FontNoteStyle";

	public static final String STRING_BACKGROUND_COLOR_R = "BackgroundColorR";
	public static final String STRING_BACKGROUND_COLOR_G = "BackgroundColorG";
	public static final String STRING_BACKGROUND_COLOR_B = "BackgroundColorB";

	public static final String STRING_GRAPH_COLOR_R = "GraphColorR";
	public static final String STRING_GRAPH_COLOR_G = "GraphColorG";
	public static final String STRING_GRAPH_COLOR_B = "GraphColorB";

	public static final String STRING_TAB_COLOR_R = "TabColorR";
	public static final String STRING_TAB_COLOR_G = "TabColorG";
	public static final String STRING_TAB_COLOR_B = "TabColorB";

	public static final String STRING_ICON_SIZE   = "IconSize";
	public static final String STRING_LINE_WIDTH  = "LineWidth";
	public static final String STRING_SHADOW_SIZE = "ShadowSize";
	public static final String STRING_LOG_LEVEL   = "LogLevel";	
	public static final String STRING_LOG_FILTER  = "LogFilter";
	public static final String STRING_MIDDLE_PCT  = "MiddlePct";
	
	public static final String STRING_LAST_PREVIEW_TRANS = "LastPreviewTrans";
	public static final String STRING_LAST_PREVIEW_STEP  = "LastPreviewStep";
	public static final String STRING_LAST_PREVIEW_SIZE  = "LastPreviewSize";
		
	public static final String STRING_MAX_UNDO  = "MaxUndo";

	public static final String STRING_SIZE_MAX = "SizeMax";
	public static final String STRING_SIZE_X   = "SizeX";
	public static final String STRING_SIZE_Y   = "SizeY";
	public static final String STRING_SIZE_W   = "SizeW";
	public static final String STRING_SIZE_H   = "SizeH";

	public static final String STRING_SASH_W1  = "SashWeight1";
	public static final String STRING_SASH_W2  = "SashWeight2";

	public static final String STRING_SHOW_TIPS               = "ShowTips";
	public static final String STRING_TIP_NR                  = "TipNr";
	public static final String STRING_AUTO_SAVE               = "AutoSave";
	public static final String STRING_SAVE_CONF               = "SaveConfirmation";
	public static final String STRING_AUTO_SPLIT              = "AutoSplit";
	public static final String STRING_AUTO_COLLAPSE_CORE_TREE = "AutoCollapseCoreObjectsTree";

	public static final String STRING_USE_DB_CACHE            = "UseDBCache";
	public static final String STRING_OPEN_LAST_FILE          = "OpenLastFile";
	
	public static final String STRING_LAST_REPOSITORY_LOGIN   = "RepositoryLastLogin";
	public static final String STRING_LAST_REPOSITORY         = "RepositoryLast";
	
	public static final String STRING_ONLY_ACTIVE_STEPS       = "OnlyActiveSteps";
    public static final String STRING_START_SHOW_REPOSITORIES = "ShowRepositoriesAtStartup";
    public static final String STRING_ANTI_ALIASING           = "EnableAntiAliasing";
    public static final String STRING_SHOW_EXIT_WARNING       = "ShowExitWarning";
    public static final String STRING_SHOW_OS_LOOK            = "ShowOSLook";
    public static final String STRING_LAST_ARGUMENT           = "LastArgument";

    public static final String STRING_ARGUMENT_NAME_PREFIX    = "Argument ";
    
    public static final String STRING_CUSTOM_PARAMETER        = "CustomParameter";
    
    public static final String STRING_PLUGIN_HISTORY          = "PluginHistory";

    public static final String STRING_DEFAULT_PREVIEW_SIZE    = "DefaultPreviewSize";
    public static final String STRING_ONLY_USED_DB_TO_XML     = "SaveOnlyUsedConnectionsToXML";
		
    public static final String STRING_ASK_ABOUT_REPLACING_DATABASES = "AskAboutReplacingDatabases";
    public static final String STRING_REPLACE_DATABASES             = "ReplaceDatabases";

    private static final String STRING_MAX_NR_LINES_IN_LOG = "MaxNrOfLinesInLog";
    private static final String STRING_MAX_NR_LINES_IN_HISTORY = "MaxNrOfLinesInHistory";

    protected LogWriter log = LogWriter.getInstance();
	protected Properties properties;
	    
    protected ArrayList<ObjectUsageCount> pluginHistory;
	
    protected int type;
    protected String filename;
	
	public static final int TYPE_PROPERTIES_EMPTY   = 0;
	public static final int TYPE_PROPERTIES_SPOON   = 1;
	public static final int TYPE_PROPERTIES_PAN     = 2;
	public static final int TYPE_PROPERTIES_CHEF    = 3;
	public static final int TYPE_PROPERTIES_KITCHEN = 4;
	public static final int TYPE_PROPERTIES_MENU    = 5;
	public static final int TYPE_PROPERTIES_PLATE   = 6;
    
    public static final int WIDGET_STYLE_DEFAULT = 0;
    public static final int WIDGET_STYLE_FIXED   = 1;
    public static final int WIDGET_STYLE_TABLE   = 2;
    public static final int WIDGET_STYLE_NOTEPAD = 3;
    public static final int WIDGET_STYLE_GRAPH   = 4;
    public static final int WIDGET_STYLE_TAB     = 5;




 


	/**
	 * Initialize the properties: load from disk.
	 * @param display The Display
	 * @param t The type of properties file.
	 */
	public static final void init(int t)
	{
		if (props==null)
		{
			props = new Props(t);
            
		}
		else
		{
			throw new RuntimeException("The Properties systems settings are already initialised!");
		}
	}
    
    /**
     * Initialize the properties: load from disk.
     * @param display The Display
     * @param filename the filename to use 
     */
    public static final void init(String filename)
    {
        if (props==null)
        {
            props = new Props(filename);
            
        }
        else
        {
            throw new RuntimeException("The properties systems settings are already initialised!");
        }
    }
	
	/**
	 * Check to see whether the Kettle properties where loaded.
	 * @return true if the Kettle properties where loaded.
	 */
	public static boolean isInitialized()
	{
		return props!=null;
	}
	
	public static Props getInstance()
	{
		if (props!=null) return props;
		
		throw new RuntimeException("Properties, Kettle systems settings, not initialised!");
	}
	
	protected Props(int t)
	{
		type=t;
        filename=getFilename();
        init();
	}

	protected void init() {
		properties = new Properties();
        pluginHistory = new ArrayList<ObjectUsageCount>();

        loadProps();
        addDefaultEntries();
        
        loadPluginHistory();
	}
	
	protected Props(String filename)
    {
        properties = new Properties();
        this.type=TYPE_PROPERTIES_EMPTY;
        this.filename=filename;
        init();
    }
    
    public String toString()
    {
        return "User preferences";
    }

    public String getFilename()
	{
		String directory = Const.getKettleDirectory();
		String filename = "";
		
		// Try to create the directory...
		File dir = new File(directory);
		try 
		{ 
			dir.mkdirs();
			
			// Also create a file called kettle.properties
			//
			createDefaultKettleProperties(directory);
		} 
		catch(Exception e) 
		{ 
			
		}
		
		switch(type)
		{
			case TYPE_PROPERTIES_SPOON:
			case TYPE_PROPERTIES_PAN:
				filename=directory+Const.FILE_SEPARATOR+".spoonrc";
				break;
			case TYPE_PROPERTIES_CHEF:
			case TYPE_PROPERTIES_KITCHEN:
				filename=directory+Const.FILE_SEPARATOR+".chefrc";
				break;
			case TYPE_PROPERTIES_MENU:
				filename=directory+Const.FILE_SEPARATOR+".menurc";
				break;
			case TYPE_PROPERTIES_PLATE:
				filename=directory+Const.FILE_SEPARATOR+".platerc";
				break;
			default: break;
		}

		return filename;
	}
	
	public void createDefaultKettleProperties(String directory) {
		String kpFile = directory+Const.FILE_SEPARATOR+Const.KETTLE_PROPERTIES;
		File file = new File(kpFile);
		if (!file.exists()) 
		{
			FileOutputStream out = null;
			try 
			{
				out = new FileOutputStream(file);
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line01", Const.VERSION)+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line02")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line03")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line04")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line05")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line06")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line07")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line08")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line09")+Const.CR).getBytes());
				out.write((Messages.getString("Props.Kettle.Properties.Sample.Line10")+Const.CR).getBytes());
			} 
			catch (IOException e) 
			{
				log.logError(Const.KETTLE_PROPERTIES, Messages.getString("Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES, kpFile), e);
			}
			finally 
			{
				if (out!=null) {
					try {
						out.close();
					} catch (IOException e) {
						log.logError(Const.KETTLE_PROPERTIES, Messages.getString("Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES, kpFile), e);
					}
				}
			}
		}
	}

	public String getLicenseFilename()
	{
		String directory = Const.getKettleDirectory();
		String filename = directory+Const.FILE_SEPARATOR+".licence";
		
		// Try to create the directory...
		File dir = new File(directory);
        if (!dir.exists())
        {
            try { dir.mkdirs(); } 
            catch(Exception e) { }
        }
		
		return filename;
	}

	public boolean fileExists()
	{
	    File f = new File(filename);
		return f.exists();
	}
	
	public void setType(int t)
	{
		type=t;
	}
	
	public int getType()
	{
		return type;
	}
	
	public boolean loadProps()
	{
		try
		{
			properties.load(new FileInputStream(filename));
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

    protected void addDefaultEntries()
    {
        if (!properties.containsKey("JobDialogStyle"))
            properties.setProperty("JobDialogStyle", "RESIZE,MAX,MIN");
    }

	public void saveProps()
	{
		
        File spoonRc = new File(filename);
		try
		{
            // FileOutputStream fos = new FileOutputStream(spoonRc);

            SortedFileOutputStream fos = new SortedFileOutputStream(spoonRc);
            fos.setLogger(log);
			properties.store(fos, "Kettle Properties file");
            fos.close();
            log.logDetailed(toString(), org.pentaho.di.core.Messages.getString("Spoon.Log.SaveProperties"));
		}
		catch(IOException e)
		{
            // If saving fails this could be a known Java bug: If running Spoon on windows the spoon
            // config file gets created with the 'hidden' attribute set. Some Java JREs cannot open
            // FileOutputStreams on files with that attribute set. The user has to unset that attribute
            // manually.
            if (spoonRc.isHidden() && filename.indexOf('\\') != -1)
            {
                // If filename contains a backslash we consider Spoon as running on Windows
                log.logError(toString(), org.pentaho.di.core.Messages.getString("Spoon.Log.SavePropertiesFailedWindowsBugAttr", filename));
            }
            else
            {
                // Another reason why the save failed
                log.logError(toString(), org.pentaho.di.core.Messages.getString("Spoon.Log.SavePropertiesFailed") + e.getMessage());
            }
		}
	}
    
	public void setLogLevel(String level)
	{
		properties.setProperty(STRING_LOG_LEVEL, level);
	}

	public String getLogLevel()
	{
		String level = properties.getProperty(STRING_LOG_LEVEL, "Basic");
		return level;
	}

	public void setLogFilter(String filter)
	{
		properties.setProperty(STRING_LOG_FILTER, Const.NVL(filter, ""));
	}

	public String getLogFilter()
	{
		String level = properties.getProperty(STRING_LOG_FILTER, "");
		return level;
	}
	
	public void setUseDBCache(boolean use)
	{
		properties.setProperty(STRING_USE_DB_CACHE, use?"Y":"N");
	}

	public boolean useDBCache()
	{
		String use=properties.getProperty(STRING_USE_DB_CACHE);
		return !"N".equalsIgnoreCase(use);
	}

	public void setLastRepository(String repname)
	{
		properties.setProperty(STRING_LAST_REPOSITORY, repname);
	}
	
	public String getLastRepository()
	{
		return properties.getProperty(STRING_LAST_REPOSITORY);
	}

	public void setLastRepositoryLogin(String login)
	{
		properties.setProperty(STRING_LAST_REPOSITORY_LOGIN, login);
	}
	
	public String getLastRepositoryLogin()
	{
		return properties.getProperty(STRING_LAST_REPOSITORY_LOGIN);
	}

	public void setOnlyActiveSteps(boolean only)
	{
		properties.setProperty(STRING_ONLY_ACTIVE_STEPS, only?"Y":"N");
	}
	
	public boolean getOnlyActiveSteps()
	{
		String only = properties.getProperty(STRING_ONLY_ACTIVE_STEPS, "N");
		return "Y".equalsIgnoreCase(only); // Default: show active steps.
	}
    
    public boolean askAboutReplacingDatabaseConnections()
    {
        String ask = properties.getProperty(STRING_ASK_ABOUT_REPLACING_DATABASES, "N");
        return "Y".equalsIgnoreCase(ask);
    }
    
    public void setProperty( String propertyName, String value ) {
        properties.setProperty(propertyName, value);
    }
    
    public String getProperty( String propertyName ) {
        return properties.getProperty(propertyName);
    }
    
    public void setAskAboutReplacingDatabaseConnections(boolean ask)
    {
        properties.setProperty(STRING_ASK_ABOUT_REPLACING_DATABASES, ask?"Y":"N");
    }

    /**
     * @param parameterName The parameter name
     * @param defaultValue The default value in case the parameter doesn't exist yet.
     * @return The custom parameter
     */
    public String getCustomParameter(String parameterName, String defaultValue)
    {
        return properties.getProperty(STRING_CUSTOM_PARAMETER+parameterName, defaultValue);
    }
    
    /**
     * Set the custom parameter
     * @param parameterName The name of the parameter
     * @param value The value to be stored in the properties file.
     */
    public void setCustomParameter(String parameterName, String value)
    {
        properties.setProperty(STRING_CUSTOM_PARAMETER+parameterName, value);
    }

    public void clearCustomParameters()
    {
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (key.startsWith(STRING_CUSTOM_PARAMETER)) // Clear this one
            {
                properties.remove(key);
            }
        }
    }

    
    /**
     * Convert "argument 1" to 1
     * @param value The value to determine the argument number for
     * @return The argument number
     */
    public static final int getArgumentNumber(ValueMetaInterface value)
    {
        if (value!=null && value.getName().startsWith(Props.STRING_ARGUMENT_NAME_PREFIX))
        {
            return Const.toInt(value.getName().substring(Props.STRING_ARGUMENT_NAME_PREFIX.length()), -1);
        }
        return -1;
    }
    
    public static final String[] convertArguments(RowMetaAndData row)
    {
        String args[] = new String[10];
        for (int i=0;i<row.size();i++)
        {
            ValueMetaInterface valueMeta = row.getValueMeta(i);
            int argNr = getArgumentNumber(valueMeta);
            if (argNr>=0 && argNr<10)
            {
                try
                {
                    args[argNr] = row.getString(i, "");
                }
                catch (KettleValueException e)
                {
                    args[argNr] = ""; // Should never happen
                }
            }
        }
        return args;
    }
    
    /**
     * Set the last arguments so that we can recall it the next time...
     * 
     * @param args the arguments to save
     */
    public void setLastArguments(String args[])
    {
        for (int i=0;i<args.length;i++)
        {
            if (args[i]!=null)
            {
                properties.setProperty(STRING_LAST_ARGUMENT+"_"+i, args[i]);
            }
        }
    }

    /** Get the last entered arguments...
     * 
     * @return the last entered arguments...
     */
    public String[] getLastArguments()
    {
        String args[] = new String[10];
        for (int i=0;i<args.length;i++)
        {
            args[i] = properties.getProperty(STRING_LAST_ARGUMENT+"_"+i);
        }
        return args;
    }
    
    /**
     * Get the list of recently used step
     * @return a list of strings: the plug-in IDs
     */
    public List<ObjectUsageCount> getPluginHistory()
    {
        return pluginHistory;
    }

    public int increasePluginHistory(String pluginID)
    {
        for (int i=0;i<pluginHistory.size();i++)
        {
            ObjectUsageCount usage = pluginHistory.get(i);
            if (usage.getObjectName().equalsIgnoreCase(pluginID))
            {
                int uses = usage.increment();
                Collections.sort(pluginHistory);
                savePluginHistory();
                return uses;
            }
        }
        addPluginHistory(pluginID, 1);
        Collections.sort(pluginHistory);
        savePluginHistory();
        
        return 1;
    }
    
    /*
    /**
     * Set the last plugin used in the plugin history
     * @param pluginID The last plugin ID
     */
    public void addPluginHistory(String pluginID, int uses)
    {
        // Add at the front
        pluginHistory.add(new ObjectUsageCount(pluginID, uses));
    }

    /**
     * Load the plugin history from the properties file
     *
     */
    protected void loadPluginHistory()
    {
        pluginHistory = new ArrayList<ObjectUsageCount>();
        int i=0;
        String string = properties.getProperty(STRING_PLUGIN_HISTORY+"_"+i);
        while (string!=null)
        {
            pluginHistory.add(ObjectUsageCount.fromString(string));
            i++;
            string = properties.getProperty(STRING_PLUGIN_HISTORY+"_"+i);
        }
        
        Collections.sort(pluginHistory);
    }
    
    private void savePluginHistory()
    {
        for (int i=0;i<pluginHistory.size();i++)
        {
            ObjectUsageCount usage = pluginHistory.get(i);
            properties.setProperty(STRING_PLUGIN_HISTORY+"_"+i, usage.toString());
        }
    }

    public boolean areOnlyUsedConnectionsSavedToXML()
    {
        String show = properties.getProperty(STRING_ONLY_USED_DB_TO_XML, "N");
        return !"N".equalsIgnoreCase(show); // Default: save all connections
    }
    
    public void setOnlyUsedConnectionsSavedToXML(boolean onlyUsedConnections)
    {
        properties.setProperty(STRING_ONLY_USED_DB_TO_XML, onlyUsedConnections?"Y":"N");
    }

    public boolean replaceExistingDatabaseConnections()
    {
        String replace = properties.getProperty(STRING_REPLACE_DATABASES, "Y");
        return "Y".equalsIgnoreCase(replace);
    }

    public void setReplaceDatabaseConnections(boolean replace)
    {
        properties.setProperty(STRING_REPLACE_DATABASES, replace?"Y":"N");
    }
    
    public int getMaxNrLinesInLog()
    {
        String lines = properties.getProperty(STRING_MAX_NR_LINES_IN_LOG);
        return Const.toInt(lines, Const.MAX_NR_LOG_LINES);
    }
    
    public void setMaxNrLinesInLog(int maxNrLinesInLog)
    {
        properties.setProperty(STRING_MAX_NR_LINES_IN_LOG, Integer.toString(maxNrLinesInLog));
    }

    public int getMaxNrLinesInHistory()
    {
        String lines = properties.getProperty(STRING_MAX_NR_LINES_IN_HISTORY);
        return Const.toInt(lines, Const.MAX_NR_HISTORY_LINES);
    }
    
    public void setMaxNrLinesInHistory(int maxNrLinesInHistory)
    {
        properties.setProperty(STRING_MAX_NR_LINES_IN_HISTORY, Integer.toString(maxNrLinesInHistory));
    }

}
