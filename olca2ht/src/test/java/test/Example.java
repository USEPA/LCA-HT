package test;

import java.io.File;
import org.openlca.lcaht.converter.Json2Zip;

public class Example {
	public static void main (String[] args) {
		File in = new File("examples/ht_out.jsonld");
		File out = new File("examples/olca_in.zip");
		if(out.exists())
			out.delete();
		new Json2Zip(in, out).run();
	}
}
