package harmonizationtool.comands;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import harmonizationtool.Activator;
import harmonizationtool.QueryView;
import harmonizationtool.ViewData;
import harmonizationtool.model.CuratorMD;
import harmonizationtool.model.DataSetKeeper;
import harmonizationtool.model.DataSetMD;
import harmonizationtool.model.DataSetProvider;
import harmonizationtool.utils.Util;
import harmonizationtool.vocabulary.ECO;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping.Factory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

//import com.hp.hpl.jena.query.Dataset;
//import com.hp.hpl.jena.query.Query;
//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.Literal;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.rdf.model.Resource;
//import com.hp.hpl.jena.tdb.TDBFactory;

public class SelectTDB implements IHandler, ISelectedTDB {
	public static Model model = null;
	public static Dataset dataset = null;
	public static String tdbDir = null;
	public static GraphStore graphStore = null;
	private static SelectTDB instance = null;
	private List<ISelectedTDBListener> selectedTDBListeners = new ArrayList<ISelectedTDBListener>();

	public SelectTDB() {
		System.out.println("created SelectTDB");
		instance = this;
	}

	public static SelectTDB getInstance() {
		return instance;
	}

	public void finalize() {
		System.out.println("closing dataset and model");
		cleanUp();
	}

	private void cleanUp() {
		try {
			dataset.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			model.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			graphStore.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		System.out.println("added listener: " + handlerListener);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		openTDB();
		updateStatusLine();
		syncTDB_to_DataSetKeeper();

		return null;
	}

	private void updateStatusLine() {
		String msg;
		msg = "Using TDB: " + Util.getPreferenceStore().getString("defaultTDB");
		Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}

	private void openTDB() {
		String msg = "Opening TDB: " + Util.getPreferenceStore().getString("defaultTDB");
		Util.findView(QueryView.ID).getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
		String defaultTDB = Util.getPreferenceStore().getString("defaultTDB");
		File defaultTDBFile = new File(defaultTDB);
		if (defaultTDBFile.isDirectory()) {
			System.out.println("defaultTDBFile.list().length=" + defaultTDBFile.list().length);
			try {
				dataset = TDBFactory.createDataset(defaultTDBFile.getPath());
				assert dataset != null : "dataset cannot be null";
				model = dataset.getDefaultModel();
				graphStore = GraphStoreFactory.create(dataset); // FIXME DO WE
																// NEED

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Because the DataSetKeeper does not contain DataSets, but the TDB might, we need to get
	 * DataSet info from the TDB Each TDB subject which is rdf:type eco:DataSource should have a
	 * DataSetProvider NOTE: eco:DataSource should change to lcaht:DataSet
	 */
	public void syncTDB_to_DataSetKeeper() {
		if (model == null) {
			openTDB();
		}
		assert model != null : "model should not be null";
		ResIterator iterator = model.listSubjectsWithProperty(RDF.type, ECO.DataSource);

		while (iterator.hasNext()) {
			System.out.println("got another...");
			Resource subject = (Resource) iterator.next();

			if (DataSetKeeper.getByTdbResource(subject) == -1) {
				DataSetProvider dataSetProvider = new DataSetProvider();
				dataSetProvider.setTdbResource(subject);
				DataSetMD dataSetMD = new DataSetMD();
				CuratorMD curatorMD = new CuratorMD();
				if (model.contains(subject, RDFS.label)) {
					NodeIterator iter2 = model.listObjectsOfProperty(subject, RDFS.label);
					RDFNode node = iter2.next();
					dataSetMD.setName(node.asLiteral().getString());
					System.out.println("Adding name: " + node.asLiteral().getString() + " for subject: " + subject.getURI());
				}
				if (model.contains(subject, RDFS.comment)) {
					NodeIterator iter2 = model.listObjectsOfProperty(subject, RDFS.comment);
					RDFNode node = iter2.next();
					dataSetMD.setComments(node.asLiteral().getString());
					System.out.println("Adding comment: " + node.asLiteral().getString() + " for subject: " + subject.getURI());

				}
				String version = "";
				if (model.contains(subject, DCTerms.hasVersion)) {
					version = model.listObjectsOfProperty(subject, DCTerms.hasVersion).next().asLiteral().getString();
				} else if (model.contains(subject, ECO.hasMajorVersionNumber)) {
					version = model.listObjectsOfProperty(subject, ECO.hasMajorVersionNumber).next().asLiteral().getString();
					if (model.contains(subject, ECO.hasMinorVersionNumber)) {
						version += "." + model.listObjectsOfProperty(subject, ECO.hasMinorVersionNumber).next().asLiteral().getString();
					}
					model.add(subject, DCTerms.hasVersion, version); //	ADDING VERSION INFO
				}
				dataSetMD.setVersion(version);
				System.out.println("Adding version: " + version + " for subject: " + subject.getURI());

				dataSetProvider.setDataSetMD(dataSetMD);
				dataSetProvider.setCuratorMD(curatorMD);
				DataSetKeeper.add(dataSetProvider);
			} else {
				System.out.println("Id for " + subject.getURI() + " = " + DataSetKeeper.getByTdbResource(subject));
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSelectedTDBListener(ISelectedTDBListener listener) {
		System.out.println("Added TDBListener = " + listener);
		selectedTDBListeners.add(listener);
	}

	@Override
	public void removeSelectedTDBListener(ISelectedTDBListener listener) {
		// TODO Auto-generated method stub

	}

}
