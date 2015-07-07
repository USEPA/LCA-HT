package gov.epa.nrmrl.std.lca.ht.utils;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * This class is intended to contain most utilities for conversions between typed temporal
 * objects and between such and String representations in various formats.  It does not
 * read or write from the TDB, but can perform operations on Literal objects.
 * 
 * @author Tom Transue
 *
 */
public class Temporal {

	/**
	 *  It appears that when retrieving a literal of type xsd:dateTime from the TDB, the Literal.getValue()
	 *  method will cause an exception if the value stored does not contain fractions of a second.  Interestingly
	 *  the API does not appear to have problems storing such values, suggesting the question of what if any checking
	 *  is done prior to typing any Literal.  It would make sense for the engine to save time by assuming the user
	 *  has properly typed their variable, but Jena would be better served if the getValue() function could fail more
	 *  gracefully for incorrectly formatted entries.  More investigation may reveal more details.  
	 */
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	/**
	 * getDateObject methods accept different parameters
	 * @param calendar A <i>Calendar</i> object.
	 * @return A <i>Date</i> object representing the same time or <i>null</i>
	 * if the <i>Calendar</i> can not be parsed as a <i>Date</i>
	 */
	public static Date getDateObject(Calendar calendar){
		long milliseconds = calendar.getTimeInMillis();
		Date result = new Date(milliseconds);
		return result;
	}
	
	/**
	 * getDateObject methods accept different parameters
	 * @param xsdDateTime An <i>XSDDateTime</i> object.
	 * @return A <i>Date</i> object representing the same time or <i>null</i>
	 * if the <i>XSDDateTime</i> can not be parsed as a <i>Date</i>
	 */
	public static Date getDateObject(XSDDateTime xsdDateTime){
		Calendar calendar = xsdDateTime.asCalendar();
		return getDateObject(calendar);
	}
	
	/**
	 * getDateObject methods accept different parameters
	 * @param calendar A Jena <i>Literal</i> assumed to be an XSDDateTime
	 * @return A <i>Date</i> object representing the same time or <i>null</i>
	 * if the <i>Literal</i> can not be parsed as a <i>Date</i>
	 */
	public static Date getDateObject(Literal literal){
		try {
			Object testObject = literal.getValue();
			if (testObject instanceof XSDDateTime){
				return getDateObject((XSDDateTime) testObject);
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	/**
	 * This is one of several methods of the form: get[Object]FromDate that
	 * return different object types as converted from a Date object
	 * @param date A <i>Date</i> object.
	 * @return A <i>Calendar</i> object representing the same time.
	 */
	public static Calendar getCalendarFromDate(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
	
	/**
	 * This is one of several methods of the form: get[Object]FromDate that
	 * return different object types as converted from a Date object
	 * @param date A <i>Date</i> object.
	 * @return An <i>XSDDateTime</i> object representing the same time.
	 */
	public static XSDDateTime getXSDDateTimeFromDate(Date date){
		Calendar calendar = getCalendarFromDate(date);
		XSDDateTime xsdDateTime = new XSDDateTime(calendar);
		return xsdDateTime;
	}
	
	/**
	 * This is one of several methods of the form: get[Object]FromDate that
	 * return different object types as converted from a Date object
	 * @param date A <i>Date</i> object.
	 * @return A <i>Literal</i> object representing the same time.
	 */
	public static Literal getLiteralFromDate1(Date date){
		XSDDateTime xsdDateTime = getXSDDateTimeFromDate(date);
		//TODO - determine if the line below has any issue regarding transactions
		//TODO - determine if the literal created can be put in any graph
		Literal literal = ActiveTDB.getModel(null).createTypedLiteral(xsdDateTime);
		return literal;
	}
	
	/**
	 * This is one of several methods of the form: get[Object]FromDate that
	 * return different object types as converted from a Date object
	 * @param date A <i>Date</i> object.
	 * @return A <i>Literal</i> object representing the same time.
	 */
	public static Literal getLiteralFromDate2(Date date){
		Calendar calendar = getCalendarFromDate(date);
		//TODO - determine if the line below has any issue regarding transactions
		//TODO - determine if the literal created can be put in any graph
		Literal literal = ActiveTDB.getModel(null).createTypedLiteral(calendar);
		return literal;
	}
	
	
	/**
	 * Get a formatted String representing the local time of a <i>Date</i>
	 * @param date A <i>Date</i>
	 * @return Formatted <i>String</i> representing the local time or <i>null</i>
	 * if parameter passed in is <i>null</i>.
	 */
	public static String getLocalDateFmt(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.format(date);
	}

	/**
	 * Get a formatted String representing the local time of a <i>Calendar</i>
	 * @param calendar A <i>Calendar</i>
	 * @return Formatted <i>String</i> representing the local time or <i>null</i>
	 * if parameter passed in is <i>null</i>.
	 */
	public static String getLocalDateFmt(Calendar calendar) {
		Date date = getDateObject(calendar);
		return getLocalDateFmt(date);
	}
	
	/**
	 * Get a formatted String representing the local time of an <i>XSDDateTime</i>
	 * @param xsdDateTime An <i>XSDDateTime</i>
	 * @return Formatted <i>String</i> representing the local time or <i>null</i>
	 * if parameter passed in is <i>null</i>.
	 */
	public static String getLocalDateFmt(XSDDateTime xsdDateTime) {
		Date date = getDateObject(xsdDateTime);
		return getLocalDateFmt(date);
	}
	
	/**
	 * Get a formatted String representing the local time of a <i>Literal</i>
	 * @param literal A <i>Literal</i> assumed to be a dateTime type
	 * @return Formatted <i>String</i> representing the local time or <i>null</i>
	 * if parameter passed in is <i>null</i> or not formatted properly.
	 */
	public static String getLocalDateFmt(Literal literal) {
		Date date = getDateObject(literal);
		return getLocalDateFmt(date);
	}

	/**
	 * 
	 * @param string Accepts a formatted string
	 * @return Date Returns the Date object represented by that string
	 * @throws ParseException
	 */
	public static Date setDateFmt(String string) throws ParseException {
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatLocal.setTimeZone(TimeZone.getDefault());
		return dateFormatLocal.parse(string);
	}

	/**
	 * Get the time in GMT
	 * @param date		Pass in a date (of type Date) and the time in GMT will be returned.
	 * @return String
	 */
	public static String getGMTDateFmt(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormatGmt.format(date);
	}

}
