/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
package org.pentaho.di.trans.steps.creditcardvalidator;

import org.pentaho.di.core.Const;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class CreditCardVerifier
{
	public static final int INVALID          = -1;  
	public static final int VISA             = 0;
	public static final int MASTERCARD       = 1;
	public static final int AMERICAN_EXPRESS = 2;
	public static final int EN_ROUTE         = 3;
	public static final int DINERS_CLUB      = 4;

	  private static final String [] cardNames = 
      {   "Visa" , 
          "Mastercard", 
          "American Express", 
          "En Route", 
          "Diner's CLub/Carte Blanche",
      };
	  private static final String[] NotValidCardNames = {
	        Messages.getString("CreditCardValidator.Log.NotValidVisa"),
	        Messages.getString("CreditCardValidator.Log.NotValidMastercard"),
	        Messages.getString("CreditCardValidator.Log.NotValidAmericanExpress"),
	        Messages.getString("CreditCardValidator.Log.NotValidEnRoute"),
	        Messages.getString("CreditCardValidator.Log.NotValidDiners"),
	    };
	  public static String getCardName(int id) {
		    return (id > -1 && id < cardNames.length ? cardNames[id] : null);
		    }

	public static String getNotValidCardNames(int id) {
		    return (id > -1 && id < NotValidCardNames.length ? NotValidCardNames[id] : null);
		    }
	public  static ReturnIndicator CheckCC(String CardNumber)
	{
		ReturnIndicator ri = new ReturnIndicator();
 
		if(Const.isEmpty(CardNumber)) {
			ri.UnValidMsg=Messages.getString("CreditCardValidator.Log.EmptyNumber");
			return ri;
		}
		
		Matcher m = Pattern.compile("[^\\d\\s.-]").matcher(CardNumber);
		if (m.find()) {
			 ri.UnValidMsg="Credit card number can only contain numbers, spaces, \"-\", and \".\"";
			 return ri;
	      }
		 
		 int cardId=getCardID(CardNumber);
	     if (cardId > -1) {
	    	if(luhnValidate(CardNumber)) {
	    		ri.CardValid=true;
	    		ri.CardType=getCardName(cardId);
	    	}else {
	    		ri.CardValid=false;
	    		ri.UnValidMsg=getNotValidCardNames(cardId);
	    	}
	    }else {
	    	// try luhn
	    	//ri.UnValidMsg="This card is unsupported!";
	    	if(luhnValidate(CardNumber)) {
	    		ri.CardValid=true;
	    	}else {
	    		ri.UnValidMsg=Messages.getString("CreditCardValidator.Log.CardNotValid");
	    	}
	    }
		 
		 return ri;
	}
	 // The Luhn algorithm is basically a CRC type
    // system for checking the validity of an entry.
    // All major credit cards use numbers that will
    // pass the Luhn check. Also, all of them are based
    // on MOD 10.
	
    public static boolean luhnValidate(String numberString) {
    	try {

	      int j = numberString.length();
	  
	      String [] s1 = new String[j];
	      for (int i=0; i < j; i++) s1[i] = "" + numberString.charAt(i);
	  
	      int checksum = 0;         
	    
	      for (int i=s1.length-1; i >= 0; i-= 2) {
	        int k = 0;

	        if (i > 0) {
	           k = Integer.valueOf(s1[i-1]).intValue() * 2;
	           if (k > 9) {
	              String s = "" + k;
	              k = Integer.valueOf(s.substring(0,1)).intValue() + 
	                  Integer.valueOf(s.substring(1)).intValue();
	              }
	              checksum += Integer.valueOf(s1[i]).intValue() + k;            
	           }
	           else
	              checksum += Integer.valueOf(s1[0]).intValue();            
	        }
	      return ((checksum % 10) == 0);
	      }
	    catch (Exception e) {
	      //e.printStackTrace();
	      return false;
	    }
    }
    
	public static int getCardID(String number) {
	    int valid = INVALID;
	        
	    String digit1 = number.substring(0,1);
	    String digit2 = number.substring(0,2);
	    String digit3 = number.substring(0,3);
	    String digit4 = number.substring(0,4);
	    
	    if (isNumber(number)) {
	      /* ----
	      ** VISA  prefix=4
	      ** ----  length=13 or 16  (can be 15 too!?! maybe)
	      */
	      if (digit1.equals("4"))  {  
	        if (number.length() == 13 || number.length() == 16) 
	           valid = VISA;
	        }
	      /* ----------
	      ** MASTERCARD  prefix= 51 ... 55
	      ** ----------  length= 16
	      */
	      else if (digit2.compareTo("51")>=0 && digit2.compareTo("55")<=0) {
	        if (number.length() == 16) 
	           valid = MASTERCARD;
	        }  
            
	      /* ----
	      ** AMEX  prefix=34 or 37
	      ** ----  length=15
	      */
	      else if (digit2.equals("34") || digit2.equals("37")) {
	        if (number.length() == 15) 
	           valid = AMERICAN_EXPRESS;
	        }
	      /* -----
	      ** ENROU prefix=2014 or 2149
	      ** ----- length=15
	      */
	      else if (digit4.equals("2014") || digit4.equals("2149")) {
	         if (number.length() == 15) 
	            valid = EN_ROUTE;
	         }
	      /* -----
	      ** DCLUB prefix=300 ... 305 or 36 or 38
	      ** ----- length=14
	      */
	      else if (digit2.equals("36") || digit2.equals("38") || 
	        (digit3.compareTo("300")>=0 && digit3.compareTo("305")<=0)) {
	        if (number.length() == 14) 
	           valid = DINERS_CLUB;
	           }      
	      
	      /* ----
	      ** DISCOVER card prefix = 60
	      ** --------      lenght = 16
	      */
	      else if (digit2.equals("60")) {
	  	        if (number.length() == 16) 
	  	           valid = DINERS_CLUB;
	  	           }
	  	      } 
	    
	 	return valid;
	 }
	    
	  public static boolean isNumber(String n) {
	    try  {
	      Double.valueOf(n).doubleValue();
	      return true;
	      }
	    catch (NumberFormatException e) { // ignore this exception
	      return false;
	      }
	    }
}
