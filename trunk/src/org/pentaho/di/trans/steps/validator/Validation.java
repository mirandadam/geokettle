package org.pentaho.di.trans.steps.validator;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

public class Validation implements Cloneable {
	public static final String XML_TAG = "validator_field";
	public static final String XML_TAG_ALLOWED = "allowed_value";

	private String name;
	private String fieldName;

	private int maximumLength;
	private int minimumLength;
	
	private boolean nullAllowed;
	private boolean onlyNullAllowed;
	private boolean onlyNumericAllowed;

	private int     dataType;
	private boolean dataTypeVerified;
	private String  conversionMask;
	private String  decimalSymbol;
	private String  groupingSymbol;

	private String   minimumValue;
	private String   maximumValue;
	private String[] allowedValues;
	private boolean  sourcingValues;
	private String   sourcingStepName;
	private StepMeta sourcingStep;
	private String   sourcingField;
	
	private String   startString;
	private String   startStringNotAllowed;
	private String   endString;
	private String   endStringNotAllowed;

	private String   regularExpression;
	private String   regularExpressionNotAllowed;

	private String   errorCode;
	private String   errorDescription;
	
	
	public Validation() {
		maximumLength=-1;
		minimumLength=-1;
		nullAllowed=true;
		onlyNullAllowed=false;
		onlyNumericAllowed=false;
	}
	
	public Validation(String name) {
		this();
		this.fieldName = name;
	}
	
	@Override
	public Validation clone() {
		try {
			return (Validation) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public boolean equals(Validation validation) {
		return validation.getName().equalsIgnoreCase(name);
	}

	public String getXML() {
		StringBuffer xml = new StringBuffer();
		
		xml.append(XMLHandler.openTag(XML_TAG));
		
		xml.append(XMLHandler.addTagValue("name", fieldName));
		xml.append(XMLHandler.addTagValue("validation_name", name));
		xml.append(XMLHandler.addTagValue("max_length", maximumLength));
		xml.append(XMLHandler.addTagValue("min_length", minimumLength));

		xml.append(XMLHandler.addTagValue("null_allowed", nullAllowed));
		xml.append(XMLHandler.addTagValue("only_null_allowed", onlyNullAllowed));
		xml.append(XMLHandler.addTagValue("only_numeric_allowed", onlyNumericAllowed));

		xml.append(XMLHandler.addTagValue("data_type", ValueMeta.getTypeDesc(dataType)));
		xml.append(XMLHandler.addTagValue("data_type_verified", dataTypeVerified));
		xml.append(XMLHandler.addTagValue("conversion_mask", conversionMask));
		xml.append(XMLHandler.addTagValue("decimal_symbol", decimalSymbol));
		xml.append(XMLHandler.addTagValue("grouping_symbol", groupingSymbol));

		xml.append(XMLHandler.addTagValue("max_value", maximumValue));
		xml.append(XMLHandler.addTagValue("min_value", minimumValue));

		xml.append(XMLHandler.addTagValue("start_string", startString));
		xml.append(XMLHandler.addTagValue("end_string", endString));
		xml.append(XMLHandler.addTagValue("start_string_not_allowed", startStringNotAllowed));
		xml.append(XMLHandler.addTagValue("end_string_not_allowed", endStringNotAllowed));

		xml.append(XMLHandler.addTagValue("regular_expression", regularExpression));
		xml.append(XMLHandler.addTagValue("regular_expression_not_allowed", regularExpressionNotAllowed));

		xml.append(XMLHandler.addTagValue("error_code", errorCode));
		xml.append(XMLHandler.addTagValue("error_description", errorDescription));

		xml.append(XMLHandler.addTagValue("is_sourcing_values", sourcingValues));
		xml.append(XMLHandler.addTagValue("sourcing_step", sourcingStep!=null ? sourcingStep.getName() : null));
		xml.append(XMLHandler.addTagValue("sourcing_field", sourcingField));

		xml.append(XMLHandler.openTag(XML_TAG_ALLOWED));
		if (allowedValues!=null) {
				
			for (String allowedValue : allowedValues) {
				xml.append(XMLHandler.addTagValue("value", allowedValue));
			}
		}
		xml.append(XMLHandler.closeTag(XML_TAG_ALLOWED));

		xml.append(XMLHandler.closeTag(XML_TAG));
		
		return xml.toString();
	}

	public Validation(Node calcnode) throws KettleXMLException {
		this();

		fieldName = XMLHandler.getTagValue(calcnode, "name");
		name = XMLHandler.getTagValue(calcnode, "validation_name");
		if (Const.isEmpty(name)) name = fieldName; // remain backward compatible 
		
		maximumLength = Const.toInt(XMLHandler.getTagValue(calcnode, "max_length"), -1);
		minimumLength = Const.toInt(XMLHandler.getTagValue(calcnode, "min_length"), -1);

		nullAllowed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "null_allowed"));
		onlyNullAllowed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "only_null_allowed"));
		onlyNumericAllowed = "Y".equalsIgnoreCase(XMLHandler.getTagValue(calcnode, "only_numeric_allowed"));

		dataType = ValueMeta.getType( XMLHandler.getTagValue(calcnode, "data_type") );
		dataTypeVerified = "Y".equalsIgnoreCase( XMLHandler.getTagValue(calcnode, "data_type_verified"));
		conversionMask = XMLHandler.getTagValue(calcnode, "conversion_mask");
		decimalSymbol = XMLHandler.getTagValue(calcnode, "decimal_symbol");
		groupingSymbol = XMLHandler.getTagValue(calcnode, "grouping_symbol");

		minimumValue = XMLHandler.getTagValue(calcnode, "min_value");
		maximumValue = XMLHandler.getTagValue(calcnode, "max_value");

		startString = XMLHandler.getTagValue(calcnode, "start_string");
		endString = XMLHandler.getTagValue(calcnode, "end_string");
		startStringNotAllowed = XMLHandler.getTagValue(calcnode, "start_string_not_allowed");
		endStringNotAllowed = XMLHandler.getTagValue(calcnode, "end_string_not_allowed");

		regularExpression = XMLHandler.getTagValue(calcnode, "regular_expression");
		regularExpressionNotAllowed = XMLHandler.getTagValue(calcnode, "regular_expression_not_allowed");

		errorCode = XMLHandler.getTagValue(calcnode, "error_code");
		errorDescription = XMLHandler.getTagValue(calcnode, "error_description");
		
		sourcingValues = "Y".equalsIgnoreCase( XMLHandler.getTagValue(calcnode, "is_sourcing_values"));
		sourcingStepName = XMLHandler.getTagValue(calcnode, "sourcing_step");
		sourcingField = XMLHandler.getTagValue(calcnode, "sourcing_field");
		
		Node allowedValuesNode = XMLHandler.getSubNode(calcnode, XML_TAG_ALLOWED);
		int nrValues = XMLHandler.countNodes(allowedValuesNode, "value");
		allowedValues = new String[nrValues];
		for (int i=0;i<nrValues;i++) {
			Node allowedNode = XMLHandler.getSubNodeByNr(allowedValuesNode, "value", i);
			allowedValues[i] = XMLHandler.getNodeValue(allowedNode);
		}
	}
	
	public Validation(Repository rep, long id_step, int i) throws KettleException {
		fieldName = rep.getStepAttributeString(id_step, i, "validator_field_name");
		name = rep.getStepAttributeString(id_step, i, "validator_field_validation_name");
		if (Const.isEmpty(name)) name = fieldName; // remain backward compatible
		
		maximumLength = (int)rep.getStepAttributeInteger(id_step, i, "validator_field_max_length");
		minimumLength = (int)rep.getStepAttributeInteger(id_step, i, "validator_field_min_length");

		nullAllowed = rep.getStepAttributeBoolean(id_step, i, "validator_field_null_allowed");
		onlyNullAllowed = rep.getStepAttributeBoolean(id_step, i, "validator_field_only_null_allowed");
		onlyNumericAllowed = rep.getStepAttributeBoolean(id_step, i, "validator_field_only_numeric_allowed");

		dataType = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "validator_field_data_type") );
		dataTypeVerified = rep.getStepAttributeBoolean(id_step, i, "validator_field_data_type_verified");
		conversionMask = rep.getStepAttributeString(id_step, i, "validator_field_conversion_mask");
		decimalSymbol = rep.getStepAttributeString(id_step, i, "validator_field_decimal_symbol");
		groupingSymbol = rep.getStepAttributeString(id_step, i, "validator_field_grouping_symbol");

		minimumValue = rep.getStepAttributeString(id_step, i, "validator_field_min_value");
		maximumValue = rep.getStepAttributeString(id_step, i, "validator_field_max_value");

		startString = rep.getStepAttributeString(id_step, i, "validator_field_start_string");
		endString = rep.getStepAttributeString(id_step, i, "validator_field_end_string");
		startStringNotAllowed = rep.getStepAttributeString(id_step, i, "validator_field_start_string_not_allowed");
		endStringNotAllowed = rep.getStepAttributeString(id_step, i, "validator_field_end_string_not_allowed");

		regularExpression = rep.getStepAttributeString(id_step, i, "validator_field_regular_expression");
		regularExpressionNotAllowed = rep.getStepAttributeString(id_step, i, "validator_field_regular_expression_not_allowed");

		errorCode = rep.getStepAttributeString(id_step, i, "validator_field_error_code");
		errorDescription = rep.getStepAttributeString(id_step, i, "validator_field_error_description");

		sourcingValues = rep.getStepAttributeBoolean(id_step, i, "validator_field_is_sourcing_values");
		sourcingStepName = rep.getStepAttributeString(id_step, i, "validator_field_sourcing_step");
		sourcingField = rep.getStepAttributeString(id_step, i, "validator_field_sourcing_field");
		
		List<String> allowed = new ArrayList<String>();
		
		int nr = 1;
		String value = rep.getStepAttributeString(id_step, i, "validator_field_value_"+nr);
		while (value!=null) {
			allowed.add(value);
			nr++;
			value = rep.getStepAttributeString(id_step, i, "validator_field_value_"+nr);
		}
		allowedValues = allowed.toArray(new String[allowed.size()]);
	}

	public void saveRep(Repository rep, long id_transformation, long id_step, int i) throws KettleException {
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_name", fieldName);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_validation_name", name);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_max_length", maximumLength);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_min_length", minimumLength);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_null_allowed", nullAllowed);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_only_null_allowed", onlyNullAllowed);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_only_numeric_allowed", onlyNumericAllowed);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_data_type", ValueMeta.getTypeDesc(dataType));
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_data_type_verified", dataTypeVerified);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_conversion_mask", conversionMask);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_decimal_symbol", decimalSymbol);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_grouping_symbol", groupingSymbol);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_max_value", maximumValue);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_min_value", minimumValue);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_start_string", startString);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_end_string", endString);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_start_string_not_allowed", startStringNotAllowed);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_end_string_not_allowed", endStringNotAllowed);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_regular_expression", regularExpression);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_regular_expression_not_allowed", regularExpressionNotAllowed);

		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_error_code", errorCode);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_error_description", errorDescription);
		
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_is_sourcing_values", sourcingValues);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_sourcing_step", sourcingStep!=null ? sourcingStep.getName() : null);
		rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_sourcing_field", sourcingField);
		
		if (allowedValues!=null) {
			for (int nr=1;nr<=allowedValues.length;nr++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "validator_field_value_"+nr, allowedValues[nr-1]);
			}
		}
	}

	/**
	 * @return the field name to validate
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the field name to validate
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return the maximumLength
	 */
	public int getMaximumLength() {
		return maximumLength;
	}

	/**
	 * @param maximumLength the maximumLength to set
	 */
	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	/**
	 * @return the minimumLength
	 */
	public int getMinimumLength() {
		return minimumLength;
	}

	/**
	 * @param minimumLength the minimumLength to set
	 */
	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

	/**
	 * @return the nullAllowed
	 */
	public boolean isNullAllowed() {
		return nullAllowed;
	}

	/**
	 * @param nullAllowed the nullAllowed to set
	 */
	public void setNullAllowed(boolean nullAllowed) {
		this.nullAllowed = nullAllowed;
	}

	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the conversionMask
	 */
	public String getConversionMask() {
		return conversionMask;
	}

	/**
	 * @param conversionMask the conversionMask to set
	 */
	public void setConversionMask(String conversionMask) {
		this.conversionMask = conversionMask;
	}

	/**
	 * @return the decimalSymbol
	 */
	public String getDecimalSymbol() {
		return decimalSymbol;
	}

	/**
	 * @param decimalSymbol the decimalSymbol to set
	 */
	public void setDecimalSymbol(String decimalSymbol) {
		this.decimalSymbol = decimalSymbol;
	}

	/**
	 * @return the groupingSymbol
	 */
	public String getGroupingSymbol() {
		return groupingSymbol;
	}

	/**
	 * @param groupingSymbol the groupingSymbol to set
	 */
	public void setGroupingSymbol(String groupingSymbol) {
		this.groupingSymbol = groupingSymbol;
	}

	/**
	 * @return the minimumValue
	 */
	public String getMinimumValue() {
		return minimumValue;
	}

	/**
	 * @param minimumValue the minimumValue to set
	 */
	public void setMinimumValue(String minimumValue) {
		this.minimumValue = minimumValue;
	}

	/**
	 * @return the maximumValue
	 */
	public String getMaximumValue() {
		return maximumValue;
	}

	/**
	 * @param maximumValue the maximumValue to set
	 */
	public void setMaximumValue(String maximumValue) {
		this.maximumValue = maximumValue;
	}

	/**
	 * @return the allowedValues
	 */
	public String[] getAllowedValues() {
		return allowedValues;
	}

	/**
	 * @param allowedValues the allowedValues to set
	 */
	public void setAllowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	/**
	 * @return the dataTypeVerified
	 */
	public boolean isDataTypeVerified() {
		return dataTypeVerified;
	}

	/**
	 * @param dataTypeVerified the dataTypeVerified to set
	 */
	public void setDataTypeVerified(boolean dataTypeVerified) {
		this.dataTypeVerified = dataTypeVerified;
	}

	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorDescription
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * @param errorDescription the errorDescription to set
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	/**
	 * @return true if only numeric values are allowed: A numeric data type, a date or a String containing digits only
	 */
	public boolean isOnlyNumericAllowed() {
		return onlyNumericAllowed;
	}

	/**
	 * @return the startString
	 */
	public String getStartString() {
		return startString;
	}

	/**
	 * @param startString the startString to set
	 */
	public void setStartString(String startString) {
		this.startString = startString;
	}

	/**
	 * @return the startStringNotAllowed
	 */
	public String getStartStringNotAllowed() {
		return startStringNotAllowed;
	}

	/**
	 * @param startStringNotAllowed the startStringNotAllowed to set
	 */
	public void setStartStringNotAllowed(String startStringNotAllowed) {
		this.startStringNotAllowed = startStringNotAllowed;
	}

	/**
	 * @return the endString
	 */
	public String getEndString() {
		return endString;
	}

	/**
	 * @param endString the endString to set
	 */
	public void setEndString(String endString) {
		this.endString = endString;
	}

	/**
	 * @return the endStringNotAllowed
	 */
	public String getEndStringNotAllowed() {
		return endStringNotAllowed;
	}

	/**
	 * @param endStringNotAllowed the endStringNotAllowed to set
	 */
	public void setEndStringNotAllowed(String endStringNotAllowed) {
		this.endStringNotAllowed = endStringNotAllowed;
	}

	/**
	 * @param onlyNumericAllowed the onlyNumericAllowed to set
	 */
	public void setOnlyNumericAllowed(boolean onlyNumericAllowed) {
		this.onlyNumericAllowed = onlyNumericAllowed;
	}

	/**
	 * @return the regularExpression
	 */
	public String getRegularExpression() {
		return regularExpression;
	}

	/**
	 * @param regularExpression the regularExpression to set
	 */
	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

	/**
	 * @return the name of this validation
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the new name for this validation
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the regularExpressionNotAllowed
	 */
	public String getRegularExpressionNotAllowed() {
		return regularExpressionNotAllowed;
	}

	/**
	 * @param regularExpressionNotAllowed the regularExpressionNotAllowed to set
	 */
	public void setRegularExpressionNotAllowed(String regularExpressionNotAllowed) {
		this.regularExpressionNotAllowed = regularExpressionNotAllowed;
	}
	
	/**
	 * Find a validation by name in a list of validations
	 * @param validations The list to search
	 * @param name the name to search for
	 * @return the validation if one matches or null if none is found.
	 */
	public static Validation findValidation(List<Validation> validations, String name) {
		for (Validation validation : validations) {
			if (validation.getName().equalsIgnoreCase(name)) return validation;
		}
		return null;
	}

	/**
	 * @return the onlyNullAllowed
	 */
	public boolean isOnlyNullAllowed() {
		return onlyNullAllowed;
	}

	/**
	 * @param onlyNullAllowed the onlyNullAllowed to set
	 */
	public void setOnlyNullAllowed(boolean onlyNullAllowed) {
		this.onlyNullAllowed = onlyNullAllowed;
	}

	/**
	 * @return the sourcingValues
	 */
	public boolean isSourcingValues() {
		return sourcingValues;
	}

	/**
	 * @param sourcingValues the sourcingValues to set
	 */
	public void setSourcingValues(boolean sourcingValues) {
		this.sourcingValues = sourcingValues;
	}

	/**
	 * @return the sourcingStep
	 */
	public StepMeta getSourcingStep() {
		return sourcingStep;
	}

	/**
	 * @param sourcingStep the sourcingStep to set
	 */
	public void setSourcingStep(StepMeta sourcingStep) {
		this.sourcingStep = sourcingStep;
	}

	/**
	 * @return the sourcingField
	 */
	public String getSourcingField() {
		return sourcingField;
	}

	/**
	 * @param sourcingField the sourcingField to set
	 */
	public void setSourcingField(String sourcingField) {
		this.sourcingField = sourcingField;
	}

	/**
	 * @return the sourcingStepName
	 */
	public String getSourcingStepName() {
		return sourcingStepName;
	}

	/**
	 * @param sourcingStepName the sourcingStepName to set
	 */
	public void setSourcingStepName(String sourcingStepName) {
		this.sourcingStepName = sourcingStepName;
	}
}
