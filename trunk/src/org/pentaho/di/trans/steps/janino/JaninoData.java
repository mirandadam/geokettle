 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

package org.pentaho.di.trans.steps.janino;

import org.codehaus.janino.ExpressionEvaluator;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 8-sep-2005
 *
 */
public class JaninoData extends BaseStepData implements StepDataInterface
{
	public static final int RETURN_TYPE_STRING = 0;
	public static final int RETURN_TYPE_NUMBER = 1;
	public static final int RETURN_TYPE_INTEGER = 2;
	public static final int RETURN_TYPE_LONG = 3;
	public static final int RETURN_TYPE_DATE = 4;
	public static final int RETURN_TYPE_BIGDECIMAL = 5;
	public static final int RETURN_TYPE_BYTE_ARRAY = 6;
	public static final int RETURN_TYPE_BOOLEAN = 7;
	
	
    public RowMetaInterface outputRowMeta;
	public int[] returnType;
	public int[] replaceIndex;

	public ExpressionEvaluator[] expressionEvaluators;
	
	/**
	 * 
	 */
	public JaninoData()
	{
		super();
	}

}
