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
package org.pentaho.di.core.variables;

import java.util.Map;


/**
 * Interface to implement variable sensitive objects.
 * 
 * @author Sven Boden 
 */
public interface VariableSpace
{
	/**
	 * Initialize variable space using the defaults, copy over the variables 
	 * from the parent (using copyVariablesFrom()), after this the "injected" 
	 * variables should be inserted (injectVariables()). 
	 * 
	 * The parent is set as parent variable space.
	 * 
	 * @param parent the parent to start from, or null if root.
	 */
    void initializeVariablesFrom(VariableSpace parent);
    
    /**
     * Copy the variables from another space, without initializing with the
     * defaults. This does not affect any parent relationship.
     * 
     * @param space the space to copy the variables from.
     */
    void copyVariablesFrom(VariableSpace space);
    
    /**
     * Share a variable space from another variable space. This means
     * that the object should take over the space used as argument.
     * 
     * @param space Variable space to be shared.
     */
    void shareVariablesWith(VariableSpace space);
    
    /**
     * Get the parent of the variable space.
     * 
     * @return the parent.
     */
    VariableSpace getParentVariableSpace();
    
    /**
     * Set the parent variable space
     * @param parent The parent variable space to set
     */
    void setParentVariableSpace(VariableSpace parent);
    
    /**
     * Sets a variable in the Kettle Variables list.
     * 
     * @param variableName The name of the variable to set
     * @param variableValue The value of the variable to set.  If the 
     *                      variableValue is null, the variable is cleared 
     *                      from the list. 
     */
    void setVariable(String variableName, String variableValue);

    /**
     * Get the value of a variable with a default in case the variable 
     * is not found.
     * 
     * @param variableName The name of the variable
     * @param defaultValue The default value in case the variable could not be 
     *                     found
     * @return the String value of a variable
     */
    String getVariable(String variableName, String defaultValue);

    /**
     * Get the value of a variable.
     * 
     * @param variableName The name of the variable
     * @return the String value of a variable or null in case the variable could not be found.
     */
    String getVariable(String variableName);
    
    /**
     * This method returns a boolean for the new variable check boxes.
     * If the variable name is not set or the variable name is not specified, this method simply returns the default value.
     * If not, it convert the variable value to a boolean.  "Y", "YES" and "TRUE" all convert to true. (case insensitive)
     * @see also static method ValueMeta.convertStringToBoolean()
     *
     * @param variableName The variable to look up.
     * @param defaultValue The default value to return.
     * @return
     */
    boolean getBooleanValueOfVariable(String variableName, boolean defaultValue);
    
    /**
     * List the variables (not the values) that are currently in the
     * variable space.
     * 
     * @return Array of String variable names.
     */
    String[] listVariables();
    
    /**
     * Substitute the string using the current variable space.
     * 
     * @param aString The string to substitute.
     * 
     * @return The substituted string.
     */
    String environmentSubstitute(String aString);
    
	/**
	 * Replaces environment variables in an array of strings.
	 *
	 * See also: environmentSubstitute(String string)
	 * 
	 * @param string
	 *            The array of strings that wants its variables to be replaced.
	 * @return the array with the environment variables replaced.
	 */
	public String[] environmentSubstitute(String string[]);    
    
    /**
     * Inject variables. The behaviour should be that the properties
     * object will be stored and at the time the VariableSpace is
     * initialized (or upon calling this method if the space is already 
     * initialized).
     * After injecting the link of the properties object should be removed.
     *  
     * @param prop Properties object containing key-value pairs.
     */
    void injectVariables(Map<String, String> prop);
}