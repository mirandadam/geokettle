/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Header:$
 */

package org.pentaho.di.trans.steps.webservices.wsdl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.pentaho.di.core.logging.LogWriter;

/**
 * Wsdl abstraction.
 */
public final class Wsdl implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Port _port;
    private final Definition _wsdlDefinition;
    private final Service _service;
    private final WsdlTypes _wsdlTypes;
    private HashMap<String, WsdlOperation> _operationCache;

    /**
     * Loads and parses the specified WSDL file.
     *
     * @param wsdlURI      URI of a WSDL file.
     * @param serviceQName Name of the service in the WSDL, if null default to first service in WSDL.
     * @param portName     The service port name, if null default to first port in service.
     */
    public Wsdl(URI wsdlURI, QName serviceQName, String portName) {

        try {
            _wsdlDefinition = parse(wsdlURI);
        }
        catch (WSDLException e) {
            throw new RuntimeException("Could not load WSDL file: " + e.getMessage(), e);
        }

        if (serviceQName == null) {
            _service = (Service) _wsdlDefinition.getServices().values().iterator().next();
        }
        else {
            _service = _wsdlDefinition.getService(serviceQName);
            if (_service == null) {
                throw new IllegalArgumentException("Service: " + serviceQName
                        + " is not defined in the WSDL file " + wsdlURI);
            }
        }

        if (portName == null) {
            _port = (Port) _service.getPorts().values().iterator().next();
        }
        else {
            _port = _service.getPort(portName);
            if (_port == null) {
                throw new IllegalArgumentException("Port: " + portName
                        + " is not defined in the service: " + serviceQName);
            }
        }

        _wsdlTypes = new WsdlTypes(_wsdlDefinition);
        _operationCache = new HashMap<String, WsdlOperation>();
    }

    /**
     * Loads and parses the specified WSDL file.
     *
     * @param wsdlLocator  A javax.wsdl.WSDLLocator instance.
     * @param serviceQName Name of the service in the WSDL.
     * @param portName     The service port name.
     */
    public Wsdl(WSDLLocator wsdlLocator, QName serviceQName, String portName) {

        // load and parse the WSDL
        try {
            _wsdlDefinition = parse(wsdlLocator);
        }
        catch (WSDLException e) {
            throw new RuntimeException("Could not load WSDL file: " + e.getMessage(), e);
        }

        _service = _wsdlDefinition.getService(serviceQName);
        if (_service == null) {
            throw new IllegalArgumentException("Service: " + serviceQName + " is not defined in the WSDL file.");
        }

        _port = _service.getPort(portName);
        if (_port == null) {
            throw new IllegalArgumentException("Port: " + portName
                    + " is not defined in the service: " + serviceQName);
        }

        _wsdlTypes = new WsdlTypes(_wsdlDefinition);
        _operationCache = new HashMap<String, WsdlOperation>();
    }

    /**
     * Get the WsdlComplexTypes instance of this wsdl.  WsdlComplex types provides type information
     * for named complextypes defined in the wsdl's &lt;types&gt; section.
     *
     * @return WsdlComplexTypes instance.
     */
    public WsdlComplexTypes getComplexTypes() {
        return _wsdlTypes.getNamedComplexTypes();
    }

    /**
     * Find the specified operation in the WSDL definition.
     *
     * @param operationName Name of operation to find.
     * @return A WsdlOperation instance, null if operation can not be found in WSDL.
     */
    public WsdlOperation getOperation(String operationName) {

        // is the operation in the cache?
        if (_operationCache.containsKey(operationName)) {
            return (WsdlOperation) _operationCache.get(operationName);
        }

        Binding b = _port.getBinding();
        PortType pt = b.getPortType();
        Operation op = pt.getOperation(operationName, null, null);
        if (op != null) {
        	try {
	            WsdlOperation wop = new WsdlOperation(b, op, _wsdlTypes);
	            // cache the operation
	            _operationCache.put(operationName, wop);
	            return wop;
        	}
        	catch(Exception e) {
        		LogWriter.getInstance().logError("WSDL", "Could retrieve WSDL Operator for operation name: "+operationName, e);
        	}
        }
        return null;
    }

    /**
     * Get a list of all operations defined in this WSDL.
     *
     * @return List of WsdlOperations.
     */
    @SuppressWarnings("unchecked")
	public List<WsdlOperation> getOperations() {

        List<WsdlOperation> opList = new ArrayList<WsdlOperation>();
        PortType pt = _port.getBinding().getPortType();

        List<Operation> operations = pt.getOperations();
        for (Iterator<Operation> itr = operations.iterator(); itr.hasNext();) {
        	WsdlOperation operation = getOperation(((Operation) itr.next()).getName());
        	if (operation!=null) {
        		opList.add(operation);
        	}
        }
        return opList;
    }

    /**
     * Get the name of the current port.
     *
     * @return Name of the current port.
     */
    public String getPortName() {
        return _port.getName();
    }

    /**
     * Get the PortType name for the service which has been specified by serviceName and portName
     * at construction time.
     *
     * @return QName of the PortType.
     */
    public QName getPortTypeQName() {

        Binding b = _port.getBinding();
        return b.getPortType().getQName();
    }

    /**
     * Get the service endpoint.
     *
     * @return String containing the service endpoint.
     */
    public String getServiceEndpoint() {
        return WsdlUtils.getSOAPAddress(_port);
    }

    /**
     * Get the name of this service.
     *
     * @return Service name.
     */
    public String getServiceName() {
        return _service.getQName().getLocalPart();
    }

    /**
     * Get the target namespace for the WSDL.
     *
     * @return The targetNamespace
     */
    public String getTargetNamespace() {
        return _wsdlDefinition.getTargetNamespace();
    }

    /**
     * Change the port of the service.
     *
     * @param portName The new port name.
     * @throws IllegalArgumentException if port name is not defined in WSDL.
     */
    public void setPort(QName portName) {

        Port port = _service.getPort(portName.getLocalPart());
        if (port == null) {
            throw new IllegalArgumentException("Port name: '" + portName + "' was not found in the WSDL file.");
        }

        _port = port;
        _operationCache.clear();
    }

    /**
     * Get a WSDLReader.
     *
     * @return WSDLReader.
     * @throws WSDLException on error.
     */
    private WSDLReader getReader() throws WSDLException {

        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        ExtensionRegistry registry = wsdlFactory.newPopulatedExtensionRegistry();
        wsdlReader.setExtensionRegistry(registry);
        wsdlReader.setFeature("javax.wsdl.verbose", true);
        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        return wsdlReader;
    }

    /**
     * Load and parse the WSDL file using the wsdlLocator.
     *
     * @param wsdlLocator A WSDLLocator instance.
     * @return wsdl Definition.
     * @throws WSDLException on error.
     */
    private Definition parse(WSDLLocator wsdlLocator) throws WSDLException {

        WSDLReader wsdlReader = getReader();
        return wsdlReader.readWSDL(wsdlLocator);
    }

    /**
     * Load and parse the WSDL file at the specified URI.
     *
     * @param wsdlURI URI of the WSDL file.
     * @return wsdl Definition
     * @throws WSDLException on error.
     */
    private Definition parse(URI wsdlURI) throws WSDLException {

        WSDLReader wsdlReader = getReader();
        return wsdlReader.readWSDL(wsdlURI.toString());
    }

}
