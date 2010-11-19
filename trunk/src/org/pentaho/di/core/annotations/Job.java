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
package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pentaho.di.job.JobEntryType;

/**
 * An alternative when defining Jobs. Classes annotated with "Job" are
 * automatically recognized and registered as a job.
 * 
 * Important: The XML definitions alienate annoated steps and the two methods of definition are therefore
 * mutually exclusive.
 * 
 * @author Alex Silva
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Job
{
	String id();
	
	String name() default "";

	String tooltip() default "";

	String image();

	JobEntryType type();
	
	String version() default "";
	
	int category() default -1;
	
	String categoryDescription() default "";
	
	String i18nPackageName() default "";
}
