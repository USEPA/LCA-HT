package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSourceKeeper {

	private static List<DataSourceProvider> dataSourceProviderList = new ArrayList<DataSourceProvider>();

	private DataSourceKeeper() {
	}

	public static boolean add(DataSourceProvider dataSourceProvider) {
		if (dataSourceProvider.getTdbResource() == null) {
			Model model = ActiveTDB.model;
			Resource tdbResource = model.createResource();
			model.add(tdbResource, RDF.type, ECO.DataSource);
			model.add(tdbResource, RDFS.label, model.createLiteral(dataSourceProvider.getDataSourceMD().getName()));
			model.add(tdbResource, RDFS.comment, model.createLiteral(dataSourceProvider.getDataSourceMD().getComments()));
			model.add(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSourceProvider.getDataSourceMD().getVersion()));
			dataSourceProvider.setTdbResource(tdbResource);
		}
		return dataSourceProviderList.add(dataSourceProvider);
	}

	public static boolean remove(DataSourceProvider dataSourceProvider) {
		return dataSourceProviderList.remove(dataSourceProvider);
	}

	public static boolean remove(DataSourceProvider dataSourceProvider, boolean removeTDBData) {
		if (removeTDBData) {
			Resource tdbResource = dataSourceProvider.getTdbResource();
			ActiveTDB.removeAllWithSubject(tdbResource);
			ActiveTDB.removeAllWithObject(tdbResource);
		}
		return dataSourceProviderList.remove(dataSourceProvider);
	}

	public static List<Integer> getIDs() {
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			ids.add(dataSourceProviderList.indexOf(iterator.next()));
		}
		return ids;
	}

	public static List<String> getNames() {
		List<String> results = new ArrayList<String>();
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			results.add(iterator.next().getDataSourceMD().getName());
		}
		Collections.sort(results);
		return results;
	}

	public static String uniquify(String proposedNewDataSourceName) {
		String uniqueName = proposedNewDataSourceName;
		Pattern numberedEnding = Pattern.compile("(.*)_(\\d+)$");

		while (indexOfDataSourceName(uniqueName) > -1) {

			// System.out.println("checking uniqueName: "+uniqueName);
			// Pattern numberedEnding = Pattern.compile("_\\d\\d$");
			Matcher endingMatcher = numberedEnding.matcher(uniqueName);

			if (endingMatcher.find()) {
				String baseName = endingMatcher.group(1);
				// System.out.println("Match, so baseName: "+baseName);

				int nextVal = Integer.parseInt(endingMatcher.group(2)) + 1;
				uniqueName = baseName + "_" + nextVal;
				// System.out.println("... and uniqueName is now: "+uniqueName);

			} else {
				uniqueName += "_1";
				// System.out.println("No match, so uniqueName is now: "+uniqueName);
			}
		}
		return uniqueName;
	}

	public static DataSourceProvider get(int index) {
		if (index < 0) {
			return null;
		}
		if (index >= dataSourceProviderList.size()) {
			return null;
		}
		return dataSourceProviderList.get(index);
	}

	public static boolean hasIndex(int index) {
		try {
			dataSourceProviderList.get(index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer indexOf(DataSourceProvider dataSourceProvider) {
		return dataSourceProviderList.indexOf(dataSourceProvider);
	}

	public static int indexOfDataSourceName(String name) {
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			DataSourceProvider dataSourceProvider = (DataSourceProvider) iterator.next();
			if (dataSourceProvider.getDataSourceMD().getName().equals(name)) {
				return DataSourceKeeper.indexOf(dataSourceProvider);
			}
		}
		return -1;
	}

	public static int size() {
		return dataSourceProviderList.size();
	}

	public static int getByTdbResource(Resource tdbResource) {
		Iterator<DataSourceProvider> iterator = dataSourceProviderList.iterator();
		while (iterator.hasNext()) {
			DataSourceProvider dataSourceProvider = iterator.next();
			Resource resource = dataSourceProvider.getTdbResource();
			if (resource.equals(tdbResource)) {
				return dataSourceProviderList.indexOf(dataSourceProvider);
			}
		}
		return -1;
	}

	public static DataSourceProvider get(FileMD fileMD) {
		for (DataSourceProvider dataSourceProvider : dataSourceProviderList) {
			if (dataSourceProvider.getFileMDList().contains(fileMD)) {
				return dataSourceProvider;
			}
		}
		return null;
	}

	public static DataSourceProvider getByName(String name) {
		for (DataSourceProvider dataSourceProvider : dataSourceProviderList) {
			if (dataSourceProvider.getDataSourceMD().getName().equals(name)) {
				return dataSourceProvider;
			}
		}
		return null;
	}

}
