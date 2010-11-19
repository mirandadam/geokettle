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

package org.pentaho.di.trans.steps.rssoutput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.Element;
import java.util.Date;
import org.apache.commons.vfs.FileObject;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.feed.synd.SyndImageImpl;
import com.sun.syndication.feed.module.georss.SimpleModuleImpl;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.W3CGeoModuleImpl;
import com.sun.syndication.feed.module.georss.geometries.Position;

import java.io.FileWriter;
import java.io.Writer;
import java.io.File;


/**
 * Output rows to RSS feed and create a file.
 * 
 * @author Samatar
 * @since 6-nov-2007
 */
public class RssOutput extends BaseStep implements StepInterface
{
	private RssOutputMeta meta;
	private RssOutputData data;
	

	public RssOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(RssOutputMeta)smi;
		data=(RssOutputData)sdi;
		
		
		Object[] r=getRow();    // this also waits for a previous step to be finished.

		if (r==null)  // no more input to be expected...
		{
			if(!first)
			{
				if(!meta.isCustomRss())
				{
					// No more input..so write and close the file.
					WriteToFile(data.channeltitlevalue, data.channellinkvalue, data.channeldescriptionvalue, data.channelpubdatevalue,
							data.channelcopyrightvalue,data.channelimagelinkvalue, data.channelimagedescriptionvalue,
							data.channelimagelinkvalue,data.channelimageurlvalue,data.channellanguagevalue,data.channelauthorvalue);
				}else{

					// Write to document
					OutputFormat format = org.dom4j.io.OutputFormat.createPrettyPrint();
		            // Set encoding ...			 
		            if(Const.isEmpty(meta.getEncoding())) 
		            	format.setEncoding("iso-8859-1");
		            else
		            	format.setEncoding(meta.getEncoding());
					
					try{
						XMLWriter writer = new XMLWriter(new FileWriter(new File(data.filename)),format);
						writer.write(data.document);
						writer.close();
					}catch(Exception e){}
					finally{data.document=null;}


				}
			}
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
			data.inputRowMeta = getInputRowMeta();
            data.outputRowMeta = data.inputRowMeta.clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			// Let's check for filename...
			
			if(meta.isFilenameInField())
			{
				if(Const.isEmpty(meta.getFileNameField()))
				{
					logError(Messages.getString("RssOutput.Log.FilenameFieldMissing"));
					setErrors(1);
					stopAll();
					return false;	
				}
				
				// get filename field index
				data.indexOfFieldfilename =data.inputRowMeta.indexOfValue(meta.getFileNameField());
				if (data.indexOfFieldfilename<0)
				{
					// The field is unreachable !
					logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getFileNameField()));
					throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getFileNameField()));
				}
				
			}
			else
			{
				data.filename=buildFilename();
			}
			
			// Check if filename is empty..
			if(Const.isEmpty(data.filename))
			{
				logError(Messages.getString("RssOutput.Log.FilenameEmpty"));
				throw new KettleException(Messages.getString("RssOutput.Log.FilenameEmpty"));	
			}
			
			// Do we need to create parent folder ?
			if(meta.isCreateParentFolder())
			{
				// Check for parent folder
				FileObject parentfolder=null;
	    		try
	    		{
	    			// Get parent folder
		    		parentfolder=KettleVFS.getFileObject(data.filename).getParent();	    		
		    		if(!parentfolder.exists())	
		    		{
		    			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("RssOutput.Log.ParentFolderExists",parentfolder.getName().toString()));
		    			parentfolder.createFolder();
		    			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("RssOutput.Log.CanNotCreateParentFolder",parentfolder.getName().toString()));
		    		}
	    		}
	    		catch (Exception e) {					
					// The field is unreachable !
					logError(Messages.getString("RssOutput.Log.CanNotCreateParentFolder", parentfolder.getName().toString()));
					throw new KettleException(Messages.getString("RssOutput.Log.CanNotCreateParentFolder",parentfolder.getName().toString()));
					
	    		}
	    		 finally {
	             	if ( parentfolder != null )
	             	{
	             		try  {
	             			parentfolder.close();
	             		}
	             		catch ( Exception ex ) {};
	             	}
	             }		
			}		
			
			if(!meta.isCustomRss())
			{
				// Let's check for mandatory fields ...
				if(Const.isEmpty(meta.getChannelTitle()))
				{
					logError(Messages.getString("RssOutput.Log.ChannelTitleMissing"));
					setErrors(1);
					stopAll();
					return false;	
				}
				if(Const.isEmpty(meta.getChannelDescription()))
				{
					logError(Messages.getString("RssOutput.Log.ChannelDescription"));
					setErrors(1);
					stopAll();
					return false;	
				}
				if(Const.isEmpty(meta.getChannelLink()))
				{
					logError(Messages.getString("RssOutput.Log.ChannelLink"));
					setErrors(1);
					stopAll();
					return false;
				}
				
				// Let's take the index of channel title field ...
				data.indexOfFieldchanneltitle =data.inputRowMeta.indexOfValue(meta.getChannelTitle());
				if (data.indexOfFieldchanneltitle<0)
				{
					// The field is unreachable !
					logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelTitle()));
					throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelTitle()));
				}
				data.channeltitlevalue= data.inputRowMeta.getString(r,data.indexOfFieldchanneltitle);
				
				// Let's take the index of channel description field ...	
				data.indexOfFieldchanneldescription =data.inputRowMeta.indexOfValue(meta.getChannelDescription());
				if (data.indexOfFieldchanneldescription<0)
				{
					// The field is unreachable !
					logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelDescription()));
					throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelDescription()));
				}
				
				data.channeldescriptionvalue= data.inputRowMeta.getString(r,data.indexOfFieldchanneldescription);
				
				// Let's take the index of channel link field ...	
				data.indexOfFieldchannellink =data.inputRowMeta.indexOfValue(meta.getChannelLink());
				if (data.indexOfFieldchannellink<0)
				{
					// The field is unreachable !
					logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelLink()));
					throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelLink()));
				}
			
				data.channellinkvalue= data.inputRowMeta.getString(r,data.indexOfFieldchannellink);
				
				if(!Const.isEmpty( meta.getItemTitle()))
				{
					// Let's take the index of item title field ...	
					data.indexOfFielditemtitle =data.inputRowMeta.indexOfValue(meta.getItemTitle());
					if (data.indexOfFielditemtitle<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getItemTitle()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getItemTitle()));
					}
				}
				
				if(!Const.isEmpty( meta.getItemDescription()))
				{
					// Let's take the index of item description field ...
					data.indexOfFielditemdescription =data.inputRowMeta.indexOfValue(meta.getItemDescription());
					if (data.indexOfFielditemdescription<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getItemDescription()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getItemDescription()));
					}
				}
				if(meta.AddGeoRSS())
				{
					if(Const.isEmpty( meta.getGeoPointLong()))
						throw new KettleException(Messages.getString("RssOutput.Log.GeoPointLatEmpty"));
					if(Const.isEmpty( meta.getGeoPointLong()))
						throw new KettleException(Messages.getString("RssOutput.Log.GeoPointLongEmpty"));	
						
					
					// Let's take the index of item geopointX field ...
					data.indexOfFielditempointx =data.inputRowMeta.indexOfValue(meta.getGeoPointLat());
					if (data.indexOfFielditempointx<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getGeoPointLat()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getGeoPointLat()));
					}
					// Let's take the index of item geopointY field ...
					data.indexOfFielditempointy =data.inputRowMeta.indexOfValue(meta.getGeoPointLong());
					if (data.indexOfFielditempointy<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getGeoPointLong()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getGeoPointLong()));
					}
				}
				
				//It's time to check non empty fields !
				// Channel PubDate field ...
				if(!Const.isEmpty(meta.getChannelPubDate()))
				{
					data.indexOfFieldchannelpubdate =data.inputRowMeta.indexOfValue(meta.getChannelPubDate());
					if (data.indexOfFieldchannelpubdate<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelPubDate()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelPubDate()));
					}
					
					data.channelpubdatevalue= data.inputRowMeta.getDate(r,data.indexOfFieldchannelpubdate);
				}
				// Channel Language field ...
				if(!Const.isEmpty(meta.getChannelLanguage()))
				{
					data.indexOfFieldchannellanguage =data.inputRowMeta.indexOfValue(meta.getChannelLanguage());
					if (data.indexOfFieldchannellanguage<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelLanguage()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelLanguage()));
					}
		
					data.channellanguagevalue= data.inputRowMeta.getString(r,data.indexOfFieldchannellanguage);
				}
				
				// Channel Copyright field ...
				if(!Const.isEmpty(meta.getChannelCopyright()))
				{
					data.indexOfFieldchannelcopyright =data.inputRowMeta.indexOfValue(meta.getChannelCopyright());
					if (data.indexOfFieldchannelcopyright<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelCopyright()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelCopyright()));
					}
					
					data.channelcopyrightvalue= data.inputRowMeta.getString(r,data.indexOfFieldchannelcopyright);
				}
				
				// Channel Author field ...
				if(!Const.isEmpty(meta.getChannelAuthor()))
				{	
					data.indexOfFieldchannelauthor =data.inputRowMeta.indexOfValue(meta.getChannelAuthor());
					if (data.indexOfFieldchannelauthor<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelAuthor()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelAuthor()));
					}
			
					data.channelauthorvalue= data.inputRowMeta.getString(r,data.indexOfFieldchannelauthor);
				}
				
				// Channel Image field ...
				if(meta.AddImage())
				{
					// Channel image title
					if(!Const.isEmpty(meta.getChannelImageTitle()))
					{
						data.indexOfFieldchannelimagetitle =data.inputRowMeta.indexOfValue(meta.getChannelImageTitle());
						if (data.indexOfFieldchannelimagetitle<0)
						{
							// The field is unreachable !
							logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelImageTitle()));
							throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelImageTitle()));
						}
						
						data.channelimagetitlevalue= data.inputRowMeta.getString(r,data.indexOfFieldchannelimagetitle);
					}
					
					// Channel link title
					if(!Const.isEmpty(meta.getChannelImageLink()))
					{
						data.indexOfFieldchannelimagelink =data.inputRowMeta.indexOfValue(meta.getChannelImageLink());
						if (data.indexOfFieldchannelimagelink<0)
						{
							// The field is unreachable !
							logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelImageLink()));
							throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelImageLink()));
						}
					
						data.channelimagelinkvalue= data.inputRowMeta.getString(r,data.indexOfFieldchannelimagelink);
					}
					
					// Channel url title
					if(!Const.isEmpty(meta.getChannelImageUrl()))
					{
						data.indexOfFieldchannelimageurl =data.inputRowMeta.indexOfValue(meta.getChannelImageUrl());
						if (data.indexOfFieldchannelimageurl<0)
						{
							// The field is unreachable !
							logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelImageUrl()));
							throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelImageUrl()));
						}
					
						data.channelimageurlvalue= data.inputRowMeta.getString(r,data.indexOfFieldchannelimageurl);
					}
					
					// Channel description title
					if(!Const.isEmpty(meta.getChannelImageDescription()))
					{	
						data.indexOfFieldchannelimagedescription =data.inputRowMeta.indexOfValue(meta.getChannelImageDescription());
						if (data.indexOfFieldchannelimagedescription<0)
						{
							// The field is unreachable !
							logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getChannelImageDescription()));
							throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getChannelImageDescription()));
						}

						data.channelimagedescriptionvalue= data.inputRowMeta.getString(r,data.indexOfFieldchannelimagedescription);
					}
					
					
				}
				
				// Item link field ...
				if(!Const.isEmpty(meta.getItemLink()))
				{
					data.indexOfFielditemlink =data.inputRowMeta.indexOfValue(meta.getItemLink());
					if (data.indexOfFielditemlink<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getItemLink()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getItemLink()));
					}
	
				}
				
				// Item pubdate field ...
				if(!Const.isEmpty(meta.getItemPubDate()))
				{	
					data.indexOfFielditempubdate =data.inputRowMeta.indexOfValue(meta.getItemPubDate());
					if (data.indexOfFielditempubdate<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getItemPubDate()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getItemPubDate()));
					}
				}
				
				
				// Item author field ...
				if(!Const.isEmpty(meta.getItemAuthor()))
				{	
					data.indexOfFielditemauthor =data.inputRowMeta.indexOfValue(meta.getItemAuthor());
					if (data.indexOfFielditemauthor<0)
					{
						// The field is unreachable !
						logError(Messages.getString("RssOutput.Log.ErrorFindingField", meta.getItemAuthor()));
						throw new KettleException(Messages.getString("RssOutput.Log.ErrorFindingField",meta.getItemAuthor()));
					}
				}
			}else
			{
				// Custom RSS
				// Check Custom channel fields
				data.customchannels= new int[meta.getChannelCustomFields().length];
				for (int i=0;i<meta.getChannelCustomFields().length;i++)
				{
					data.customchannels[i] =data.inputRowMeta.indexOfValue(meta.getChannelCustomFields()[i]);
					if (data.customchannels[i]<0) // couldn't find field!
					{
						throw new KettleStepException(Messages.getString("RssOutput.Exception.FieldRequired",meta.getChannelCustomFields()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				// Check Custom channel fields
				data.customitems= new int[meta.getItemCustomFields().length];
				for (int i=0;i<meta.getItemCustomFields().length;i++)
				{
					data.customitems[i] =data.inputRowMeta.indexOfValue(meta.getItemCustomFields()[i]);
					if (data.customitems[i]<0)  // couldn't find field!
					{
						throw new KettleStepException(Messages.getString("RssOutput.Exception.FieldRequired",meta.getItemCustomFields()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				// Prepare Output RSS Custom document
				data.document = DocumentHelper.createDocument();
				data.rssElement = data.document.addElement("rss");
				data.rssElement.addAttribute("version", "2.0");
				// add namespaces here ...
				for(int i=0;i<meta.getNameSpaces().length;i++)
				{
					data.rssElement.addAttribute(environmentSubstitute(meta.getNameSpacesTitle()[i]),
							environmentSubstitute(meta.getNameSpaces()[i]));
				}

				// Add channel
				data.channel = data.rssElement.addElement("channel");
				
				// Set channel Only the first time ...
				for(int i=0;i<data.customchannels.length;i++)
				{
					String channelname=environmentSubstitute(meta.getChannelCustomTags()[i]);
					String channelvalue= data.inputRowMeta.getString(r,data.customchannels[i]);
					
					if(log.isDetailed())
						log.logDetailed(toString(),"outputting channel value <"+channelname+">"+channelvalue+"<"+channelname+"/>");
					
					// add Channel
					Element channeltag = data.channel.addElement(channelname);
					channeltag.setText(channelvalue);
					
				}
			}	
		}  // end test first time

		// Let's get value for each item...
		if(!meta.isCustomRss())
		{
			String itemtitlevalue=  null;
			String itemauthorvalue=null;
			String itemlinkvalue=null;
			Date itemdatevalue=null;
			String itemdescvalue=null;
			String itemgeopointx=null;
			String itemgeopointy=null;
			
			if(data.indexOfFielditemtitle>-1) 	     itemtitlevalue= data.inputRowMeta.getString(r,data.indexOfFielditemtitle);
			if(data.indexOfFielditemauthor>-1) 		 itemauthorvalue= data.inputRowMeta.getString(r,data.indexOfFielditemauthor);
			if(data.indexOfFielditemlink>-1)  		 itemlinkvalue= data.inputRowMeta.getString(r,data.indexOfFielditemlink);
			if(data.indexOfFielditempubdate>-1) 	 itemdatevalue= data.inputRowMeta.getDate(r,data.indexOfFielditempubdate);
			if(data.indexOfFielditemdescription>-1)  itemdescvalue= data.inputRowMeta.getString(r,data.indexOfFielditemdescription);
			if(data.indexOfFielditempointx>-1) 		 itemgeopointx= data.inputRowMeta.getString(r,data.indexOfFielditempointx);
			if(data.indexOfFielditempointy>-1) 		 itemgeopointy= data.inputRowMeta.getString(r,data.indexOfFielditempointy);
			
			
			// Now add entry ..
			if(!createEntry(itemauthorvalue, itemtitlevalue, itemlinkvalue, itemdatevalue, itemdescvalue,itemgeopointx,itemgeopointy))
				throw new KettleException("Error adding item to feed");
		}else{
			
			// Set item tag at each row received 
			if(meta.isDisplayItem())
			{
				data.itemtag = data.channel.addElement("item");
			}
			for(int i=0;i<data.customitems.length;i++)
			{
				// get item value and name
				String itemname=environmentSubstitute(meta.getItemCustomTags()[i]);
				String itemvalue= data.inputRowMeta.getString(r,data.customitems[i]);
				
				if(log.isDetailed())
					log.logDetailed(toString(),"outputting item value <"+itemname+">"+itemvalue+"<"+itemname+"/>");
				
				// add Item
				if(meta.isDisplayItem())
				{
					Element itemtagsub = data.itemtag.addElement(itemname);
					itemtagsub.setText(itemvalue);
				}else{
					// display item at channel level
					data.channel.addElement(itemname);
					data.channel.setText(itemvalue);
				}
			}
		}

        try
        {
    		putRow(data.outputRowMeta, r);       // in case we want it to go further...
    		incrementLinesOutput();
	        
	        if (checkFeedback(getLinesOutput())) 
	        {
	        	if(log.isDebug()) logDebug(Messages.getString("RssOutput.Log.Linenr",""+getLinesOutput()));
	        }
			
		}
		catch(KettleStepException e)
		{
			logError(Messages.getString("RssOutputMeta.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}	
		
		return true;
	}
	public String buildFilename()
	{
		return meta.buildFilename(this,getCopy());
	}
	 /**
     * @param author :
     *          The author of the event
     * @param title :
     *          The title of the event
     * @param link :
     *          The link to the element in RES
     * @param date :
     *          The event's date
     * @param desc :
     *          The event's description
     */
   @SuppressWarnings("unchecked")
   public boolean createEntry(String author, String title, String link, Date date, String desc, String geopointLat,String geopointLong) 
   {
	   boolean retval=false;
	   try
        {
        	// Add entry to the feed
	        SyndEntry entry = new SyndEntryImpl();
            SyndContent description;
            
            entry = new SyndEntryImpl();
            if(title!=null) 	entry.setTitle(title);
            if(link!=null) 		entry.setLink(link);
            if(date!=null) 		entry.setPublishedDate(date);
            if(author!=null) 	entry.setAuthor(author);
            if(desc!=null)
            {
	            description = new SyndContentImpl();
	            description.setType("text/plain");
	            description.setValue(desc);
	            entry.setDescription(description);
            }
            if(meta.AddGeoRSS() && geopointLat!=null && geopointLong!=null)
            {
	            // Add GeoRSS?
            	GeoRSSModule geoRSSModule = new SimpleModuleImpl();
            	if(meta.useGeoRSSGML())  geoRSSModule = new W3CGeoModuleImpl();
	            geoRSSModule.setPosition(new Position(Const.toDouble(geopointLat.replace(',', '.'),0), Const.toDouble(geopointLong.replace(',', '.'),0)));
	            entry.getModules().add(geoRSSModule);
            }
            
            data.entries.add(entry);

            retval=true;
        } catch (Exception e)
        {
			logError(Messages.getString("RssOutput.Log.ErrorAddingEntry", e.getMessage()));
			setErrors(1);
			retval = false;
        }
        return retval;
    }
	
	private boolean WriteToFile(String title, String link, String description, Date Pubdate,
			String copyright,String imageTitle, String imageDescription,String imageLink, String imageUrl,String language,String author)
	{
		boolean retval=false;
		try
		{
			// Specify Filename
            String fileName=data.filename;;
            
			// Set channel ...
			data.feed =new SyndFeedImpl();				 
            if(Const.isEmpty(meta.getVersion())) 
            	data.feed.setFeedType("rss_2.0");
            else
            	data.feed.setFeedType(meta.getVersion());
            
            // Set encoding ...			 
            if(Const.isEmpty(meta.getEncoding())) 
            	data.feed.setEncoding("iso-8859-1");
            else
            	data.feed.setEncoding(meta.getEncoding());
            
            
            if(title!=null) data.feed.setTitle(title);
            if(link!=null) data.feed.setLink(link);
            if(description!=null) data.feed.setDescription(description);
            if(Pubdate!=null)  data.feed.setPublishedDate(Pubdate);//data.dateParser.parse(Pubdate.toString()));
            // Set image ..
            if(meta.AddImage())
            {
	            SyndImage image = new SyndImageImpl();
	            if(imageTitle!=null) image.setTitle(title);
	            if(imageLink!=null) image.setLink(link);
	            if(imageUrl!=null) image.setUrl(imageUrl);
	            if(imageDescription!=null)  image.setDescription(imageDescription);
	            data.feed.setImage(image);
            }
            if(language!=null) data.feed.setLanguage(language);
            if(copyright!=null) data.feed.setCopyright(copyright);
            if(author!=null) data.feed.setAuthor(author);
            
			// Add entries
			data.feed.setEntries(data.entries);
            
            Writer writer = new FileWriter(fileName);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(data.feed,writer);
            writer.close();
            
        	if (meta.AddToResult())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(fileName), getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with a RSS Output step");
	            addResultFile(resultFile);
			}
	            
	        if(log.isDetailed())
	        	log.logDetailed(toString(),Messages.getString("RssOutput.Log.CreatingFileOK",fileName));
        	
			retval=true;
		}
		catch(Exception e)
		{
			logError(Messages.getString("RssOutput.Log.ErrorCreatingFile",e.toString()));
			setErrors(1);
			retval = false;
		}	
		return retval;
	}
	
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RssOutputMeta)smi;
		data=(RssOutputData)sdi;

		if (super.init(smi, sdi))
		{
			
			return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RssOutputMeta)smi;
		data=(RssOutputData)sdi;
		if(data.document!=null) data.document=null;
		if(data.rssElement!=null) data.rssElement=null;
		if(data.channel!=null) data.channel=null;
		setOutputDone();
		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}
