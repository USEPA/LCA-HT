package harmonizationtool.model;

import harmonizationtool.comands.SelectTDB;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DataSetKeeper {

	private static List<DataSetProvider> dsList = new ArrayList<DataSetProvider>();

	private DataSetKeeper() {
	}

	public static boolean add(DataSetProvider dataSetProvider) {
		if (dataSetProvider.getTdbResource() == null) {
			Model model = SelectTDB.model;
			Resource tdbResource = model.createResource();
			model.add(tdbResource, RDF.type, ECO.DataSource);
			model.add(tdbResource, RDFS.label, model.createLiteral(dataSetProvider.getDataSetMD()
					.getName()));
			model.add(tdbResource, RDFS.comment, model.createLiteral(dataSetProvider.getDataSetMD()
					.getComments()));
			model.add(tdbResource, DCTerms.hasVersion, model.createLiteral(dataSetProvider
					.getDataSetMD().getVersion()));
			dataSetProvider.setTdbResource(tdbResource);
		}
		return dsList.add(dataSetProvider);
	}

	public static List<Integer> getIDs() {
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<DataSetProvider> iterator = dsList.iterator();
		while (iterator.hasNext()) {
			ids.add(dsList.indexOf(iterator.next()));
		}
		return ids;
	}

	public static DataSetProvider get(int index) {
		return dsList.get(index);
	}

	public static boolean hasIndex(int index) {
		try {
			dsList.get(index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer indexOf(DataSetProvider dataSetProvider) {
		return dsList.indexOf(dataSetProvider);
	}

	public static int size() {
		return dsList.size();
	}

	public static int getByTdbResource(Resource tdbResource) {
		Iterator<DataSetProvider> iterator = dsList.iterator();
		while (iterator.hasNext()) {
			DataSetProvider dataSetProvider = iterator.next();
			Resource resource = dataSetProvider.getTdbResource();
			if (resource.equals(tdbResource)) {
				return dsList.indexOf(dataSetProvider);
			}
		}
		return -1;
	}

	public static DataSetProvider get(FileMD fileMD) {
		for(DataSetProvider dataSetProvider : dsList){
			if(dataSetProvider.getFileMDList().contains(fileMD)){
				return dataSetProvider;
			}
		}
		return null;
	}

}
