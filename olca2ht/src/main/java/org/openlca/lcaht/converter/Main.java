package org.openlca.lcaht.converter;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			printHelp();
			return;
		}
		String inFile = null;
		String outFile = null;
		for (String arg : args) {
			if (arg.startsWith("-in="))
				inFile = arg.substring(4).trim();
			if (arg.startsWith("-out="))
				outFile = arg.substring(5).trim();
		}
		if (!valid(inFile) || !valid(outFile))
			printHelp();
		else
			tryDoIt(inFile, outFile);
	}

	private static void tryDoIt(String inFile, String outFile) {
		File input = new File(inFile);
		File output = new File(outFile);
		if (!input.exists()) {
			System.err.println("File " + inFile + " does not exist.");
			return;
		}
		if (isZip(inFile) && isJson(outFile))
			new Zip2Json(input, output).run();
		else if (isJson(inFile) && isZip(outFile))
			new Json2Zip(input, output).run();
		else {
			System.err.println("Invalid file parameters");
			printHelp();
		}
	}

	private static boolean valid(String fileName) {
		return fileName != null && fileName.length() > 0;
	}

	private static boolean isZip(String fileName) {
		if (fileName == null)
			return false;
		return fileName.toLowerCase().endsWith(".zip");
	}

	private static boolean isJson(String fileName) {
		if (fileName == null)
			return false;
		String fn = fileName.toLowerCase();
		return fn.endsWith(".json") || fn.endsWith(".jsonld");
	}

	private static void printHelp() {
		System.out.println("olca2ht usage:");
		System.out.println("  java -jar olca2ht-X.X.jar -in=<input file>" +
				" -out=<output file>");
		System.out.println("  <input file>: *.zip, *.json, or *.jsonld file");
		System.out.println("  <output file>: *.zip, *.json, or *.jsonld file");
		System.out.println("  Converts a openLCA zip to a single json file");
		System.out.println("  or a LCA-HT json file to an openLCA zip file.");
	}
}
