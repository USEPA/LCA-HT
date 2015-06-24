/**
 * <p>The 'Data Format Check' package contains classes designed to help confirm that imported data meet basic 
 * formatting criteria.  Because data may be read from text files and converted to non-text types (e.g. integer, 
 * float, dateTime, or boolean), it is invaluable to confirm that each data item can be parsed 
 * into the proper data format for the field it represents.  To do this, a series of "FormatCheck" instances
 * are developed to confirm the presence of required features and the absence of forbidden features.</p>
 * 
 * <p>In addition, "FormatCheck" instances to identify common formatting errors such as those caused by automated
 * processes and data inter-conversion are used to alert users to conditions which are not strictly errors, but
 * may be incorrect interpretations of the original information.</p>
 * 
 * <p>Each failed FormatCheck generates an "Issue" whose "Status" initially represents a call for attention from
 * the user.  In some cases, the user may choose to ignore the Issue in which case the Status changes.  In other
 * cases, the user may request automatic resolution if such is defined.  Some Status conditions prevent the user
 * from moving forward with the data import.</p>
 * 
 * <p>The use of format checks in intended to ensure that imported data accurately represent the information the
 * user intended to read in.</p>
 *  
 * @author Tom Transue
 *
 */
package gov.epa.nrmrl.std.lca.ht.dataFormatCheck;