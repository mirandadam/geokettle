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
 
package org.pentaho.di.compatibility;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of ValueBoolean.
 *
 * @author Sven Boden
 */
public class ValueBooleanTest extends TestCase
{
	/**
	 * Constructor test 1.
	 */
	public void testConstructor1()
	{
		ValueBoolean vs = new ValueBoolean();

		assertEquals(Value.VALUE_TYPE_BOOLEAN, vs.getType());
		assertEquals("Boolean", vs.getTypeDesc() );
		assertEquals(false, vs.getBoolean());
		assertEquals(-1, vs.getLength());
		assertEquals(-1, vs.getPrecision());

		ValueBoolean vs1 = new ValueBoolean(true);
		assertEquals(Value.VALUE_TYPE_BOOLEAN, vs1.getType());

		// Length and precision are ignored
		vs1.setLength(2);
		assertEquals(-1, vs1.getLength());
		assertEquals(-1, vs1.getPrecision());

		vs1.setLength(2, 2);
		assertEquals(-1, vs1.getLength());
		assertEquals(-1, vs1.getPrecision());

		vs1.setPrecision(2);
		assertEquals(-1, vs1.getLength());
		assertEquals(-1, vs1.getPrecision());		
	}

	/**
	 * Test the getters of ValueBoolean
	 */
	public void testGetters()
	{
		ValueBoolean vs1 = new ValueBoolean(true);
		ValueBoolean vs2 = new ValueBoolean(false);

		assertEquals(true, vs1.getBoolean());
		assertEquals(false, vs2.getBoolean());

		assertEquals("Y", vs1.getString());
		assertEquals("N", vs2.getString());
		
		assertEquals(1.0D, vs1.getNumber(), 0.001D);
		assertEquals(0.0D, vs2.getNumber(), 0.001D);

		assertEquals(1L, vs1.getInteger());
		assertEquals(0L, vs2.getInteger());

		assertEquals(new BigDecimal(1), vs1.getBigNumber());
		assertEquals(new BigDecimal(0), vs2.getBigNumber());
		
		assertNull(vs1.getDate());
		assertNull(vs2.getDate());
		
		assertEquals(new Boolean(true), vs1.getSerializable());
		assertEquals(new Boolean(false), vs2.getSerializable());		
	}

	/**
	 * Test the setters of ValueBoolean
	 */
	public void testSetters()
	{
		ValueBoolean vs = new ValueBoolean(true);
		
		vs.setString("unknown");
		assertEquals(false, vs.getBoolean());
		vs.setString("y");
		assertEquals(true, vs.getBoolean());
		vs.setString("Y");
		assertEquals(true, vs.getBoolean());
		vs.setString("yes");
		assertEquals(true, vs.getBoolean());
		vs.setString("YES");
		assertEquals(true, vs.getBoolean());
		vs.setString("true");
		assertEquals(true, vs.getBoolean());
		vs.setString("TRUE");
		assertEquals(true, vs.getBoolean());
		vs.setString("false");
		assertEquals(false, vs.getBoolean());

		vs.setDate(new Date());
		assertEquals(false, vs.getBoolean());

		vs.setBoolean(true);
		assertEquals(true, vs.getBoolean());
		vs.setBoolean(false);
		assertEquals(false, vs.getBoolean());

		vs.setNumber(5.0D);
		assertEquals(true, vs.getBoolean());
		vs.setNumber(0.0D);
		assertEquals(false, vs.getBoolean());
		
		vs.setInteger(5L);
		assertEquals(true, vs.getBoolean());
		vs.setInteger(0L);
		assertEquals(false, vs.getBoolean());

		vs.setBigNumber(new BigDecimal(5));
		assertEquals(true, vs.getBoolean());
		vs.setBigNumber(new BigDecimal(0));
		assertEquals(false, vs.getBoolean());
				
		// setSerializable is ignored ???
	}

	/**
	 * Test clone()
	 */
	public void testClone()
	{
		ValueBoolean vs1 = new ValueBoolean(true);
		
		ValueBoolean cloneVs1 = (ValueBoolean)vs1.clone();
		assertTrue(cloneVs1.getBoolean() == vs1.getBoolean()); 
		assertFalse(cloneVs1 == vs1);
		
		ValueBoolean vs2 = new ValueBoolean(false);
		
		ValueBoolean cloneVs2 = (ValueBoolean)vs2.clone();
		assertTrue(cloneVs2.getBoolean() == vs2.getBoolean()); 
		assertFalse(cloneVs2 == vs2);		
	}
}