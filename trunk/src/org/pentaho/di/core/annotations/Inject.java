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
/**
 * Allows for properties to be dynamically injected into classes during runtime.  
 * 
 * Both methods and fields can be annotated.
 * 
 * @author Alex Silva
 *
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject
{
	/**
	 * The name of the property to be injected.
	 * Default values:
	 * <ol><li>
	 * <u>Fields:</u> The name of the annotated field.  For instance, annotating a field named "callerId" with @Inject has the same effect as
	 * annotating this field with @Inject("callerId")</li>
	 * <li><u>Methods:</u>The name of the property being set by the method, as defined by <code>Introspector.decapitalize</code> and 
	 * the Java Beans API.</li>
	 * </ol>
	 * @return the property name to be injected
	 */
	String property() default "";
}
