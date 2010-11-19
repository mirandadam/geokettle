package org.pentaho.di.core.geospatial;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.GeometryException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
//import org.pentaho.di.trans.steps.gisfileoutput.Messages;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles GML file writing from a Deegree datastore.
 * 
 * @author jmathieu, tbadard
 * @since 10-may-2010
 */
public class GMLWriter
{
    private LogWriter   log;
    private boolean     error;
    private java.net.URL gmlURL;
    private FeatureType ft;
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private FeatureCollection fc;
    private RowMetaInterface rowMeta;
    private int count = 0;
    
    public GMLWriter(java.net.URL fileURL)
    {
        this.log      = LogWriter.getInstance();
        this.gmlURL = fileURL;
        error         = false;
        rowMeta = null;
    }
    
    public void open() throws KettleException
    {
 		try {
 			
 			close();

 			if((!gmlURL.toString().substring(gmlURL.toString().length()-3,gmlURL.toString().length()).equalsIgnoreCase("gml")) && (!gmlURL.toString().substring(gmlURL.toString().length()-3,gmlURL.toString().length()).equalsIgnoreCase("xml")))
 				throw new KettleException("The output specified is not in gml format (.gml, .xml)");
		}
		catch(Exception e) {
			throw new KettleException("Error opening GML file at URL: " + gmlURL, e);
		}
    }
   
    public void createFeatureType(RowMetaInterface fields, Object[] firstRow, URL url) throws KettleException{
        String debug="get attributes from table";
        
        rowMeta = fields;            
        
        PropertyType[] props = new PropertyType[rowMeta.size()];
        
        try
        {
            // Fetch all field information
            debug="allocate data types";

            for(int i = 0; i < fields.size(); i++)
            {           	
              if (log.isDebug()) debug="get attribute #"+i;

              ValueMetaInterface value = fields.getValueMeta(i);
                                
              if (value.getType() == ValueMeta.TYPE_STRING)
              {    
            	  QualifiedName qn = new QualifiedName(value.getName());
            	  SimplePropertyType spt = new SimplePropertyType(qn,org.deegree.datatypes.Types.VARCHAR,0,-1);
                  props[i] = spt;         	  
              }
              else if (value.getType() == ValueMeta.TYPE_INTEGER)
              {    
            	  QualifiedName qn = new QualifiedName(value.getName());
            	  SimplePropertyType spt = new SimplePropertyType(qn,org.deegree.datatypes.Types.INTEGER,0,-1);
                  props[i] = spt;          	  
              }
              else if (value.getType() == ValueMeta.TYPE_NUMBER)
              {   
            	  QualifiedName qn = new QualifiedName(value.getName());
            	  SimplePropertyType spt = new SimplePropertyType(qn,org.deegree.datatypes.Types.DOUBLE,0,-1);
                  props[i] = spt;         	  
              }
              else if (value.getType() == ValueMeta.TYPE_DATE)
              {   
            	  QualifiedName qn = new QualifiedName(value.getName());
            	  SimplePropertyType spt = new SimplePropertyType(qn,org.deegree.datatypes.Types.DATE,0,-1);
                  props[i] = spt;          	  
              }
              else if (value.getType() == ValueMeta.TYPE_GEOMETRY)
              {    
            	  // determine the geometry type from the first row's geometry object
            	  Object o = firstRow[i];
            	  if(o instanceof Geometry) {           		 
                	  QualifiedName qn = new QualifiedName(value.getName());
                	  QualifiedName qntype = new QualifiedName("GeometryPropertyType");
                	  GeometryPropertyType gpt = new GeometryPropertyType(qn,qntype,org.deegree.datatypes.Types.GEOMETRY,0,-1);
                      props[i] = gpt;
            	  }
            	  else {
            		  throw new KettleException("Wrong object type for Geometry field");
            	  }         	  
              }
              else {
            	  //unknown
            	  QualifiedName qn = new QualifiedName(value.getName());
            	  SimplePropertyType spt = new SimplePropertyType(qn,java.sql.Types.VARCHAR,0,-1);
                  props[i] = spt;
              }
            }            
        }
        catch(Exception e)
        {
            throw new KettleException("Error reading GML file metadata (in part "+debug+")", e);
        }
        QualifiedName ft_name = new QualifiedName("type");
        ft = org.deegree.model.feature.FeatureFactory.createFeatureType(ft_name, false, props);       
    }
    

    public void putRow(Object[] r) throws KettleException{       
    	Object[] rowCopy = rowMeta.cloneRow(r);
    	   	
    	PropertyType[] props = ft.getProperties();   	
    	FeatureProperty[] fprop = new FeatureProperty[props.length];
    	for ( int i = 0; i < props.length ; i++)
    	{
    		FeatureProperty prop = null;
    		QualifiedName ftprop_name = props[i].getName();
    		if(rowCopy[i] instanceof Geometry) {
       		 	try {
       		 		org.deegree.model.spatialschema.Geometry g = org.deegree.model.spatialschema.JTSAdapter.wrap((Geometry) rowCopy[i]);
       		 		prop = org.deegree.model.feature.FeatureFactory.createFeatureProperty(ftprop_name,g);
       		 	} catch (GeometryException e) {
       		 		e.printStackTrace();
       		 	}      		 
    		}else{
    			prop = org.deegree.model.feature.FeatureFactory.createFeatureProperty(ftprop_name,rowCopy[i]);	
    		}
       		
    		fprop[i]=prop;

    	}
    	Feature feat = org.deegree.model.feature.FeatureFactory.createFeature("feature"+count, ft, fprop);
    	count++;
    	features.add(feat);   	
    }
    
    public void write() throws KettleException{
        try{
        	Feature[] feats = new Feature[features.size()];
        	for (int i = 0; i < features.size(); i++){
        		feats[i] = features.get(i);
        	}
        	fc = org.deegree.model.feature.FeatureFactory.createFeatureCollection("kettlecoll", feats);
            FileOutputStream fos = new FileOutputStream( gmlURL.toString().substring(5));
            new GMLFeatureAdapter().export( fc, fos );
            fos.close();
        }
        catch(Exception e)
        {
        	throw new KettleException("An error has occured");
        }
    }
    
    public boolean close(){
        boolean retval = false;
        try{
            retval=true;
        }
        catch(Exception e){
            log.logError(toString(), "Couldn't close iterator for datastore ["+gmlURL+"] : "+e.toString());
            error = true;
        }       
        return retval;
    }
    
    public boolean hasError(){
    	return error;
    }
    
    public String getVersionInfo(){
    	return null;
    }    
}