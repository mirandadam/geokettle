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

package org.pentaho.di.trans.steps.mailvalidator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;

public class MailValidation {

	private static LogWriter log = LogWriter.getInstance();

	public static boolean isRegExValid(String emailAdress) {
		//Set the email pattern string
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");

		//Match the given string with the pattern
		Matcher m = p.matcher(emailAdress);

		//check whether match is found 
		return (m.matches());
	}

	/** 
	 * verify if there is a mail server registered to the domain name.
	 * and return the email servers count  
	 */
	public static int mailServersCount(String hostName) throws NamingException {
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[] {
			"MX"
		});
		Attribute attr = attrs.get("MX");
		if (attr == null)
			return (0);
		return (attr.size());
	}

	private static String className() {
		return Messages.getString("MailValidator.ClassName");
	}

	private static int hear(BufferedReader in) throws IOException {
		String line = null;
		int res = 0;

		while ((line = in.readLine()) != null) {
			String pfx = line.substring(0, 3);
			try {
				res = Integer.parseInt(pfx);
			} catch (Exception ex) {
				res = -1;
			}
			if (line.charAt(3) != '-')
				break;
		}

		return res;
	}

	private static void say(BufferedWriter wr, String text) throws IOException {
		wr.write(text + "\r\n");
		wr.flush();

		return;
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<String> getMX(String hostName) throws NamingException {
		// Perform a DNS lookup for MX records in the domain
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx = new InitialDirContext(env);
		Attributes attrs = ictx.getAttributes(hostName, new String[] {
			"MX"
		});
		Attribute attr = attrs.get("MX");

		// if we don't have an MX record, try the machine itself
		if ((attr == null) || (attr.size() == 0)) {
			attrs = ictx.getAttributes(hostName, new String[] {
				"A"
			});
			attr = attrs.get("A");
			if (attr == null)
				throw new NamingException(Messages.getString("MailValidator.NoMatchName", hostName));
		}

		// Huzzah! we have machines to try. Return them as an array list
		// NOTE: We SHOULD take the preference into account to be absolutely
		//   correct. This is left as an exercise for anyone who cares.
		ArrayList<String> res = new ArrayList<String>();
		NamingEnumeration en = attr.getAll();

		while (en.hasMore()) {
			String x = (String) en.next();
			String f[] = x.split(" ");
			if (f[1].endsWith("."))
				f[1] = f[1].substring(0, (f[1].length() - 1));
			res.add(f[1]);
		}
		return res;
	}

	/** Validate an email address
	 * This code is from : http://www.rgagnon.com/javadetails/java-0452.html
	 * @param email address
	 * @param sender email address
	 * @param default SMTP Server
	 * @param timeout for socket connection
	 * @param deepCheck (if we want to perform a SMTP check
	 * @return true or false
	 */
	public static MailValidationResult isAddressValid(String address, String senderAddress, 
			String defaultSMTPServer, int timeout, boolean deepCheck) {

		MailValidationResult result = new MailValidationResult();

		if (!isRegExValid(address)) {
			result.setErrorMessage(Messages.getString("MailValidator.MalformedAddress", address));
			return result;
		}

		// Find the separator for the domain name
		int pos = address.indexOf('@');

		// If the address does not contain an '@', it's not valid
		if (pos == -1)
			return result;

		if (!deepCheck) {
			result.setValide(true);
			return result;
		}

		// Isolate the domain/machine name and get a list of mail exchangers
		String domain = address.substring(++pos);

		// Maybe user want to switch to a default SMTP server?
		// In that case, we will ignore the domain
		// extracted from email address

		ArrayList<String> mxList = new ArrayList<String>();
		if (Const.isEmpty(defaultSMTPServer)) {
			try {
				mxList = getMX(domain);

				// Just because we can send mail to the domain, doesn't mean that the
				// address is valid, but if we can't, it's a sure sign that it isn't
				if (mxList == null || mxList.size() == 0) {
					result.setErrorMessage(Messages.getString("MailValidator.NoMachinesInDomain", domain));
					return result;
				}
			} catch (Exception ex) {
				result.setErrorMessage(Messages.getString("MailValidator.ErrorGettingMachinesInDomain", ex.getMessage()));
				return result;
			}
		} else {
			mxList.add(defaultSMTPServer);
		}

		if (log.isDebug())
			log.logDebug(className(), Messages.getString("MailValidator.ExchangersFound", "" + mxList.size()));

		// Now, do the SMTP validation, try each mail exchanger until we get
		// a positive acceptance. It *MAY* be possible for one MX to allow
		// a message [store and forwarder for example] and another [like
		// the actual mail server] to reject it. This is why we REALLY ought
		// to take the preference into account.
		for (int mx = 0; mx < mxList.size(); mx++) {
			boolean valid = false;
			BufferedReader rdr = null;
			BufferedWriter wtr = null;
			Socket skt = null;
			try {
				String exhanger = (String) mxList.get(mx);
				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.TryingExchanger", exhanger));

				int res;

				skt = new Socket(exhanger, 25);
				// set timeout (milliseconds)
				if (timeout > 0)
					skt.setSoTimeout(timeout);

				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.ConnectingTo", exhanger, "25", skt.isConnected() + ""));

				rdr = new BufferedReader(new InputStreamReader(skt.getInputStream()));
				wtr = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));

				res = hear(rdr);
				if (res != 220)
					throw new Exception(Messages.getString("MailValidator.InvalidHeader"));

				// say HELLO it's me
				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.SayHello", domain));
				say(wtr, "EHLO " + domain);
				res = hear(rdr);
				if (res != 250)
					throw new Exception("Not ESMTP");
				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.ServerReplied", "" + res));

				// validate the sender address   
				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.CheckSender", senderAddress));
				say(wtr, "MAIL FROM: <" + senderAddress + ">");
				res = hear(rdr);
				if (res != 250)
					throw new Exception(Messages.getString("MailValidator.SenderRejected"));
				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.SenderAccepted", "" + res));

				// Validate receiver
				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.CheckReceiver", address));
				say(wtr, "RCPT TO: <" + address + ">");
				res = hear(rdr);

				// be polite
				say(wtr, "RSET");
				hear(rdr);
				say(wtr, "QUIT");
				hear(rdr);
				if (res != 250)
					throw new Exception(Messages.getString("MailValidator.AddressNotValid", address));

				if (log.isDebug())
					log.logDebug(className(), Messages.getString("MailValidator.ReceiverAccepted", address, "" + res));
				valid = true;

			} catch (Exception ex) {
				// Do nothing but try next host
				result.setValide(false);
				result.setErrorMessage(ex.getMessage());
			} finally {
				if (rdr != null)
					try {
						rdr.close();
					} catch (Exception e) {
					}
				;// ignore this
				if (wtr != null)
					try {
						wtr.close();
					} catch (Exception e) {
					}
				;// ignore this
				if (skt != null)
					try {
						skt.close();
					} catch (Exception e) {
					}
				; // ignore this

				if (valid) {
					result.setValide(true);
					result.setErrorMessage(null);
					if (log.isDebug())
						log.logDebug(className(), "=============================================");
					return result;
				}
			}
		}
		if (log.isDebug())
			log.logDebug(className(), "=============================================");

		return result;
	}

}
