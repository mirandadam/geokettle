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
package org.pentaho.di.core.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;

/**
 * A collection of utilities to manipulate strings.
 * 
 * @author wdeclerc
 */
public class StringUtil
{
	public static final String UNIX_OPEN = "${";

	public static final String UNIX_CLOSE = "}";

	public static final String WINDOWS_OPEN = "%%";

	public static final String WINDOWS_CLOSE = "%%";
	
	public static final String HEX_OPEN = "$[";
	public static final String HEX_CLOSE = "]";
  
    public static final String CRLF = "\r\n"; //$NON-NLS-1$

    public static final String INDENTCHARS = "                    "; //$NON-NLS-1$
    
    public static final String[] SYSTEM_PROPERTIES = new String[] {
    	 "java.version",
    	 "java.vendor",
    	 "java.vendor.url",
    	 "java.home",
    	 "java.vm.specification.version",
    	 "java.vm.specification.vendor",
    	 "java.vm.specification.name",
    	 "java.vm.version",
    	 "java.vm.vendor",
    	 "java.vm.name",
    	 "java.specification.version",
    	 "java.specification.vendor",
    	 "java.specification.name",
    	 "java.class.version",
    	 "java.class.path",
    	 "java.library.path",
    	 "java.io.tmpdir",
    	 "java.compiler",
    	 "java.ext.dirs",
    	 
    	 "os.name",
    	 "os.arch",
    	 "os.version",
    	 
    	 "file.separator",
    	 "path.separator",
    	 "line.separator",
    	 
    	 "user.name",
    	 "user.home",
    	 "user.dir",
    	 "user.country",
    	 "user.language",
    	 "user.timezone",
    	 
    	 "org.apache.commons.logging.Log",
    	 "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
    	 "org.apache.commons.logging.simplelog.showdatetime",
    	 "org.eclipse.swt.browser.XULRunnerInitialized",
    	 "org.eclipse.swt.browser.XULRunnerPath",
    	 
    	 "sun.arch.data.model",
    	 "sun.boot.class.path",
    	 "sun.boot.library.path",
    	 "sun.cpu.endian",
    	 "sun.cpu.isalist",
    	 "sun.io.unicode.encoding",
    	 "sun.java.launcher",
    	 "sun.jnu.encoding",
    	 "sun.management.compiler",
    	 "sun.os.patch.level",
      };
  
	/**
	 * Substitutes variables in <code>aString</code>. Variable names are
	 * delimited by open and close strings. The values are retrieved from the
	 * given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variablesValues
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @param open
	 *            the open delimiter for variables.
	 * @param close
	 *            the close delimiter for variables.
	 * @return the string with the substitution applied.
	 */
	public static String substitute(String aString, Map<String, String> variablesValues, String open, String close)
	{
		return substitute(aString, variablesValues, open, close, 0);
	}

	/**
	 * Substitutes variables in <code>aString</code>. Variable names are
	 * delimited by open and close strings. The values are retrieved from the
	 * given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variablesValues
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @param open
	 *            the open delimiter for variables.
	 * @param close
	 *            the close delimiter for variables.
	 * @param recursion
	 *            the number of recursion (internal counter to avoid endless
	 *            loops)
	 * @return the string with the substitution applied.
	 */
	public static String substitute(String aString, Map<String, String> variablesValues, String open, String close,
			int recursion)
	{
		if (aString == null)
			return null;

		StringBuffer buffer = new StringBuffer();

		String rest = aString;

		// search for opening string
		int i = rest.indexOf(open);
		while (i > -1)
		{
			int j = rest.indexOf(close, i + open.length());
			// search for closing string
			if (j > -1)
			{
				String varName = rest.substring(i + open.length(), j);
				Object value = variablesValues.get(varName);
				if (value == null)
				{
					value = open + varName + close;
				} else
				{
					// check for another variable inside this value
					int another = ((String) value).indexOf(open); // check
					// here
					// first for
					// speed
					if (another > -1)
					{
						if (recursion > 50) // for safety: avoid recursive
						// endless loops with stack overflow
						{
							throw new RuntimeException("Endless loop detected for substitution of variable: "
									+ (String) value);
						}
						value = substitute((String) value, variablesValues, open, close, ++recursion);
					}
				}
				buffer.append(rest.substring(0, i));
				buffer.append(value);
				rest = rest.substring(j + close.length());
			} else
			{
				// no closing tag found; end the search
				buffer.append(rest);
				rest = "";
			}
			// keep searching
			i = rest.indexOf(open);
		}
		buffer.append(rest);
		return buffer.toString();
	}

	/**
	 * Substitutes hex values in <code>aString</code> and convert them to operating system char equivalents in the return string.
	 * Format is $[01] or $[6F,FF,00,1F] 
	 * Example: "This is a hex encoded six digits number 123456 in this string: $[31,32,33,34,35,36]"
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @return the string with the substitution applied.
	 */
	public static String substituteHex(String aString)
	{
		if (aString == null)
			return null;

		StringBuffer buffer = new StringBuffer();

		String rest = aString;

		// search for opening string
		int i = rest.indexOf(HEX_OPEN);
		while (i > -1)
		{
			int j = rest.indexOf(HEX_CLOSE, i + HEX_OPEN.length());
			// search for closing string
			if (j > -1)
			{
				buffer.append(rest.substring(0, i));
				String hexString = rest.substring(i + HEX_OPEN.length(), j);
				String[] hexStringArray=hexString.split(",");
				int hexInt;
				byte[] hexByte=new byte[1];
				for (int pos = 0; pos < hexStringArray.length; pos++) {
					try {
						hexInt = Integer.parseInt(hexStringArray[pos],16);
					} catch (NumberFormatException e) {
						hexInt=0; // in case we get an invalid hex value, ignore: we can not log here
					}
					hexByte[0]=(byte)hexInt;
					buffer.append(new String(hexByte));
				}
				rest = rest.substring(j + HEX_CLOSE.length());
			} else
			{
				// no closing tag found; end the search
				buffer.append(rest);
				rest = "";
			}
			// keep searching
			i = rest.indexOf(HEX_OPEN);
		}
		buffer.append(rest);
		return buffer.toString();
	}

	/**
	 * Substitutes variables in <code>aString</code> with the environment
	 * values in the system properties
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param systemProperties
	 *            the system properties to use
	 * @return the string with the substitution applied.
	 */
	public synchronized static final String environmentSubstitute(String aString, Map<String, String> systemProperties)
	{
		Map<String, String> sysMap = new HashMap<String, String>();
		synchronized(sysMap) {
			sysMap.putAll(systemProperties);
			
			aString = substituteWindows(aString, sysMap);
			aString = substituteUnix(aString, sysMap);
			aString = substituteHex(aString);
			return aString;
		}
	}

	/**
	 * Substitutes variables in <code>aString</code>. Variables are of the
	 * form "${<variable name>}", following the usin convention. The values are
	 * retrieved from the given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variables
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @return the string with the substitution applied.
	 */
	public static String substituteUnix(String aString, Map<String, String> variables)
	{
		return substitute(aString, variables, UNIX_OPEN, UNIX_CLOSE);
	}

	/**
	 * Substitutes variables in <code>aString</code>. Variables are of the
	 * form "%%<variable name>%%", following the windows convention. The values
	 * are retrieved from the given map.
	 * 
	 * @param aString
	 *            the string on which to apply the substitution.
	 * @param variables
	 *            a map containg the variable values. The keys are the variable
	 *            names, the values are the variable values.
	 * @return the string with the substitution applied.
	 */
	public static String substituteWindows(String aString, Map<String, String> variables)
	{
		return substitute(aString, variables, WINDOWS_OPEN, WINDOWS_CLOSE);
	}

	/**
	 * Search the string and report back on the variables used
	 * 
	 * @param aString
	 *            The string to search
	 * @param open
	 *            the open or "start of variable" characters ${ or %%
	 * @param close
	 *            the close or "end of variable" characters } or %%
	 * @param list
	 *            the list of variables to add to
	 * @param includeSystemVariables
	 *            also check for system variables.
	 */
	private static void getUsedVariables(String aString, String open, String close, List<String> list,
			boolean includeSystemVariables)
	{
		if (aString == null)
			return;

		int p = 0;
		while (p < aString.length())
		{
			// OK, we found something... : start of Unix variable
			if (aString.substring(p).startsWith(open))
			{
				// See if it's closed...
				int from = p + open.length();
				int to = aString.indexOf(close, from + 1);

				if (to >= 0)
				{
					String variable = aString.substring(from, to);

					if (Const.indexOfString(variable, list) < 0)
					{
						// Either we include the system variables (all)
						// Or the variable is not a system variable
						// Or it's a system variable but the value has not been set (and we offer the user the option to set it)
						//
						if (includeSystemVariables || !isSystemVariable(variable) || System.getProperty(variable)==null)
						{
							list.add(variable);
						}
					}
					// OK, continue
					p = to + close.length();
				}
			}
			p++;
		}
	}
	
	public static boolean isSystemVariable(String aString)
	{
		return Const.indexOfString(aString, SYSTEM_PROPERTIES)>=0; 
	}

	public static void getUsedVariables(String aString, List<String> list, boolean includeSystemVariables)
	{
		getUsedVariables(aString, UNIX_OPEN, UNIX_CLOSE, list, includeSystemVariables);
		getUsedVariables(aString, WINDOWS_OPEN, WINDOWS_CLOSE, list, includeSystemVariables);
	}

	public static final String generateRandomString(int length, String prefix, String postfix,
			boolean uppercase)
	{
		StringBuffer buffer = new StringBuffer();

		if (!Const.isEmpty(prefix))
			buffer.append(prefix);

		for (int i = 0; i < length; i++)
		{
			int c = 'a' + (int) (Math.random() * 26);
			buffer.append((char) c);
		}
		if (!Const.isEmpty(postfix))
			buffer.append(postfix);

		if (uppercase)
			return buffer.toString().toUpperCase();

		return buffer.toString();
	}

	public static String initCap(String st)
	{
		if (st == null || st.trim().length() == 0)
			return "";

		if ( st.substring(0,1).equals(st.substring(0,1).toUpperCase()))
		{
			// Already initially capitalized.
			return st;
		}
		else
		{
			// Capitalize first character
			return st.substring(0,1).toUpperCase() + st.substring(1); 
		}			
	}

	public static double str2num(String pattern, String decimal, String grouping, String currency,
			String value) throws KettleValueException
	{
		// 0 : pattern
		// 1 : Decimal separator
		// 2 : Grouping separator
		// 3 : Currency symbol

		NumberFormat nf = NumberFormat.getInstance();
		DecimalFormat df = (DecimalFormat) nf;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();

		if (!Const.isEmpty(pattern))
			df.applyPattern(pattern);
		if (!Const.isEmpty(decimal))
			dfs.setDecimalSeparator(decimal.charAt(0));
		if (!Const.isEmpty(grouping))
			dfs.setGroupingSeparator(grouping.charAt(0));
		if (!Const.isEmpty(currency))
			dfs.setCurrencySymbol(currency);
		try
		{
			df.setDecimalFormatSymbols(dfs);
			return df.parse(value).doubleValue();
		} catch (Exception e)
		{
			String message = "Couldn't convert string to number " + e.toString();
			if (!isEmpty(pattern))
				message += " pattern=" + pattern;
			if (!isEmpty(decimal))
				message += " decimal=" + decimal;
			if (!isEmpty(grouping))
				message += " grouping=" + grouping.charAt(0);
			if (!isEmpty(currency))
				message += " currency=" + currency;
			throw new KettleValueException(message);
		}
	}

	/**
	 * Check if the string supplied is empty. A String is empty when it is null
	 * or when the length is 0
	 * 
	 * @param string
	 *            The string to check
	 * @return true if the string supplied is empty
	 */
	public static final boolean isEmpty(String string)
	{
		return string == null || string.length() == 0;
	}

	/**
	 * Check if the stringBuffer supplied is empty. A StringBuffer is empty when
	 * it is null or when the length is 0
	 * 
	 * @param string
	 *            The stringBuffer to check
	 * @return true if the stringBuffer supplied is empty
	 */
	public static final boolean isEmpty(StringBuffer string)
	{
		return string == null || string.length() == 0;
	}

	public static Date str2dat(String arg0, String arg1, String val) throws KettleValueException
	{
		SimpleDateFormat df = new SimpleDateFormat();

		DateFormatSymbols dfs = new DateFormatSymbols();
		if (arg1 != null)
			dfs.setLocalPatternChars(arg1);
		if (arg0 != null)
			df.applyPattern(arg0);

		try
		{
			return df.parse(val);
		} catch (Exception e)
		{
			throw new KettleValueException("TO_DATE Couldn't convert String to Date " + e.toString());
		}
	}
  
    public static String getIndent(int indentLevel) {
        return INDENTCHARS.substring(0, indentLevel);
    }

	/**
	 * Giving back a date/time string in the format following the rule
	 * from the most to the least significant
	 * @param date the date to convert
	 * @return a string in the form yyyddMM_hhmmss
	 */
	public static String getFormattedDateTime(Date date)
	{
		return getFormattedDateTime(date, false);
	}
	
	/**
	 * Giving back a date/time string in the format following the rule
	 * from the most to the least significant
	 * @param date the date to convert
	 * @param milliseconds true when milliseconds should be added
	 * @return a string in the form yyyddMM_hhmmssSSS (milliseconds will be optional)
	 */
	public static String getFormattedDateTime(Date date, boolean milliseconds)
	{
		DateFormat dateFormat =null;
		if (milliseconds) {
			dateFormat = new SimpleDateFormat(Const.GENERALIZED_DATE_TIME_FORMAT_MILLIS);
		} else {
			dateFormat = new SimpleDateFormat(Const.GENERALIZED_DATE_TIME_FORMAT);
		}
		return dateFormat.format(date);
	}
	
	/**
	 * Giving back the actual time as a date/time string in the format following the rule
	 * from the most to the least significant
	 * @return a string in the form yyyddMM_hhmmss
	 */
	public static String getFormattedDateTimeNow()
	{
		return getFormattedDateTime(new Date(), false);
	}
	
	/**
	 * Giving back the actual time as a date/time string in the format following the rule
	 * from the most to the least significant
	 * @param milliseconds true when milliseconds should be added
	 * @return a string in the form yyyddMM_hhmmssSSS (milliseconds will be optional)
	 */
	public static String getFormattedDateTimeNow(boolean milliseconds)
	{
		return getFormattedDateTime(new Date(), milliseconds);
	}	
}