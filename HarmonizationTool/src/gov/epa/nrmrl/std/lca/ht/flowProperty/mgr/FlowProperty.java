package gov.epa.nrmrl.std.lca.ht.flowProperty.mgr;

import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataPropertyProvider;
import gov.epa.nrmrl.std.lca.ht.dataModels.LCADataValue;
import gov.epa.nrmrl.std.lca.ht.dataModels.QACheck;
import gov.epa.nrmrl.std.lca.ht.tdb.ActiveTDB;
import gov.epa.nrmrl.std.lca.ht.utils.RDFUtil;
import gov.epa.nrmrl.std.lca.ht.vocabulary.FedLCA;
import gov.epa.nrmrl.std.lca.ht.vocabulary.OpenLCA;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FlowProperty {
	// CLASS VARIABLES
	// public static final String flowPropertyPrimaryIdentifier = "Primary Info";
	public static final String flowPropertyUnit = "Unit";
	public static final String flowPropertyString = "Property";
	public static final String flowPropertyAdditionalIdentifier = "Additional Info";
	public static final Resource rdfClass = FedLCA.FlowProperty;
	// NOTE: EVENTUALLY label AND comment SHOULD COME FROM ONTOLOGY
	public static final String label = "Flow Property";
	public static final String comment = "The Flow Property is the characteristic used to measure the quanitity of the flowable.  Examples include 'volume', 'mass*time', and 'person transport'.  For a given Flow Property, only certain units are valid: e.g. 'm3' for 'volume', 'kg*hr' for 'mass*time', and 'people*km' for 'person transport'.";
	public static List<LCAUnit> lcaMasterUnits = new ArrayList<LCAUnit>();
	private static Map<String, LCADataPropertyProvider> dataPropertyMap;

	static {
		if (dataPropertyMap == null) {
			dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
			if (dataPropertyMap.isEmpty()) {

				ActiveTDB.tsReplaceLiteral(rdfClass, RDFS.label, label);
				ActiveTDB.tsAddGeneralTriple(rdfClass, RDFS.comment, comment, null);
				ActiveTDB.tsAddGeneralTriple(rdfClass, RDF.type, OWL.Class, null);

				System.out.println("label assigned to Flow Property");

				// dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
				// LCADataPropertyProvider lcaDataPropertyProvider;
				//
				// lcaDataPropertyProvider = new LCADataPropertyProvider(
				// flowPropertyPrimaryIdentifier);
				// lcaDataPropertyProvider.setPropertyClass(label);
				// lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				// lcaDataPropertyProvider.setRequired(true);
				// lcaDataPropertyProvider.setUnique(true);
				// lcaDataPropertyProvider.setLeftJustified(true);
				// lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				// lcaDataPropertyProvider
				// .setTDBProperty(FedLCA.flowPropertyPrimaryDescription);
				// dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(),
				// lcaDataPropertyProvider);

				dataPropertyMap = new LinkedHashMap<String, LCADataPropertyProvider>();
				LCADataPropertyProvider lcaDataPropertyProvider;

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyUnit);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(true);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyUnitString);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyString);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(true);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertyString);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				lcaDataPropertyProvider = new LCADataPropertyProvider(flowPropertyAdditionalIdentifier);
				lcaDataPropertyProvider.setPropertyClass(label);
				lcaDataPropertyProvider.setRDFDatatype(XSDDatatype.XSDstring);
				lcaDataPropertyProvider.setRequired(false);
				lcaDataPropertyProvider.setUnique(false);
				lcaDataPropertyProvider.setLeftJustified(true);
				lcaDataPropertyProvider.setCheckLists(getPropertyNameCheckList());
				lcaDataPropertyProvider.setTDBProperty(FedLCA.flowPropertySupplementalDescription);
				dataPropertyMap.put(lcaDataPropertyProvider.getPropertyName(), lcaDataPropertyProvider);

				// addUnit("currency 2000","14ed0060-9198-48c1-b458-7fa4075c9811",0.63,"Australian dollar","AUD","","EUR");
				// addUnit("currency 2000","18b04307-98b7-4d9e-82ee-f11d1b9f0d28",0.73,"Canadian dollar","CAD","","EUR");
				// addUnit("currency 2000","fd100082-4cdf-4932-9282-6bb7d7091bd4",0.02,"Czech coruna","CZK","","EUR");
				// addUnit("currency 2000","2f3cc326-da25-4086-b06d-3a7ef914aef6",0.13,"Danish krone","DKK","","EUR");
				// addUnit("currency 2000","0866bd06-77b3-41a2-8157-6d96ffb5666b",0.06,"Estonian kroon","EEK","","EUR");
				// addUnit("currency 2000","a55b7fe7-fa09-47ef-be62-e07b8e6f4351",1,"Euro","EUR","","EUR");
				// addUnit("currency 2000","fb4258af-95b2-4359-af03-9687178df527",0.0038,"Hungarian forint","HUF","","EUR");
				// addUnit("currency 2000","28bb8ff6-79d1-4f97-b7c4-7fa3dd05bec8",0.01,"Iceland krona","ISK","","EUR");
				// addUnit("currency 2000","6c277db7-2784-4296-83bf-15c04a9d59b2",0.01,"Japanese yen","JPY","","EUR");
				// addUnit("currency 2000","007f0ce1-4a0b-4bb2-8f7c-1b2a0e6ef6b2",1.78,"Latvian lats","LVL","","EUR");
				// addUnit("currency 2000","dce13106-087c-4fea-bfaa-def214e3cecb",0.27,"Lithuanian litas","LTL","","EUR");
				// addUnit("currency 2000","97ee3997-137d-4d96-80bf-c4f106c743c4",0.12,"Norwegian krone","NOK","","EUR");
				// addUnit("currency 2000","ae63f1fd-3679-443a-ad06-8d03e867351e",1.65,"Pound sterling","GBP","","EUR");
				// addUnit("currency 2000","71326000-165d-41fb-aea9-1bfbd9d7fc6c",0.15,"South African rand","ZAR","","EUR");
				// addUnit("currency 2000","d246fd48-c363-4f0e-ab01-641325dd89eb",0.00095,"South Korean won","KRW","","EUR");
				// addUnit("currency 2000","594fb96f-0d06-4053-9263-826fcd4d0046",0.12,"Swedish krona","SEK","","EUR");
				// addUnit("currency 2000","8632c804-eed7-4a57-91e6-1798289c49e3",0.64,"Swiss franc","CHF","","EUR");
				// addUnit("currency 2000","3f838c0d-bf6c-49f1-a465-d256ab0d13eb",1.1,"US dollar","USD","","EUR");
				addUnit("Mass", "20aadc24-a391-41cf-b340-3e4529f44bde", 1, "Kilogram", "kg", "", "kg", FedLCA.kg);
				addUnit("Mass", "0300ec69-ce1a-45f0-bcf0-7b33845dc53e", 0.45, "British pound (avoirdupois)", "lb av",
						"", "kg", FedLCA.lb);
				addUnit("Mass", "ae5cced9-f9f1-4719-bd37-837b36013d96", 0.0002, "Carat (metric)", "kt", "", "kg",
						FedLCA.kt);
				addUnit("Mass", "bdd34bd7-7d0b-4be4-9aca-b231f4067388", 0.0002, "Carat (metric)", "carat", "", "kg",
						FedLCA.carat);
				addUnit("Mass", "e1317ffc-7f83-4a85-bc65-4fb229a25cf8", 0.001, "Gram", "g", "", "kg", FedLCA.g);
				addUnit("Mass", "95dd50e9-c184-4412-9afc-9764b1ffcf8f", 1, "Kilogram SWU", "kg SWU", "", "kg",
						FedLCA.kg_SWU);
				addUnit("Mass", "e5ccc940-eb40-41f9-847e-5ee691ba8c2f", 1016.04, "Long ton", "long tn", "", "kg",
						FedLCA.long_ton);
				addUnit("Mass", "d022bf34-1162-4579-90ab-dcca9620ce54", 1000, "Megagram = 1 metric ton", "Mg", "",
						"kg", FedLCA.Mg);
				addUnit("Mass", "d01259b2-24c2-46af-a7fe-36dd025ead15", 0.000000001, "Microgram", "ug", "µg", "kg",
						FedLCA.ug);
				addUnit("Mass", "b872a063-0500-42b7-9e5d-441642d84417", 0.000001, "Milligram", "mg", "", "kg",
						FedLCA.mg);
				addUnit("Mass", "63214902-17d4-41a0-be10-d1cad375c32e", 0.000000000001, "Nanogram", "ng", "", "kg",
						FedLCA.ng);
				addUnit("Mass", "fa44e424-f37b-419d-8475-723429d63c08", 0.02,
						"Ounce (avoirdupois); commonly used, but NOT for gold, platinum etc. (see \"Ounce (troy)\")",
						"oz av", "", "kg", FedLCA.oz);
				addUnit("Mass", "2457aee5-c7ee-4416-a65a-5be4ff2b2976", 0.03, "Ounce (troy)", "oz t", "", "kg",
						FedLCA.oz_troy);
				addUnit("Mass", "68c83ad3-bc5b-414a-81f7-5b47af2c8a23", 0.000000000000001, "Picogram; 10^-12 g", "pg",
						"", "kg", FedLCA.pg);
				addUnit("Mass", "d4922696-9c95-4b5d-a876-425e98276978", 907.18, "Short ton", "sh tn", "", "kg",
						FedLCA.sh_ton);
				addUnit("Mass", "83192ffa-5990-490b-a23a-b45ca072db6f", 1000, "Ton", "t", "Mg", "kg", FedLCA.t);
				addUnit("Length", "3d314eab-ef11-4ff3-a35e-9bc5cd858694", 1, "Meter", "m", "", "m", FedLCA.m);
				addUnit("Length", "d018183e-ea8c-4a41-a303-627c9b9b173d", 0.01, "Centimeter", "cm", "", "m", FedLCA.cm);
				addUnit("Length", "f7905d3f-904d-4fdb-829e-54a72fb4c98e", 0.3, "Foot (international)", "ft", "", "m",
						FedLCA.ft);
				addUnit("Length", "030e99e1-2237-483c-8447-8b3a256d8b0d", 0.02, "Inch", "in", "", "m", FedLCA.in);
				addUnit("Length", "4a51d52b-f94f-4168-a38c-c612f14b8a2d", 1609.34, "International mile", "mi", "", "m",
						FedLCA.mi);
				addUnit("Length", "715ca68e-0ac5-4c4b-b557-fdc36623be88", 1000, "Kilometer", "km", "", "m", FedLCA.km);
				addUnit("Length", "9003179a-bf17-4e89-8fd7-a0c1c91ac189", 0.000001, "Micron", "u", "", "m", FedLCA.u);
				addUnit("Length", "cc44768b-c0ce-4bc9-804a-c59b67d22e39", 0.001, "Millimetre", "mm", "", "m", FedLCA.mm);
				addUnit("Length", "1f112940-6c27-4c75-828a-46d548d71cff", 1852, "Nautical mile", "nmi", "", "m",
						FedLCA.nmi);
				addUnit("Length", "a16960ba-31e2-4fe9-82fe-219013f4708f", 0.91, "Yard (international)", "yd", "", "m",
						FedLCA.yd);
				addUnit("Area", "3ce61faa-5716-41c1-aef6-b5920054acc9", 1, "Square meter", "m2", "m²", "m2", FedLCA.m2);
				addUnit("Area", "8ee3bcbf-9e65-4f59-9b0b-40b504cbe345", 4046.85, "Acre (US survey)", "ac", "acre (US)",
						"m2", FedLCA.ac);
				addUnit("Area", "e114cd83-3e7f-4466-8028-761c0469f7c7", 100, "Are", "a", "", "m2", FedLCA.a);
				addUnit("Area", "c66d26f1-7946-4027-85f0-ac79222a59f1", 0.09, "British square feet", "ft2", "ft²",
						"m2", FedLCA.ft2);
				addUnit("Area", "20ce5f57-69b1-438b-a8e8-23089854d058", 2589988.11, "British square mile", "mi2",
						"mi²", "m2", FedLCA.mi2);
				addUnit("Area", "debee4d9-bc47-4e35-8624-4957ecb75386", 10000, "Hectare", "ha", "", "m2", FedLCA.ha);
				addUnit("Area", "588613d5-6c8c-4ab6-8c69-ec5e20ef7881", 0.0001, "Square centimetre", "cm2", "", "m2",
						FedLCA.cm2);
				addUnit("Area", "b9a011a0-c9bf-459b-b105-2623d1b61ddf", 1000000, "Square kilometer", "km2", "km²",
						"m2", FedLCA.km2);
				addUnit("Area", "4689d28c-29f6-4f0a-ad03-0276f7070edd", 3429904, "Square nautical mile", "nmi2", "",
						"m2", FedLCA.nmi2);
				addUnit("Area", "6c4b1e4a-bf45-4385-a60c-12cc48ecbab5", 0.83, "Square yard (imperial/US)", "yd2", "",
						"m2", FedLCA.yd2);
				addUnit("Volume", "9bc2cfb2-fb43-42e6-9783-6c7479c78cce", 0.16, "Barrel (Imperial)", "bl (Imp)", "",
						"m3", FedLCA.m3);
				addUnit("Volume", "a681e9e5-6304-45f0-a5f4-df413eee0724", 0.11, "Barrel (US beer)", "bl (US beer)", "",
						"m3", FedLCA.bl_Imp);
				addUnit("Volume", "1c4169d1-4fee-40e3-9868-953ee15e26ae", 0.11, "Barrel (US dry)", "bl (US dry)", "",
						"m3", FedLCA.bl_US_beer);
				addUnit("Volume", "89121cea-0d98-4466-9b0f-e33e448db7ec", 0.11, "Barrel (US non-beer fluid)",
						"bl (US fl)", "", "m3", FedLCA.bl_US_dry);
				addUnit("Volume", "91995a9e-3cb4-4fc9-a93b-8c618ff9b948", 0.15, "Barrel (petroleum)", "bbl", "", "m3",
						FedLCA.bl_US_fl);
				addUnit("Volume", "07973a41-56b3-4e1b-a208-fd75a09fbd4b", 0.02, "Cubic feet", "cu ft", "", "m3",
						FedLCA.bbl);
				addUnit("Volume", "1c3a9695-398d-4b1f-b07e-a8715b610f70", 1, "Cubic meter", "m3", "m³", "m3",
						FedLCA.ft3);
				addUnit("Volume", "806c64aa-8609-4ff6-a033-6714a3348f24", 0.0000284130625, "Fluid ounce (Imperial)",
						"fl oz (Imp)", "", "m3", FedLCA.fl_oz_Imp);
				addUnit("Volume", "8aa31dda-e324-469c-a29d-56476584f5ca", 0.00454609,
						"Gallon (Imperial); used in UK, United Arab Emirates for fuels", "gal (Imp)", "", "m3",
						FedLCA.gal_Imp);
				addUnit("Volume", "c530708d-be50-4207-b536-23d0857210bf", 0.00440488377086, "Gallon (US dry)",
						"gal (US dry)", "", "m3", FedLCA.gal_US_dry);
				addUnit("Volume", "431df202-da83-4baa-b576-5d17f59f0c8a", 0.003785411784,
						"Gallon (US fluid); used in US e.g. for fuel", "gal (US fl)", "", "m3", FedLCA.gal_US_fl);
				addUnit("Volume", "62eb6f51-0574-4489-ae1a-1bd806f6c2ac", 0.003785412, "Gallon (US liquid)",
						"gal (US liq)", "", "m3", FedLCA.gal_US_liq);
				addUnit("Volume", "90888d8d-3d57-497f-a3fa-12aefd2bc774", 0.03, "Imperial bushel", "bsh (Imp)", "",
						"m3", FedLCA.bushel_Imp);
				addUnit("Volume", "b80a512e-e402-4363-8ad0-7d02dcf4a459", 0.001, "Liter", "l", "", "m3", FedLCA.l);
				addUnit("Volume", "df6987d7-afcc-4c92-ae2c-3ce3bc6f5578", 0.000000001, "Microlitre", "ul", "", "m3",
						FedLCA.micro_l);
				addUnit("Volume", "81696a66-6919-4bbb-a3ab-c79b2900de7d", 0.000001, "Milliliter", "ml", "", "m3",
						FedLCA.ml);
				addUnit("Volume", "4669b8ce-894d-405a-95d1-3211accaa739", 1, "Normal cubic meters", "Nm3", "Nm³", "m3",
						FedLCA.normal_m3);
				addUnit("Volume", "c026770b-5b4f-467d-8fdb-15dfb8e52913", 0.00056826125, "Pint (Imperial)", "pt (Imp)",
						"", "m3", FedLCA.pt_Imp);
				addUnit("Volume", "a19bcd84-e836-4764-8367-419dac3d2434", 0.0005506104713575, "Pint (US dry)",
						"pt (US dry)", "", "m3", FedLCA.pt_US_dry);
				addUnit("Volume", "2df86f1f-293e-4e07-9cde-34eebc6d2c5f", 0.000473176473, "Pint (US fluid)",
						"pt (US fl)", "", "m3", FedLCA.pt_US_fl);
				addUnit("Volume", "5ec65531-6bb4-4990-a687-44e9ab8f3529", 0.03, "US bushel", "bsh (US)", "", "m3",
						FedLCA.bsh_US);
				addUnit("Volume", "2d64c7e0-1167-42ec-9988-c441c261a35c", 0.0000295735295625,
						"US customary fluid ounce", "US fl oz", "", "m3", FedLCA.US_fl_oz);
				addUnit("Time", "11074cfd-08a4-449b-adad-18ce24a1b275", 1, "Day", "d", "", "d", FedLCA.day);
				addUnit("Time", "227a54d9-44e7-468c-b8bb-f2dd1ae68c7a", 0.04, "Hour", "h", "", "d", FedLCA.hour);
				addUnit("Time", "9fa94e47-03bd-4ad1-8726-e10cfb6dbb7a", 0.000694444444, "Minute", "min", "", "d",
						FedLCA.min);
				addUnit("Time", "845178d8-3f2c-497f-9ca6-3c5657c2823c", 0.000011574074, "Second", "s", "", "d",
						FedLCA.sec);
				addUnit("Time", "9a87f840-752d-4863-b911-533f92ee5073", 365, "Year (rounded)", "a", "", "d",
						FedLCA.year);
				addUnit("Energy", "55244053-94ba-404e-9172-cb279d905e00", 0.001055056,
						"British thermal unit (International table)", "btu", "", "MJ", FedLCA.MJ);
				addUnit("Energy", "01e58eb9-0aba-4c76-ba0c-03f6f3be1353", 1000, "Gigajoule", "GJ", "", "MJ", FedLCA.btu);
				addUnit("Energy", "469d61f1-3bc4-4841-8adf-873825c1bc11", 0.000001, "Joule", "J", "", "MJ", FedLCA.GJ);
				addUnit("Energy", "010f811e-3cc2-4b14-a901-337da9b3e49c", 0.0041867,
						"Kilocalorie (International table)", "kcal", "", "MJ", FedLCA.J);
				addUnit("Energy", "f4119718-2d50-47fe-9154-cab6fd2d30eb", 0.001, "Kilojoule", "kJ", "", "MJ",
						FedLCA.kcal);
				addUnit("Energy", "86ad2244-1f0e-4912-af53-7865283103e4", 3.6, "Kilowatt times hour", "kWh", "", "MJ",
						FedLCA.kJ);
				addUnit("Energy", "52765a6c-3896-43c2-b2f4-c679acf13efe", 1, "Megajoule", "MJ", "", "MJ", FedLCA.kWh);
				addUnit("Energy", "92e3bd49-8ed5-4885-9db6-fc88c7afcfcb", 3600, "Megawatt times hour", "MWh", "", "MJ",
						FedLCA.MWh);
				addUnit("Energy", "57c492e7-d94b-4fcc-98df-cdc4163b754c", 1000000, "Terajoule", "TJ", "", "MJ",
						FedLCA.TJ);
				addUnit("Energy", "787f2ac9-7bcd-4a91-9fab-55bfe414138f", 29307.6, "Ton coal equivalent", "TCE", "",
						"MJ", FedLCA.TCE);
				addUnit("Energy", "425aff51-b7e5-4561-aa5a-562ec103a79e", 41868, "Ton oil equivalent", "TOE", "", "MJ",
						FedLCA.TOE);
				addUnit("Energy", "fc3604f7-aa93-4aa3-8ae9-8f822874da5f", 0.0036, "Watt times hour", "Wh", "", "MJ",
						FedLCA.Wh);
				addUnit("Radioactivity", "ac324d87-9961-463a-81a1-099bb0f7d89b", 0.001, "Bequerel, 1 event per second",
						"Bq", "", "kBq", FedLCA.kBq);
				addUnit("Radioactivity", "ef6d9358-a156-4c73-b678-320ddee7d2eb", 37000000, "Curie", "Ci", "", "kBq",
						FedLCA.Bq);
				addUnit("Radioactivity", "e9773595-284e-46dd-9671-5fc9ff406833", 1,
						"Kilo-Bequerel, 1000 events per second", "kBq", "", "kBq", FedLCA.Ci);
				addUnit("Radioactivity", "a45722ee-fc30-4bb1-aa95-5c8fb56c6bfb", 1000, "Rutherford", "Rutherford", "",
						"kBq", FedLCA.Rutherford);

				addUnit("Mass*time", "b2ad404c-3e4f-4a7a-a604-46fb36654823", 1, "Kilogram times year", "kg*a", "",
						"kg*a", FedLCA.kg_year);
				addUnit("Mass*time", "2fdc0039-6c5e-489a-af77-b225684fa337", 0.001, "Gram times year", "g*a", "",
						"kg*a", FedLCA.g_year);
				addUnit("Mass*time", "ab01fe47-d2c0-4308-adb1-e32df38d5a50", 0.002739726,
						"Kilogram times day (1 year = 365 days)", "kg*d", "", "kg*a", FedLCA.kg_day);
				addUnit("Mass*time", "5209be3d-094e-4203-a074-06d7b75e9a38", 2.73,
						"Metric ton times day (1 year = 365 days)", "t*d", "", "kg*a", FedLCA.ton_day);
				addUnit("Mass*time", "23cc7a32-f08a-43df-a87c-66953eeeb3f5", 1000, "Metric tonnes times year", "t*a",
						"", "kg*a", FedLCA.ton_year);
				addUnit("Mass*length", "0dea4ed8-bb6b-4049-b2b4-b2c413ef2180", 1, "Metric ton-kilometer", "t*km",
						"tkm", "t*km", FedLCA.ton_km);
				addUnit("Mass*length", "5458351a-f6f7-4e0b-a449-823c9b6374db", 0.000729986,
						"British pound (avoirdupois) times international mile", "lb*mi", "", "t*km", FedLCA.lb_mi);
				addUnit("Mass*length", "d24270f0-c1f2-49ec-a6bf-e86a74177070", 0.000840053,
						"British pound (avoirdupois) times nautical mile", "lb*nmi", "", "t*km", FedLCA.lb_nautical_mi);
				addUnit("Mass*length", "a40229e6-7275-42e3-a304-23d590044770", 0.001, "Kilogram-kilometer", "kg*km",
						"kgkm", "t*km", FedLCA.kg_km);
				addUnit("Mass*length", "3cc51ae9-b993-4a3d-964e-0aed8d7f3966", 1.6,
						"Metric ton times international mile", "t*mi", "", "t*km", FedLCA.ton_mi);
				addUnit("Mass*length", "7dd57df4-c092-41aa-966e-c93a27797ea1", 1.85, "Metric ton times nautical mile",
						"t*nmi", "", "t*km", FedLCA.ton_nautical_mi);
				addUnit("Length*time", "f7fe0af2-e764-4984-bb9f-2cbff6cd2f18", 1, "Meter times year", "m*a", "ma",
						"m*a", FedLCA.m_year);
				addUnit("Area*time", "c7266b67-4ea2-457f-b391-9b94e26e195a", 1, "Square meter times year", "m2*a",
						"m²*a;m2a;m²a", "m2*a", FedLCA.m2_year);
				addUnit("Area*time", "efbbab8b-eb92-4e39-bd5f-99951ffda6c3", 0.09, "British square feet times year",
						"ft2*a", "ft²*a;ft²a;ft2a", "m2*a", FedLCA.ft2_year);
				addUnit("Area*time", "1c43f336-c84b-4f42-bbf7-b1b6f89e121a", 2589988.1,
						"British square mile times year", "mi2*a", "mi²*a;mi2a;mi²a", "m2*a", FedLCA.mi2_year);
				addUnit("Area*time", "4866ec7b-6218-4783-87b4-cbd107280a85", 10000, "Hectare times year", "ha*a", "",
						"m2*a", FedLCA.ha_year);
				addUnit("Area*time", "c8166ae2-b592-4eb9-b365-e384c8b79f3c", 1000000, "Square kilometer times year",
						"km2*a", "km²*a;km2a;km²a", "m2*a", FedLCA.km2_year);
				addUnit("Area*time", "00d8370e-2bf1-4f3b-81bb-f8f147e84819", 0.002739726, "Square metre times day",
						"m2*d", "", "m2*a", FedLCA.m2_day);
				addUnit("Volume*time", "ee5f2241-18af-4444-b457-b275660e5a20", 1, "Cubic meter times year", "m3*a",
						"m³*a;m3a", "m3*a", FedLCA.m3_year);
				addUnit("Volume*time", "f3a1ae74-9750-4199-acdc-2e7e0546e0a5", 0.002739726, "Cubic meter times day",
						"m3*d", "m³*d;m3d", "m3*a", FedLCA.m3_day);
				addUnit("Volume*time", "9942703a-5962-4823-8ea3-7af06af9a21e", 0.000002739726, "Liter times day",
						"l*d", "", "m3*a", FedLCA.l_day);
				addUnit("Volume*time", "8c1fafa2-2b2e-4fef-9581-5de34ae87350", 0.001, "Liter times year", "l*a", "",
						"m3*a", FedLCA.l_year);
				addUnit("Volume*length", "5042a5e9-b8fd-40ca-b13e-cb9f1ce0357a", 1, "Cubic metre times kilometre",
						"m3*km", "", "m3*km", FedLCA.m3_km);
				addUnit("Volume*length", "de900fea-2536-4fe5-a92d-00ac9a28654b", 1.6,
						"Cubic metre times international mile", "m3*mi", "", "m3*km", FedLCA.m3_mi);
				addUnit("Volume*length", "fad23f5c-f841-4369-ba17-2dd297b5fadf", 1.85,
						"Cubic metre times nautical mile", "m3*nmi", "", "m3*km", FedLCA.m3_nautical_mi);
				addUnit("Volume*length", "112e9d4a-262d-4dcb-8003-c19204faedfb", 0.001609344,
						"Litre times international mile", "l*mi", "", "m3*km", FedLCA.l_mi);
				addUnit("Volume*length", "a0805221-ed1e-4177-9e2a-4b72ce2beb06", 0.001, "Litre times kilometre",
						"l*km", "", "m3*km", FedLCA.l_km);
				addUnit("Volume*length", "0c62f887-da22-4b0a-999b-145c4b2ffe1e", 0.001852, "Litre times nautical mile",
						"l*nmi", "", "m3*km", FedLCA.l_nautical_mi);
				addUnit("Energy/mass*time", "94dcd768-ea3e-47da-ba1b-11fea4b5f4cc", 1,
						"Megajoule per kilogram times day", "MJ/kg*d", "", "MJ/kg*d", FedLCA.MJ_per_kg_day);
				addUnit("Energy/area*time", "aff60d84-007c-4f30-bfda-3853760f6954", 1,
						"Kilowatthour per square meter times day", "kWh/m2*d", "kWh/m2*d;kWh/m2d;kWh/m²d", "kWh/m2*d",
						FedLCA.kWh_per_m2_day);

				addUnit("Number of items", "6dabe201-aaac-4509-92f0-d00c26cb72ab", 1, "Number of items", "Item(s)",
						"unit;LU;pig place", "Item(s)", FedLCA.Item);
				addUnit("Number of items", "3bd6c6c3-bb61-46f3-b19a-c87ac5502bb7", 12, "Dozen(s) of items", "Dozen(s)",
						"", "Item(s)", FedLCA.DozenItems);
				addUnit("Items*length", "2abb86b6-e71b-4de5-a766-a20e80e59b6d", 1, "Items times kilometre", "Items*km",
						"", "Items*km", FedLCA.items_km);
				addUnit("Items*length", "ce39138f-55f8-47bc-b55a-66027fc836d9", 1.6, "Items times international mile",
						"Items*mi", "", "Items*km", FedLCA.items_mi);
				addUnit("Items*length", "9bf0166c-fa76-47d0-95af-054ea9125f2c", 1.85, "Items times nautical mile",
						"Items*nmi", "", "Items*km", FedLCA.items_nautical_mi);
				addUnit("Person transport", "fe8da65d-f0ea-4496-b13e-1955aaa412d7", 1, "Person kilometer", "p*km",
						"pkm", "p*km", FedLCA.person_km);
				addUnit("Vehicle transport", "19a89180-e40c-4f6b-bcd3-d7347566d1e7", 1, "Vehicle-kilometer", "v*km",
						"vkm", "v*km", FedLCA.vehicle_km);

				// addUnit("Currency 2000","14ed0060-9198-48c1-b458-7fa4075c9811",0.63,"Australian dollar","AUD","","EUR");
				// addUnit("Currency 2000","18b04307-98b7-4d9e-82ee-f11d1b9f0d28",0.73,"Canadian dollar","CAD","","EUR");
				// addUnit("Currency 2000","fd100082-4cdf-4932-9282-6bb7d7091bd4",0.02,"Czech coruna","CZK","","EUR");
				// addUnit("Currency 2000","2f3cc326-da25-4086-b06d-3a7ef914aef6",0.13,"Danish krone","DKK","","EUR");
				// addUnit("Currency 2000","0866bd06-77b3-41a2-8157-6d96ffb5666b",0.06,"Estonian kroon","EEK","","EUR");
				// addUnit("Currency 2000","a55b7fe7-fa09-47ef-be62-e07b8e6f4351",1,"Euro","EUR","","EUR");
				// addUnit("Currency 2000","fb4258af-95b2-4359-af03-9687178df527",0.0038,"Hungarian forint","HUF","","EUR");
				// addUnit("Currency 2000","28bb8ff6-79d1-4f97-b7c4-7fa3dd05bec8",0.01,"Iceland krona","ISK","","EUR");
				// addUnit("Currency 2000","6c277db7-2784-4296-83bf-15c04a9d59b2",0.01,"Japanese yen","JPY","","EUR");
				// addUnit("Currency 2000","007f0ce1-4a0b-4bb2-8f7c-1b2a0e6ef6b2",1.78,"Latvian lats","LVL","","EUR");
				// addUnit("Currency 2000","dce13106-087c-4fea-bfaa-def214e3cecb",0.27,"Lithuanian litas","LTL","","EUR");
				// addUnit("Currency 2000","97ee3997-137d-4d96-80bf-c4f106c743c4",0.12,"Norwegian krone","NOK","","EUR");
				// addUnit("Currency 2000","ae63f1fd-3679-443a-ad06-8d03e867351e",1.65,"Pound sterling","GBP","","EUR");
				// addUnit("Currency 2000","71326000-165d-41fb-aea9-1bfbd9d7fc6c",0.15,"South African rand","ZAR","","EUR");
				// addUnit("Currency 2000","d246fd48-c363-4f0e-ab01-641325dd89eb",0.00095,"South Korean won","KRW","","EUR");
				// addUnit("Currency 2000","594fb96f-0d06-4053-9263-826fcd4d0046",0.12,"Swedish krona","SEK","","EUR");
				// addUnit("Currency 2000","8632c804-eed7-4a57-91e6-1798289c49e3",0.64,"Swiss franc","CHF","","EUR");
				// addUnit("Currency 2000","3f838c0d-bf6c-49f1-a465-d256ab0d13eb",1.1,"US dollar","USD","","EUR");
				//
				//
				// addUnit("Mole*area*time/mass","2e16ca4a-9f65-472f-b4cb-274050aaf328",1,"Centimole times square metre times year per kilogram","(cmol*m2*a)/kg","","(cmol*m2*a)/kg");
				// addUnit("Mechanical filtration (occ.)","2f1b55fb-a432-447a-a0f5-9b02bf649724",1,"Centimeter times cubic meter","cm*m3","","cm*m3");
				// addUnit("Mole*area/mass","691ae73c-3fc1-4157-85d0-2659bdc2380a",1,"Centimole times square metre per kilogram","(cmol*m2)/kg","","(cmol*m2)/kg");
				// addUnit("Mass/time","94b84332-8f2d-4592-b2a0-e19da33a69e9",1,"Kilogram per year","kg/a","","kg/a");
				// addUnit("Groundwater replenishment (transf.)","95e8feec-abc7-4eb0-bf39-2a6d411cba8d",1,"Millimetre times square metre per year","(mm*m2)/a","","(mm*m2)/a");
				// addUnit("Length*area/time","aa14a795-2239-496a-81b0-ad7cb8bbe0d2",1,"Centimetre times square metre per day","cm*m2/d","","cm*m2/d");
				// addUnit("Length*area","0dc79b8e-47a1-4ec7-96b1-c9b9da2769fa",1,"Millimetre times square metre","mm*m2","","mm*m2");
			}
		}
	}

	// INSTANCE VARIABLES
	private Resource tdbResource;
	private List<LCADataValue> lcaDataValues;
	private Resource matchingResource;
	private int firstRow;

	// CONSTRUCTORS
	public FlowProperty() {
		this.tdbResource = ActiveTDB.tsCreateResource(rdfClass);
		lcaDataValues = new ArrayList<LCADataValue>();
		matchingResource = null;
	}

	// private static void addUnit(String unitGroup, String uuid, double conversionFactor, String description,
	// String name, String synonyms, String referenceUnit) {
	// LCAUnit lcaUnit = new LCAUnit();
	// lcaUnit.conversionFactor = conversionFactor;
	// lcaUnit.description = description;
	// lcaUnit.name = name;
	// lcaUnit.referenceUnit = referenceUnit;
	// lcaUnit.synonyms = synonyms;
	// lcaUnit.unit_group = unitGroup;
	// lcaUnit.uuid = uuid;
	// lcaUnit.tdbResource = null;
	// lcaMasterUnits.add(lcaUnit);
	// }

	private static void addUnit(String unitGroup, String uuid, double conversionFactor, String description,
			String name, String synonyms, String referenceUnit, Resource tdbResource) {
		LCAUnit lcaUnit = new LCAUnit();
		lcaUnit.conversionFactor = conversionFactor;
		lcaUnit.description = description;
		lcaUnit.name = name;
		lcaUnit.referenceUnit = referenceUnit;
		lcaUnit.synonyms = synonyms;
		lcaUnit.unit_group = unitGroup;
		lcaUnit.uuid = uuid;
		lcaUnit.tdbResource = tdbResource;

		ActiveTDB.tsAddGeneralTriple(tdbResource, RDF.type, OpenLCA.FlowProperty, null);

		ActiveTDB.tsAddGeneralTriple(tdbResource, DCTerms.description, description, null);
		ActiveTDB.tsAddGeneralTriple(tdbResource, OpenLCA.description, description, null);

		ActiveTDB.tsAddGeneralTriple(tdbResource, FedLCA.hasOpenLCAUUID, uuid, null);
		lcaMasterUnits.add(lcaUnit);
	}

	public FlowProperty(Resource tdbResource) {
		this.tdbResource = tdbResource;
		lcaDataValues = new ArrayList<LCADataValue>();
		clearSyncDataFromTDB();
	}

	public Object getOneProperty(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				return lcaDataValue.getValue();
			}
		}
		return null;
	}

	public Object[] getAllProperties(String key) {
		List<Object> resultList = new ArrayList<Object>();
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				resultList.add(lcaDataValue.getValue());
			}
		}
		Object[] results = new Object[resultList.size()];
		if (resultList.size() == 0) {
			return null;
		}
		for (int i = 0; i < resultList.size(); i++) {
			results[i] = resultList.get(i);
		}
		return results;
	}

	public List<LCADataValue> getPropertyValuesInOrder() {
		List<LCADataValue> results = new ArrayList<LCADataValue>();
		for (String key : dataPropertyMap.keySet()) {
			for (LCADataValue lcaDataValue : lcaDataValues) {
				if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
					results.add(lcaDataValue);
				}
			}
		}
		return results;
	}

	public void setProperty(String key, Object object) {
		if (object == null) {
			return;
		}
		if (!dataPropertyMap.containsKey(key)) {
			return;
		}
		LCADataPropertyProvider lcaDataPropertyProvider = dataPropertyMap.get(key);
		RDFDatatype rdfDatatype = lcaDataPropertyProvider.getRdfDatatype();
		Class<?> objectClass = RDFUtil.getJavaClassFromRDFDatatype(rdfDatatype);
		if (!objectClass.equals(object.getClass())) {
			return;
		}
		LCADataValue newLCADataValue = new LCADataValue();
		newLCADataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
		newLCADataValue.setValue(object);
		// newLCADataValue.setValueAsString(object.toString()); // SHOULD WE DO
		// THIS AT ALL?

		if (lcaDataPropertyProvider.isUnique()) {
			removeValues(lcaDataPropertyProvider.getPropertyName());
			ActiveTDB.tsReplaceLiteral(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object);
		} else {
			ActiveTDB.tsAddGeneralTriple(tdbResource, lcaDataPropertyProvider.getTDBProperty(), object, null);
		}
		lcaDataValues.add(newLCADataValue);
	}

	private void removeValues(String key) {
		for (LCADataValue lcaDataValue : lcaDataValues) {
			if (lcaDataValue.getLcaDataPropertyProvider().getPropertyName().equals(key)) {
				lcaDataValues.remove(lcaDataValue);
			}
		}
	}

	public void updateSyncDataFromTDB() {
		if (tdbResource == null) {
			return;
		}
		// LCADataPropertyProvider LIST IS ALL LITERALS
		for (LCADataPropertyProvider lcaDataPropertyProvider : dataPropertyMap.values()) {
			if (!tdbResource.hasProperty(lcaDataPropertyProvider.getTDBProperty())) {
				continue;
			}
			if (lcaDataPropertyProvider.isUnique()) {
				removeValues(lcaDataPropertyProvider.getPropertyName());
				Object value = tdbResource.getProperty(lcaDataPropertyProvider.getTDBProperty()).getLiteral()
						.getValue();
				if (value.getClass().equals(
						RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
					LCADataValue lcaDataValue = new LCADataValue();
					lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
					lcaDataValue.setValue(value);
					lcaDataValues.add(lcaDataValue);
				}
			} else {
				StmtIterator stmtIterator = tdbResource.listProperties(lcaDataPropertyProvider.getTDBProperty());
				while (stmtIterator.hasNext()) {
					Object value = stmtIterator.nextStatement().getLiteral().getValue();
					if (value.getClass().equals(
							RDFUtil.getJavaClassFromRDFDatatype(lcaDataPropertyProvider.getRdfDatatype()))) {
						LCADataValue lcaDataValue = new LCADataValue();
						lcaDataValue.setLcaDataPropertyProvider(lcaDataPropertyProvider);
						lcaDataValue.setValue(value);
						lcaDataValues.add(lcaDataValue);
					}
				}
			}
		}
		// --- BEGIN SAFE -READ- TRANSACTION ---
		ActiveTDB.tdbDataset.begin(ReadWrite.READ);
		ResIterator resIterator = ActiveTDB.getModel(null).listSubjectsWithProperty(FedLCA.comparedSource, tdbResource);
		// ActiveTDB.tdbDataset.end();
		if (resIterator.hasNext()) {
			matchingResource = resIterator.next();
			ActiveTDB.tdbDataset.end();
			return;
		}
		ActiveTDB.tdbDataset.end();
	}

	public void clearSyncDataFromTDB() {
		lcaDataValues.clear();
		updateSyncDataFromTDB();
	}

	// public static final CSVColumnInfo[] getHeaderMenuObjects() {
	// CSVColumnInfo[] results = new CSVColumnInfo[2];
	//
	// results[0] = new CSVColumnInfo("Property (primary)");
	// results[0].setRequired(true);
	// results[0].setUnique(true);
	// results[0].setCheckLists(getPropertyNameCheckList());
	// results[0].setLeftJustified(true);
	// results[0].setRDFClass(rdfClass);
	// results[0].setTdbProperty(FedLCA.flowPropertyPrimaryDescription);
	// results[0].setRdfDatatype(XSDDatatype.XSDstring);
	//
	// results[1] = new CSVColumnInfo("Property (additional)");
	// results[1].setRequired(false);
	// results[1].setUnique(false);
	// results[1].setCheckLists(getPropertyNameCheckList());
	// results[1].setLeftJustified(true);
	// results[1].setRDFClass(rdfClass);
	// results[1].setTdbProperty(FedLCA.flowPropertySupplementalDescription);
	// results[1].setRdfDatatype(XSDDatatype.XSDstring);
	// return results;
	// }

	private static List<QACheck> getPropertyNameCheckList() {
		List<QACheck> qaChecks = QACheck.getGeneralQAChecks();

		// String d1 = "Non-allowed characters";
		// String e1 =
		// "Various characters are not considered acceptible in standard chemical names.";
		// String s1 = "Check your data";
		// Pattern p1 = Pattern.compile("^([^\"]+)[\"]([^\"]+)$");
		// String r1 = null;
		//
		// qaChecks.add(new QACheck(d1, e1, s1, p1, r1, false));
		return qaChecks;
	}

	// public String getPrimaryFlowProperty() {
	// return primaryFlowProperty;
	// }
	//
	// public void setPrimaryFlowProperty(String primaryFlowProperty) {
	// this.primaryFlowProperty = primaryFlowProperty;
	// RDFDatatype rdfDatatype = getHeaderMenuObjects()[0].getRdfDatatype();
	// ActiveTDB.tsReplaceLiteral(tdbResource,
	// FedLCA.flowPropertyPrimaryDescription, rdfDatatype, primaryFlowProperty);
	// }
	//
	// public List<String> getsupplementaryFlowProperties() {
	// return supplementaryFlowProperties;
	// }
	//
	// public void setSupplementaryFlowProperties(List<String>
	// supplementaryFlowProperties) {
	// ActiveTDB.tsRemoveAllObjects(tdbResource,
	// FedLCA.flowPropertySupplementalDescription);
	// this.supplementaryFlowProperties = supplementaryFlowProperties;
	// for (String supplementaryFlowProperty : supplementaryFlowProperties) {
	// ActiveTDB.tsAddLiteral(tdbResource,
	// FedLCA.flowPropertySupplementalDescription, supplementaryFlowProperty);
	// }
	// }

	// public void addSupplementaryFlowProperty(String
	// supplementaryFlowProperty) {
	// if (supplementaryFlowProperties == null) {
	// supplementaryFlowProperties = new ArrayList<String>();
	// }
	// supplementaryFlowProperties.add(supplementaryFlowProperty);
	// ActiveTDB.tsAddLiteral(tdbResource,
	// FedLCA.flowPropertySupplementalDescription, supplementaryFlowProperty);
	// }
	//
	// public void removeSupplementaryFlowProperty(String
	// supplementaryFlowProperty) {
	// this.supplementaryFlowProperties.remove(supplementaryFlowProperty);
	// Literal literalToRemove =
	// ActiveTDB.tsCreateTypedLiteral(supplementaryFlowProperty);
	// ActiveTDB.tsRemoveStatement(tdbResource,
	// FedLCA.flowPropertySupplementalDescription, literalToRemove);
	// }

	public Resource getTdbResource() {
		return tdbResource;
	}

	public void setTdbResource(Resource tdbResource) {
		// StmtIterator stmtIterator = this.tdbResource.listProperties();
		// while (stmtIterator.hasNext()){
		// Statement statement = stmtIterator.nextStatement();
		// ActiveTDB.tdbModel.remove(statement);
		// }
		// NEXT STATEMENT REPLACES ABOVE
		this.tdbResource.removeProperties();
		this.tdbResource = tdbResource;
	}

	public static Resource getRdfclass() {
		return rdfClass;
	}

	public static Map<String, LCADataPropertyProvider> getDataPropertyMap() {
		return dataPropertyMap;
	}

	public Resource getMatchingResource() {
		return matchingResource;
	}

	public void setMatchingResource(Resource matchingResource) {
		this.matchingResource = matchingResource;
		ActiveTDB.tsReplaceResourceSameType(tdbResource, OWL.sameAs, matchingResource, null);
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public String getDataSource() {
		return "Master List";
	}

	public boolean setMatches() {
		String unitStr = (String) getOneProperty(flowPropertyUnit);

		if (unitStr == null) {
			return false;
		}
		for (LCAUnit lcaUnit : lcaMasterUnits) {
			if (lcaUnit.name.equals(unitStr)) {
				setMatchingResource(lcaUnit.tdbResource);
				// matchingResource = lcaUnit.tdbResource;
				return true;
			}
		}
		return false;
	}

	public String getUnitStr() {
		return (String) getOneProperty(flowPropertyUnit);
	}

	public String getPropertyStr() {
		return (String) getOneProperty(flowPropertyString);
	}
}
