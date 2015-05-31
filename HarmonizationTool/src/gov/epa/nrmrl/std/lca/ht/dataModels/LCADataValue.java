package gov.epa.nrmrl.std.lca.ht.dataModels;

import java.math.BigDecimal;
import java.math.BigInteger;
//import java.util.Calendar;
//import java.util.Date;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

public class LCADataValue {
	private LCADataPropertyProvider lcaDataPropertyProvider;
	private Object value;
	private String valueAsString;

	public LCADataValue() {
	}
	
	public LCADataValue(LCADataPropertyProvider lcaDataPropertyProvider) {
		this.lcaDataPropertyProvider = lcaDataPropertyProvider;
	}
	
	public LCADataPropertyProvider getLcaDataPropertyProvider() {
		return lcaDataPropertyProvider;
	}

	public void setLcaDataPropertyProvider(LCADataPropertyProvider lcaDataPropertyProvider) {
		this.lcaDataPropertyProvider = lcaDataPropertyProvider;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.valueAsString = value.toString();
		// TODO: CHECK THE ABOVE TO CONFIRM IT IS ROBUST
		this.value = value;
	}

	public String getValueAsString() {
		if (valueAsString == null) {
			setValueFromString();
		}
		return valueAsString;
	}

	public void setValueAsString(String valueAsString) {
		this.valueAsString = valueAsString;
		setValueFromString();
	}

	private void setValueFromString() {
		if (lcaDataPropertyProvider == null) {
			return;
		}

		if (valueAsString == null) {
			return;
		}

		RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();
		if (rdfDatatype == null) {
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDfloat)) {
			try {
				Object result = Float.valueOf(valueAsString);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDdouble)) {
			try {
				Object result = Double.valueOf(valueAsString);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDint)) {
			try {
				Object result = Integer.valueOf(valueAsString);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDlong)) {
			try {
				Object result = Long.valueOf(valueAsString);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDshort)) {
			try {
				Object result = Short.valueOf(valueAsString);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDbyte)) {
			try {
				Object result = Byte.valueOf(valueAsString);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDinteger)) {
			try {
				Long longResult = Long.valueOf(valueAsString);
				Object result = BigInteger.valueOf(longResult);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDdecimal)) {
			try {
				Long longResult = Long.valueOf(valueAsString);
				Object result = BigDecimal.valueOf(longResult);
				value = result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDboolean)) {
			if (valueAsString.matches("TRUE|True|true|T|t|1|yes|on")) {
				value = true;
				return;
			}
			if (valueAsString.matches("FALST|False|false|F|f|0|no|off")) {
				value = false;
				return;
			}
			return;
		}

		if (rdfDatatype.equals(XSDDatatype.XSDstring)) {
			value = valueAsString;
			return;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdateTime)) {
			// PARSE THIS LATER AS NEED BE, I SUPPOSE
			return;
		}
	}
}
