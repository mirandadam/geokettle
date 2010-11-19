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
package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;




public class TransPainter
{
    public static final String STRING_PARTITIONING_CURRENT_STEP = "PartitioningCurrentStep"; // $NON-NLS-1$
    public static final String STRING_PARTITIONING_CURRENT_NEXT = "PartitioningNextStep";    // $NON-NLS-1$
	public static final String STRING_REMOTE_INPUT_STEPS        = "RemoteInputSteps";        // $NON-NLS-1$
	public static final String STRING_REMOTE_OUTPUT_STEPS       = "RemoteOutputSteps";       // $NON-NLS-1$
	public static final String STRING_STEP_ERROR_LOG            = "StepErrorLog";            // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_COPY             = "HopTypeCopy";             // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_INFO             = "HopTypeInfo";             // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_ERROR            = "HopTypeError";            // $NON-NLS-1$
	public static final String STRING_INFO_STEP_COPIES          = "InfoStepMultipleCopies";  // $NON-NLS-1$
	
	public static final String[] magnificationDescriptions = 
		new String[] { "  200% ", "  150% ", "  100% ", "  75% ", "  50% ", "  25% "};

/*	
	public static final float[] magnifications = 
		new float[] { 0.10f, 0.15f, 0.20f, 0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f, 0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.00f, 1.25f, 1.50f, 2.00f, 3.00f, 4.00f, 5.00f, 7.50f, 10.00f, };
	
	public static final int MAGNIFICATION_100_PERCENT_INDEX = 18;

	*/
	private PropsUI      props;
    private int          shadowsize;
    private Point        area;
    private TransMeta    transMeta;
    private ScrollBar    hori, vert;

    private Point        offset;

    private Color        background;
    private Color        black;
    private Color        red;
    private Color        yellow;
    private Color        orange;
    private Color        green;
    private Color        blue;
    // private Color        magenta;
    private Color        gray;
    // private Color        lightGray;
    private Color        darkGray;

    private Font         noteFont;
    private Font         graphFont;

    private TransHopMeta candidate;
    private Point        drop_candidate;
    private int          iconsize;
    private int          gridSize;
    private Rectangle    selrect;
    private int          linewidth;
    private Map<String, Image> images;
    
    private List<AreaOwner> areaOwners;
    
    private float           magnification;
    private float           translationX;
    private float           translationY;
	private boolean shadow;
	
	private Map<StepMeta, String> stepLogMap;

    public TransPainter(TransMeta transMeta)
    {
        this(transMeta, transMeta.getMaximum(), null, null, null, null, null, new ArrayList<AreaOwner>());
    }

    public TransPainter(TransMeta transMeta, Point area)
    {
        this(transMeta, area, null, null, null, null, null, new ArrayList<AreaOwner>());
    }

    public TransPainter(TransMeta transMeta, 
                        Point area, 
                        ScrollBar hori, ScrollBar vert, 
                        TransHopMeta candidate, Point drop_candidate, Rectangle selrect,
                        List<AreaOwner> areaOwners
                        )
    {
        this.transMeta      = transMeta;
        
        this.background     = GUIResource.getInstance().getColorGraph();
        this.black          = GUIResource.getInstance().getColorBlack();
        this.red            = GUIResource.getInstance().getColorRed();
        this.yellow         = GUIResource.getInstance().getColorYellow();
        this.orange         = GUIResource.getInstance().getColorOrange();
        this.green          = GUIResource.getInstance().getColorGreen();
        this.blue           = GUIResource.getInstance().getColorBlue();
        // this.magenta        = GUIResource.getInstance().getColorMagenta();
        this.gray           = GUIResource.getInstance().getColorGray();
        // this.lightGray      = GUIResource.getInstance().getColorLightGray();
        this.darkGray       = GUIResource.getInstance().getColorDarkGray();
        
        this.area           = area;
        this.hori           = hori;
        this.vert           = vert;
        this.noteFont       = GUIResource.getInstance().getFontNote();
        this.graphFont      = GUIResource.getInstance().getFontGraph();
        this.images         = GUIResource.getInstance().getImagesSteps();
        this.candidate      = candidate;
        this.selrect        = selrect;
        this.drop_candidate = drop_candidate;
        
        this.areaOwners     = areaOwners;
        
        props = PropsUI.getInstance();
        iconsize = props.getIconSize(); 
        linewidth = props.getLineWidth();
        
        magnification = 1.0f;
        
        stepLogMap = null;
    }

    public Image getTransformationImage(Device device)
    {
        return getTransformationImage(device, false);
    }
    
    public Image getTransformationImage(Device device, boolean branded)
    {
        Image img = new Image(device, area.x, area.y);
        GC gc = new GC(img);
        
        if (props.isAntiAliasingEnabled()) gc.setAntialias(SWT.ON);
        
        areaOwners.clear(); // clear it before we start filling it up again.
        
        gridSize = props.getCanvasGridSize();
        shadowsize = props.getShadowSize();
        
        Point max   = transMeta.getMaximum();
        Point thumb = getThumb(area, max);
        offset = getOffset(thumb, area);

        // First clear the image in the background color
        gc.setBackground(background);
        gc.fillRectangle(0, 0, area.x, area.y);
        
        if (branded)
        {
            Image gradient= GUIResource.getInstance().getImageBanner();
            gc.drawImage(gradient, 0, 0);

            Image logo = GUIResource.getInstance().getImageKettleLogo();
            org.eclipse.swt.graphics.Rectangle logoBounds = logo.getBounds();
            gc.drawImage(logo, 20, area.y-logoBounds.height);
        }

        
        // If there is a shadow, we draw the transformation first with an alpha setting
        //
        if (shadowsize>0) {
        	shadow = true;
        	Transform transform = new Transform(device);
        	transform.translate(translationX+shadowsize*magnification, translationY+shadowsize*magnification);
        	transform.scale(magnification, magnification);
        	gc.setTransform(transform);
            gc.setAlpha(20);
        	
        	drawTrans(gc, thumb);
        }
        
        // Draw the transformation onto the image
        //
        shadow = false;
    	Transform transform = new Transform(device);
    	transform.translate(translationX, translationY);
    	transform.scale(magnification, magnification);
    	gc.setTransform(transform);
        gc.setAlpha(255);
        drawTrans(gc, thumb);
        
        gc.dispose();
        
        return img;
    }

    private void drawTrans(GC gc, Point thumb)
    {
        if (!shadow && gridSize>1) {
        	drawGrid(gc);
        }
        
        if (hori!=null && vert!=null)
        {
            hori.setThumb(thumb.x);
            vert.setThumb(thumb.y);
        }
        
        gc.setFont(noteFont);
        
        // First the notes
        for (int i = 0; i < transMeta.nrNotes(); i++)
        {
            NotePadMeta ni = transMeta.getNote(i);
            drawNote(gc, ni);
        }

        gc.setFont(graphFont);
        gc.setBackground(background);

        for (int i = 0; i < transMeta.nrTransHops(); i++)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            drawHop(gc, hi);
        }

        if (candidate != null)
        {
            drawHop(gc, candidate, true);
        }

        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isDrawn()) drawStep(gc, stepMeta);
        }

        if (drop_candidate != null)
        {
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setForeground(black);
            Point screen = real2screen(drop_candidate.x, drop_candidate.y, offset);
            gc.drawRectangle(screen.x, screen.y,          iconsize, iconsize);
        }

        if (!shadow) {
        	drawRect(gc, selrect);
        }
    }

    private void drawGrid(GC gc) {
    	Rectangle bounds = gc.getDevice().getBounds();
		for (int x=0;x<bounds.width;x+=gridSize) {
			for (int y=0;y<bounds.height;y+=gridSize) {
				gc.drawPoint(x+(offset.x%gridSize),y+(offset.y%gridSize));
			}
		}
	}

	private void drawHop(GC gc, TransHopMeta hi)
    {
        drawHop(gc, hi, false);
    }

    private void drawNote(GC gc, NotePadMeta notePadMeta)
    {
        int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT;

        if (notePadMeta.isSelected()) gc.setLineWidth(2); else gc.setLineWidth(1);
        
        org.eclipse.swt.graphics.Point ext;
        if (Const.isEmpty(notePadMeta.getNote()))
        {
            ext = new org.eclipse.swt.graphics.Point(10,10); // Empty note
        }
        else
        {
            ext = gc.textExtent(notePadMeta.getNote(), flags);
        }
        Point p = new Point(ext.x, ext.y);
        Point loc = notePadMeta.getLocation();
        Point note = real2screen(loc.x, loc.y, offset);
        int margin = Const.NOTE_MARGIN;
        p.x += 2 * margin;
        p.y += 2 * margin;
        int width = notePadMeta.width;
        int height = notePadMeta.height;
        if (p.x > width) width = p.x;
        if (p.y > height) height = p.y;

        int noteshape[] = new int[] { note.x, note.y, // Top left
                note.x + width + 2 * margin, note.y, // Top right
                note.x + width + 2 * margin, note.y + height, // bottom right 1
                note.x + width, note.y + height + 2 * margin, // bottom right 2
                note.x + width, note.y + height, // bottom right 3
                note.x + width + 2 * margin, note.y + height, // bottom right 1
                note.x + width, note.y + height + 2 * margin, // bottom right 2
                note.x, note.y + height + 2 * margin // bottom left
        };

        gc.setForeground(darkGray);
        gc.setBackground(yellow);

        gc.fillPolygon(noteshape);
        gc.drawPolygon(noteshape);
        
        gc.setForeground(black);
        if ( !Const.isEmpty(notePadMeta.getNote()) )
        {
            gc.drawText(notePadMeta.getNote(), note.x + margin, note.y + margin, flags);
        }

        notePadMeta.width = width; // Save for the "mouse" later on...
        notePadMeta.height = height;

        if (notePadMeta.isSelected()) gc.setLineWidth(1); else gc.setLineWidth(2);
        
        // Add to the list of areas...
        //
        if (!shadow) {
        	areaOwners.add(new AreaOwner(note.x, note.y, width, height, transMeta, notePadMeta));
        }
    }

    private void drawHop(GC gc, TransHopMeta hi, boolean is_candidate)
    {
        StepMeta fs = hi.getFromStep();
        StepMeta ts = hi.getToStep();

        if (fs != null && ts != null)
        {
            drawLine(gc, fs, ts, hi, is_candidate);
        }
    }

    private void drawStep(GC gc, StepMeta stepMeta)
    {
        if (stepMeta == null) return;

        Point pt = stepMeta.getLocation();

        int x, y;
        if (pt != null)
        {
            x = pt.x;
            y = pt.y;
        } else
        {
            x = 50;
            y = 50;
        }

        Point screen = real2screen(x, y, offset);
        
        boolean stepError = false;
        if (stepLogMap!=null && !stepLogMap.isEmpty()) {
        	String log = stepLogMap.get(stepMeta);
	        	if (!Const.isEmpty(log)) {
	        		stepError=true;
	        	}
        	}

        // REMOTE STEPS
        
        // First draw an extra indicator for remote input steps...
        //
        if (!stepMeta.getRemoteInputSteps().isEmpty()) {
        	gc.setLineWidth(1);
        	gc.setForeground(GUIResource.getInstance().getColorGray());
        	gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setFont(GUIResource.getInstance().getFontGraph());
        	String nrInput = Integer.toString(stepMeta.getRemoteInputSteps().size());
        	org.eclipse.swt.graphics.Point textExtent = gc.textExtent(nrInput);
        	textExtent.x+=2; // add a tiny little bit of a margin
        	textExtent.y+=2;
        	
        	// Draw it an icon above the step icon.
        	// Draw it an icon and a half to the left
        	//
        	Point point = new Point(screen.x-iconsize-iconsize/2, screen.y-iconsize);
        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
        	gc.drawText(nrInput, point.x+1, point.y+1);
        	
        	// Now we draw an arrow from the cube to the step...
        	// 
        	gc.drawLine(point.x+textExtent.x, point.y+textExtent.y/2, screen.x-iconsize/2, point.y+textExtent.y/2);
         	drawArrow(gc, screen.x-iconsize/2, point.y+textExtent.y/2, screen.x+iconsize/3, screen.y, Math.toRadians(15), 15, 1.8, null, null );
         	
            // Add to the list of areas...
            if (!shadow) {
            	areaOwners.add(new AreaOwner(point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_REMOTE_INPUT_STEPS));
            }
        }

        // Then draw an extra indicator for remote output steps...
        //
        if (!stepMeta.getRemoteOutputSteps().isEmpty()) {
        	gc.setLineWidth(1);
        	gc.setForeground(GUIResource.getInstance().getColorGray());
        	gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setFont(GUIResource.getInstance().getFontGraph());
        	String nrOutput = Integer.toString(stepMeta.getRemoteOutputSteps().size());
        	org.eclipse.swt.graphics.Point textExtent = gc.textExtent(nrOutput);
        	textExtent.x+=2; // add a tiny little bit of a margin
        	textExtent.y+=2;
        	
        	// Draw it an icon above the step icon.
        	// Draw it an icon and a half to the right
        	//
        	Point point = new Point(screen.x+2*iconsize+iconsize/2-textExtent.x, screen.y-iconsize);
        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
        	gc.drawText(nrOutput, point.x+1, point.y+1);
        	
        	// Now we draw an arrow from the cube to the step...
        	// This time, we start at the left side...
        	// 
        	gc.drawLine(point.x, point.y+textExtent.y/2, screen.x+iconsize+iconsize/2, point.y+textExtent.y/2);
         	drawArrow(gc, screen.x+2*iconsize/3, screen.y, screen.x+iconsize+iconsize/2, point.y+textExtent.y/2, Math.toRadians(15), 15, 1.8, null, null );

         	// Add to the list of areas...
            if (!shadow) {
            	areaOwners.add(new AreaOwner(point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_REMOTE_OUTPUT_STEPS));
            }
        }
        
        // PARTITIONING

        // If this step is partitioned, we're drawing a small symbol indicating this...
        //
        if (stepMeta.isPartitioned()) {
        	gc.setLineWidth(1);
        	gc.setForeground(GUIResource.getInstance().getColorRed());
        	gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setFont(GUIResource.getInstance().getFontGraph());
            
            PartitionSchema partitionSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
            if (partitionSchema!=null) {
	            
            	String nrInput;
            	
            	if (partitionSchema.isDynamicallyDefined()) {
            		nrInput = "Dx"+partitionSchema.getNumberOfPartitionsPerSlave();
            	}
            	else {
            		nrInput = "Px"+Integer.toString(partitionSchema.getPartitionIDs().size());
            	}
	        	
	        	org.eclipse.swt.graphics.Point textExtent = gc.textExtent(nrInput);
	        	textExtent.x+=2; // add a tiny little bit of a margin
	        	textExtent.y+=2;
	        	
	        	// Draw it a 2 icons above the step icon.
	        	// Draw it an icon and a half to the left
	        	//
	        	Point point = new Point(screen.x-iconsize-iconsize/2, screen.y-iconsize-iconsize);
	        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
	        	gc.drawText(nrInput, point.x+1, point.y+1);
	        	
	        	// Now we draw an arrow from the cube to the step...
	        	// 
	        	gc.drawLine(point.x+textExtent.x, point.y+textExtent.y/2, screen.x-iconsize/2, point.y+textExtent.y/2);
	         	gc.drawLine(screen.x-iconsize/2, point.y+textExtent.y/2, screen.x+iconsize/3, screen.y);
	         	
	         	// Also draw the name of the partition schema below the box
	         	//
	         	gc.setForeground(gray);
	         	gc.drawText(Const.NVL(partitionSchema.getName(), "<no partition name>"), point.x, point.y+textExtent.y+3, true);
	         	
	            // Add to the list of areas...
	         	//
	            if (!shadow) {
	            	areaOwners.add(new AreaOwner(point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_PARTITIONING_CURRENT_STEP));
	            }
            }
        }
                        
        String name = stepMeta.getName();

        if (stepMeta.isSelected())
            gc.setLineWidth(linewidth + 2);
        else
            gc.setLineWidth(linewidth);
        
        // Add to the list of areas...
        if (!shadow) {
        	areaOwners.add(new AreaOwner(screen.x, screen.y, iconsize, iconsize, transMeta, stepMeta));
        }
        
        // Draw a blank rectangle to prevent alpha channel problems...
        //
        gc.fillRectangle(screen.x, screen.y, iconsize, iconsize);
        String steptype = stepMeta.getStepID();
        Image im = (Image) images.get(steptype);
        if (im != null) // Draw the icon!
        {
            org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
            gc.drawImage(im, 0, 0, bounds.width, bounds.height, screen.x, screen.y, iconsize, iconsize);
        }
        gc.setBackground(background);
        if (stepError) {
        	gc.setForeground(red);
        } else {
        	gc.setForeground(black);
        }
        gc.drawRectangle(screen.x - 1, screen.y - 1, iconsize + 1, iconsize + 1);

        Point namePosition = getNamePosition(gc, name, screen, iconsize );
        
        gc.setForeground(black);
        gc.setFont(GUIResource.getInstance().getFontGraph());
        gc.drawText(name, namePosition.x, namePosition.y, SWT.DRAW_TRANSPARENT);

        boolean partitioned=false;
        
        StepPartitioningMeta meta = stepMeta.getStepPartitioningMeta();
        if (stepMeta.isPartitioned() && meta!=null)
        {
            partitioned=true;
        }
        if (stepMeta.getClusterSchema()!=null)
        {
            String message = "C";
            message+="x"+stepMeta.getClusterSchema().findNrSlaves();
            
            gc.setBackground(background);
            gc.setForeground(black);
            gc.drawText(message, screen.x + 3 + iconsize, screen.y - 8);
        }
        if (stepMeta.getCopies() > 1  && !partitioned)
        {
            gc.setBackground(background);
            gc.setForeground(black);
            gc.drawText("x" + stepMeta.getCopies(), screen.x - 5, screen.y - 5);
        }
        
        // If there was an error during the run, the map "stepLogMap" is not empty and not null.  
        //
        if (stepError) {
        	String log = stepLogMap.get(stepMeta);
    		// Show an error lines icon in the lower right corner of the step...
    		//
    		int xError = screen.x + iconsize - 5;
    		int yError = screen.y + iconsize - 5;
    		Image image = GUIResource.getInstance().getImageStepError();
    		gc.drawImage(image, xError, yError);
    		if (!shadow) {
    			areaOwners.add(new AreaOwner(pt.x + iconsize-5, pt.y + iconsize-5, image.getBounds().width, image.getBounds().height, log, STRING_STEP_ERROR_LOG));
    		}
        }
    }

    public static final Point getNamePosition(GC gc, String string, Point screen, int iconsize)
    {
        org.eclipse.swt.graphics.Point textsize = gc.textExtent(string);
        
        int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
        int ypos = screen.y + iconsize + 5;

        return new Point(xpos, ypos);
    }

    private void drawLine(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        StepMetaInterface fsii = fs.getStepMetaInterface();
        // StepMetaInterface tsii = ts.getStepMetaInterface();

        int line[] = getLine(fs, ts);

        Color col;
        int linestyle=SWT.LINE_SOLID;
        int activeLinewidth = linewidth; 
        
        if (is_candidate)
        {
            col = blue;
        }
        else
        {
            if (hi.isEnabled())
            {
                String[] targetSteps = fsii.getTargetSteps();
                // String[] infoSteps = tsii.getInfoSteps();

                if (fs.isSendingErrorRowsToStep(ts))
                {
                    col = red;
                    linestyle = SWT.LINE_DOT;
                    activeLinewidth = linewidth+1;
                }
                else
                {
                    if (targetSteps == null) // Normal link: distribute or copy data...
                    {
                    	col = black;
                    }
                    else
                    {
                        // Visual check to see if the target step is specified...
                    	//
                        // Draw different color for Filter steps
                        // Those can point to 2 different target steps
                        // Index 0 is green (true)
                        // Index 1 is red (false)
                        //
                        if (targetSteps.length==2) {
                        	int index = Const.indexOfString(ts.getName(), targetSteps);
                        	if (index==0) {
                        		col = green;
                        	} else if (index==1) {
                        		col = red;
                        	} else {
                                linestyle = SWT.LINE_DASH;
                                activeLinewidth= 2;
                        		col = orange; // Index not found / -1  TODO : figure out a way to put an error icon with tooltip on this hop.
                        	}
                        } else { 
	                        if (Const.indexOfString(ts.getName(), targetSteps) >= 0)
	                        {
	                            col = black;
	                        }
	                        else
	                        {
	                            linestyle = SWT.LINE_DOT;
                                activeLinewidth= 2;
	                            col = orange;         // TODO : figure out a way to put an error icon with tooltip on this hop.
	                        }
                        }
                    }
                }
            }
            else
            {
                col = gray;
            }
        }
        if (hi.split) activeLinewidth = linewidth+2;

        // Check to see if the source step is an info step for the target step.
        //
        String[] infoSteps = ts.getStepMetaInterface().getInfoSteps();
        if (!Const.isEmpty(infoSteps)) {
        	// Check this situation, the source step can't run in multiple copies!
        	//
        	for (String infoStep : infoSteps) {
        		if (fs.getName().equalsIgnoreCase(infoStep)) {
        			// This is the info step over this hop!
        			//
        			if (fs.getCopies()>1) {
        				// This is not a desirable situation, it will always end in error.
        				// As such, it's better not to give feedback on it.
        				// We do this by drawing an error icon over the hop...
        				//
        				col=red;
        			}
        		}
        	}
        }
        
        gc.setForeground(col);
        gc.setLineStyle(linestyle);
        gc.setLineWidth(activeLinewidth);
        
        drawArrow(gc, line, fs, ts);
        
        if (hi.split) gc.setLineWidth(linewidth);

        gc.setForeground(black);
        gc.setBackground(background);
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    private Point getThumb(Point area, Point transMax)
    {
    	Point resizedMax = magnifyPoint(transMax);
    	
        Point thumb = new Point(0, 0);
        if (resizedMax.x <= area.x)
            thumb.x = 100;
        else
            thumb.x = 100 * area.x / resizedMax.x;

        if (resizedMax.y <= area.y)
            thumb.y = 100;
        else
            thumb.y = 100 * area.y / resizedMax.y;

        return thumb;
    }
    
    private Point magnifyPoint(Point p) {
    	return new Point(Math.round(p.x * magnification), Math.round(p.y*magnification));
    }
    
    private Point getOffset(Point thumb, Point area)
    {
        Point p = new Point(0, 0);

        if (hori==null || vert==null) return p;

        Point sel = new Point(hori.getSelection(), vert.getSelection());

        if (thumb.x == 0 || thumb.y == 0) return p;

        p.x = -sel.x * area.x / thumb.x;
        p.y = -sel.y * area.y / thumb.y;

        return p;
    }
    
    public static final Point real2screen(int x, int y, Point offset)
    {
        Point screen = new Point(x + offset.x, y + offset.y);

        return screen;
    }
    
    private void drawRect(GC gc, Rectangle rect)
    {
        if (rect == null) return;
        gc.setLineStyle(SWT.LINE_DASHDOT);
        gc.setLineWidth(linewidth);
        gc.setForeground(gray);
        gc.drawRectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height);
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    private int[] getLine(StepMeta fs, StepMeta ts)
    {
        Point from = fs.getLocation();
        Point to = ts.getLocation();
        
        int x1 = from.x + iconsize / 2;
        int y1 = from.y + iconsize / 2;

        int x2 = to.x + iconsize / 2;
        int y2 = to.y + iconsize / 2;

        return new int[] { x1, y1, x2, y2 };
    }

    private void drawArrow(GC gc, int line[], Object startObject, Object endObject)
    {
    	double theta = Math.toRadians(11); // arrowhead sharpness
        int size = 19 + (linewidth - 1) * 5; // arrowhead length

        Point screen_from = real2screen(line[0], line[1], offset);
        Point screen_to = real2screen(line[2], line[3], offset);
        
        drawArrow(gc, screen_from.x, screen_from.y, screen_to.x, screen_to.y, theta, size, -1, startObject, endObject);
    }

    private void drawArrow(GC gc, int x1, int y1, int x2, int y2, double theta, int size, double factor, Object startObject, Object endObject)
    {
        int mx, my;
        int x3;
        int y3;
        int x4;
        int y4;
        int a, b, dist;
        double angle;

        gc.drawLine(x1, y1, x2, y2);

        // in between 2 points
        mx = x1 + (x2 - x1) / 2;
        my = y1 + (y2 - y1) / 2;

        a = Math.abs(x2 - x1);
        b = Math.abs(y2 - y1);
        dist = (int) Math.sqrt(a * a + b * b);

        // determine factor (position of arrow to left side or right side
        // 0-->100%)
        if (factor<0)
        {
	        if (dist >= 2 * iconsize)
	             factor = 1.5;
	        else
	             factor = 1.2;
        }

        // in between 2 points
        mx = (int) (x1 + factor * (x2 - x1) / 2);
        my = (int) (y1 + factor * (y2 - y1) / 2);
        
        // calculate points for arrowhead
        angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;

        x3 = (int) (mx + Math.cos(angle - theta) * size);
        y3 = (int) (my + Math.sin(angle - theta) * size);

        x4 = (int) (mx + Math.cos(angle + theta) * size);
        y4 = (int) (my + Math.sin(angle + theta) * size);

        Color fore = gc.getForeground();
        Color back = gc.getBackground();
        gc.setBackground(fore);
        gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
        gc.setBackground(back);
        
        if ( startObject instanceof StepMeta && endObject instanceof StepMeta) {
        	factor = 0.8;

        	StepMeta fs = (StepMeta)startObject;
        	StepMeta ts = (StepMeta)endObject;
        	
	        // in between 2 points
	        mx = (int) (x1 + factor * (x2 - x1) / 2) - 8;
	        my = (int) (y1 + factor * (y2 - y1) / 2) - 8;

	        if (!fs.isDistributes() && !ts.getStepPartitioningMeta().isMethodMirror()) {
		        
	        	Image copyHopsIcon = GUIResource.getInstance().getImageCopyHop();
	        	gc.drawImage(copyHopsIcon, mx, my);
	        	
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(mx, my, copyHopsIcon.getBounds().width, copyHopsIcon.getBounds().height, fs, STRING_HOP_TYPE_COPY));
	    		}
		        mx+=16;
	        } else if (fs.isSendingErrorRowsToStep(ts)) {
	        	Image copyHopsIcon = GUIResource.getInstance().getImageErrorHop();
		        gc.drawImage(copyHopsIcon, mx, my);
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(mx, my, copyHopsIcon.getBounds().width, copyHopsIcon.getBounds().height, new StepMeta[] { fs, ts, }, STRING_HOP_TYPE_ERROR));
	    		}
		        mx+=16;
            }
	        
	        if (Const.indexOfString(fs.getName(), ts.getStepMetaInterface().getInfoSteps()) >= 0) {
	        	Image copyHopsIcon = GUIResource.getInstance().getImageInfoHop();
	        	gc.drawImage(copyHopsIcon, mx, my);
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(mx, my, copyHopsIcon.getBounds().width, copyHopsIcon.getBounds().height, new StepMeta[] { fs, ts, }, STRING_HOP_TYPE_INFO));
	    		}
		        mx+=16;
	        }
	        
	        // Check to see if the source step is an info step for the target step.
	        //
	        String[] infoSteps = ts.getStepMetaInterface().getInfoSteps();
	        if (!Const.isEmpty(infoSteps)) {
	        	// Check this situation, the source step can't run in multiple copies!
	        	//
	        	for (String infoStep : infoSteps) {
	        		if (fs.getName().equalsIgnoreCase(infoStep)) {
	        			// This is the info step over this hop!
	        			//
	        			if (fs.getCopies()>1) {
	        				// This is not a desirable situation, it will always end in error.
	        				// As such, it's better not to give feedback on it.
	        				// We do this by drawing an error icon over the hop...
	        				//
	        	        	Image errorHopsIcon = GUIResource.getInstance().getImageErrorHop();
	        	        	gc.drawImage(errorHopsIcon, mx, my);
	        	        	if (!shadow) {
	        	    			areaOwners.add(new AreaOwner(mx, my, errorHopsIcon.getBounds().width, errorHopsIcon.getBounds().height, new StepMeta[] { fs, ts, }, STRING_INFO_STEP_COPIES));
	        	    		}
	        		        mx+=16;
	        				
	        			}
	        		}
	        	}
	        }


        }

    }

	/**
	 * @return the magnification
	 */
	public float getMagnification() {
		return magnification;
	}

	/**
	 * @param magnification the magnification to set
	 */
	public void setMagnification(float magnification) {
		this.magnification = magnification;
	}

	/**
	 * @return the translationX
	 */
	public float getTranslationX() {
		return translationX;
	}

	/**
	 * @param translationX the translationX to set
	 */
	public void setTranslationX(float translationX) {
		this.translationX = translationX;
	}

	/**
	 * @return the translationY
	 */
	public float getTranslationY() {
		return translationY;
	}

	/**
	 * @param translationY the translationY to set
	 */
	public void setTranslationY(float translationY) {
		this.translationY = translationY;
	}

	/**
	 * @return the stepLogMap
	 */
	public Map<StepMeta, String> getStepLogMap() {
		return stepLogMap;
	}

	/**
	 * @param stepLogMap the stepLogMap to set
	 */
	public void setStepLogMap(Map<StepMeta, String> stepLogMap) {
		this.stepLogMap = stepLogMap;
	}

}
