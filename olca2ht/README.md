olca2ht
=======
This is a small tool that converts zip files from the openLCA JSON-LD export
to a single JSON file that can be directly imported into the LCA-Harmonization
Tool and, the other way around, JSON-LD files from the Harmonization Tool to
openLCA JSON-LD packages that can be imported into openLCA.

Usage
-----
olca2ht is a Java command line tool and requires a
[Java Runtime Environment 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
to be installed. To use the tool type in a terminal:

	java -jar olca2ht-X.X.jar -in=<input file> -out=<output file>

This will take the input file and converts it to the output file. If the input
file is a zip file, it will create a single JSON file from all the JSON files
in the zip package. This can be then imported into the harmonization tool. If
the input is a json file, it will create a zip file with in a structure that
can be imported into openLCA.

License
-------
Unless stated otherwise, all source code of this project is licensed under the
[Mozilla Public License, v. 2.0](https://www.mozilla.org/MPL/2.0/).