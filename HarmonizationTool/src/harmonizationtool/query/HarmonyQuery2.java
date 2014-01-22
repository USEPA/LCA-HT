package harmonizationtool.query;

import com.hp.hpl.jena.query.ResultSet;
import com.sun.tools.corba.se.idl.InvalidArgument;

public interface HarmonyQuery2 {
	String getQuery();
	String getParameterizedQuery();
    ResultSet getResultSet() throws InvalidArgument;
    void setParameters(String... parameters) throws InvalidArgument;
    boolean requiresParameters();
}
