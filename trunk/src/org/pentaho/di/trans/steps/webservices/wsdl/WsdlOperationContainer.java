package org.pentaho.di.trans.steps.webservices.wsdl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameter.ParameterMode;

public class WsdlOperationContainer implements WsdlParamContainer
{

    private ParameterMode mode;

    private WsdlOperation operation;

    public WsdlOperationContainer(WsdlOperation operation, ParameterMode mode)
    {
        this.mode = mode;
        this.operation = operation;
    }

    public String getContainerName()
    {
        return null;
    }

    public String[] getParamNames()
    {
        List<String> paramsRet = new ArrayList<String>();
        for (WsdlOpParameter param : operation.getParameters()) {
            if (param.getMode().equals(mode))
            {
                paramsRet.add(param.getName().getLocalPart());
            }
        }
        return (String[]) paramsRet.toArray(new String[paramsRet.size()]);
    }

    public String getParamType(String paramName)
    {
        String typeRet = null;
        for (WsdlOpParameter param : operation.getParameters())
        {
            if (param.getMode().equals(mode) && param.getName().getLocalPart().equals(paramName))
            {
                typeRet = param.getXmlType().getLocalPart();
                break;
            }
        }
        return typeRet;
    }

    public String getItemName()
    {
        // This method is only relevant for output containers
        return null;
    }

    public boolean isArray()
    {
        return false;
    }
}
