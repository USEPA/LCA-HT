package gov.epa.nrmrl.std.lca.ht.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;

public class RDFUtil {

	public static RDFDatatype getRDFDatatypeFromJavaClass(Object object) {
		if (object instanceof Float) {
			return XSDDatatype.XSDfloat;
		}
		if (object instanceof Double) {
			return XSDDatatype.XSDdouble;
		}
		if (object instanceof Integer) {
			return XSDDatatype.XSDint;
		}
		if (object instanceof Long) {
			return XSDDatatype.XSDlong;
		}
		if (object instanceof Short) {
			return XSDDatatype.XSDshort;
		}
		if (object instanceof Byte) {
			return XSDDatatype.XSDbyte;
		}
		if (object instanceof BigInteger) {
			return XSDDatatype.XSDinteger;
		}
		if (object instanceof BigDecimal) {
			return XSDDatatype.XSDdecimal;
		}
		if (object instanceof Boolean) {
			return XSDDatatype.XSDboolean;
		}
		if (object instanceof String) {
			return XSDDatatype.XSDstring;
		}
		if (object instanceof Date) {
			return XSDDatatype.XSDdateTime;
		}
		if (object instanceof Calendar) {
			return XSDDatatype.XSDdateTime;
		}
		return null;
	}

	public static Class<?> getJavaClassFromRDFDatatype(RDFDatatype rdfDatatype) {
		if (rdfDatatype.equals(XSDDatatype.XSDfloat)) {
			return Float.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdouble)) {
			return Double.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDint)) {
			return Integer.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDlong)) {
			return Long.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDshort)) {
			return Short.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDbyte)) {
			return Byte.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDinteger)) {
			return BigInteger.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdecimal)) {
			return BigDecimal.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDboolean)) {
			return Boolean.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDstring)) {
			return String.class;
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdateTime)) {
			return Date.class;
		}
		return null;
	}

	public static Date getDateFromLiteral(Literal typedLiteralDate) {
		Date resultingDate = null;
		if (!typedLiteralDate.isLiteral()) {
			return null;
		}
		// Literal literalDate = typedLiteralDate.asLiteral();
		// String formattedDate = literalDate.getString();
		// String actualFormattedDate = formattedDate.replaceFirst("\\^\\^.*", "");

		try {
			Object thing = typedLiteralDate.getDatatype();
			Object thing2 = typedLiteralDate.getValue();
			System.out.println("thing2 = " + thing2);
			// resultingDate = ((Calendar) typedLiteralDate;
			// resultingDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(actualFormattedDate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return resultingDate;
	}

}
