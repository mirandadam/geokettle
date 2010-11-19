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


package org.pentaho.di.core.exception;

import org.pentaho.di.core.Const;

/**
 * This is a general Kettle Exception.
 * 
 * @author Matt
 * @since 9-12-2004
 *
 */
public class KettleException extends Exception
{
    private static final long serialVersionUID = -2260895195255402040L;

    /**
	 * Constructs a new throwable with null as its detail message.
	 */
	public KettleException()
	{
		super();
	}

	/**
	 * Constructs a new throwable with the specified detail message.
	 * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
	 */
	public KettleException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
	 * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public KettleException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs a new throwable with the specified detail message and cause.
	 * @param message the detail message (which is saved for later retrieval by the getMessage() method).
	 * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public KettleException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * get the messages back to it's origin cause.
	 */
	public String getMessage()
	{
		String retval=Const.CR;
		retval+=super.getMessage()+Const.CR;

		Throwable cause = getCause();
		if (cause!=null)
		{
			String message = cause.getMessage();
			if (message!=null)
			{
				retval+=message+Const.CR;
			}
			else
			{
				// Add with stack trace elements of cause...
				StackTraceElement ste[] = cause.getStackTrace();
				for (int i=ste.length-1;i>=0;i--)
				{
					retval+="	at "+ste[i].getClassName()+"."+ste[i].getMethodName()+" ("+ste[i].getFileName()+":"+ste[i].getLineNumber()+")"+Const.CR;
				}
			}
		}
		
		return retval;
	}
    
    public String getSuperMessage()
    {
        return super.getMessage();
    }

}
