package harmonizationtool.query;

import com.hp.hpl.jena.query.ResultSet;
import com.sun.tools.corba.se.idl.InvalidArgument;

/**
 * @author Tommy Cathey & Tom Transue
 * 
 * 
 *         interface for basic sparql queries returning ResultSets
 */
public interface HarmonyQuery2 {
	/**
	 * @return the un-parameterized query. That is the query with the parameters
	 *         filled in
	 */
	String getQuery();

	String getParameterizedQuery();

	ResultSet getResultSet() throws InvalidArgument;

	void setParameters(String... parameters) throws InvalidArgument;

	boolean requiresParameters();
}
