package harmonizationtool.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataSourceKeeper {
	private static List<DataSourceProvider> dataSourceProviderList = new ArrayList<DataSourceProvider>();

	// protected final static Model model = ActiveTDB.model;

	private DataSourceKeeper() {
	}

	public static boolean add(DataSourceProvider dataSourceProvider) {
		return dataSourceProviderList.add(dataSourceProvider);
	}

	public static boolean remove(DataSourceProvider dataSourceProvider) {
		dataSourceProvider.remove();
		return dataSourceProviderList.remove(dataSourceProvider);
	}

	// public static boolean remove(DataSourceProvider dataSourceProvider, boolean removeTDBData) {
	// if (removeTDBData) {
	// Resource tdbResource = dataSourceProvider.getTdbResource();
	// ActiveTDB.removeAllWithSubject(tdbResource);
	// ActiveTDB.removeAllWithObject(tdbResource);
	// }
	// return dataSourceProviderList.remove(dataSourceProvider);
	// }

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
			results.add(iterator.next().getDataSourceName());
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * THE FOLLOWING METHOD PRODUCES A DataSourceName DISTINCT FROM ANY IN THE TDB
	 * 
	 * @param proposedNewDataSourceName
	 * @return
	 */
	public static String uniquify(String proposedNewDataSourceName) {
		String uniqueName = proposedNewDataSourceName;
		Pattern numberedEnding = Pattern.compile("(.*)_(\\d+)$");

		while (indexOfDataSourceName(uniqueName) > -1) {
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
			if (dataSourceProvider.getDataSourceName().equals(name)) {
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
			if (dataSourceProvider.getDataSourceName().equals(name)) {
				return dataSourceProvider;
			}
		}
		return null;
	}
}
