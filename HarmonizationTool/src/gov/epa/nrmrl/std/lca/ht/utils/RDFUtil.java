package gov.epa.nrmrl.std.lca.ht.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

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
			try {
				return Class.forName("Float");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdouble)) {
			try {
				return Class.forName("Double");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDint)) {
			try {
				return Class.forName("Integer");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDlong)) {
			try {
				return Class.forName("Long");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDshort)) {
			try {
				return Class.forName("Short");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDbyte)) {
			try {
				return Class.forName("Byte");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDinteger)) {
			try {
				return Class.forName("BigInteger");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdecimal)) {
			try {
				return Class.forName("BigDecimal");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDboolean)) {
			try {
				return Class.forName("Boolean");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDstring)) {
			try {
//				System.out.println("String.class.getName() "+ String.class.getName());
				return Class.forName("java.lang.String");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (rdfDatatype.equals(XSDDatatype.XSDdateTime)) {
			try {
				return Class.forName("Date");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
