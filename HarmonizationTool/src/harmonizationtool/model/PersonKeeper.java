package harmonizationtool.model;

import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import harmonizationtool.vocabulary.ECO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class PersonKeeper {
	private static List<Person> personList = new ArrayList<Person>();

	// protected final static Model model = ActiveTDB.model;

	private PersonKeeper() {
	}

	public static boolean add(Person person) {
		if (!personList.contains(person)){
			personList.add(person);
			return true;
		}
		return false;
	}

	public static boolean remove(Person person) {
		person.remove();
		return personList.remove(person);
	}

	public static List<Integer> getIDs() {
		List<Integer> ids = new ArrayList<Integer>();
		Iterator<Person> iterator = personList.iterator();
		while (iterator.hasNext()) {
			ids.add(personList.indexOf(iterator.next()));
		}
		return ids;
	}

	public static List<String> getNames() {
		List<String> results = new ArrayList<String>();
		Iterator<Person> iterator = personList.iterator();
		while (iterator.hasNext()) {
			results.add(iterator.next().getName());
		}
		Collections.sort(results);
		return results;
	}

	public static Person get(int index) {
		if (index < 0) {
			return null;
		}
		if (index >= personList.size()) {
			return null;
		}
		return personList.get(index);
	}

	public static boolean hasIndex(int index) {
		try {
			personList.get(index);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Integer indexOf(Person person) {
		return personList.indexOf(person);
	}

	public static int indexOfDataSourceName(String name) {
		Iterator<Person> iterator = personList.iterator();
		while (iterator.hasNext()) {
			Person person = (Person) iterator.next();
			if (person.getName().equals(name)) {
				return PersonKeeper.indexOf(person);
			}
		}
		return -1;
	}

	public static int size() {
		return personList.size();
	}

	public static Person getPersonByTdbResource(Resource tdbResource) {
		Iterator<Person> iterator = personList.iterator();
		while (iterator.hasNext()) {
			Person person = iterator.next();
			Resource resource = person.getTdbResource();
			if (resource.equals(tdbResource)) {
				return person;
			}
		}
		return null;
	}

	public static int getByTdbResource(Resource tdbResource) {
		Iterator<Person> iterator = personList.iterator();
		while (iterator.hasNext()) {
			Person person = iterator.next();
			Resource resource = person.getTdbResource();
			if (resource.equals(tdbResource)) {
				return personList.indexOf(person);
			}
		}
		return -1;
	}

	public static Person getByName(String name) {
		for (Person person : personList) {
			if (person.getName().equals(name)) {
				return person;
			}
		}
		return null;
	}

	public static List<Person> getPersonList() {
		return personList;
	}

	public static void setPersonList(List<Person> personList) {
		PersonKeeper.personList = personList;
	}

	public static void syncFromTDB() {
		ResIterator iterator = ActiveTDB.model.listSubjectsWithProperty(RDF.type, ECO.Person);
		while (iterator.hasNext()) {
			Resource personRDFResource = iterator.next();
			// NOW SEE IF THE Person IS IN THE PersonKeeper YET
			int personIndex = getByTdbResource(personRDFResource);
			if (personIndex < 0) {
				new Person(personRDFResource);
			}
		}
	}
}
