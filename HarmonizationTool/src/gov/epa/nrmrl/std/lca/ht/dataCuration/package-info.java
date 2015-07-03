/**
 * The 'Data Curation' package contains classes designed to help in the process of identifying and cross-referencing data items
 * in each of three levels:
 * 
 * 1) Automated - in which the tool finds finds matches which are above a given threshold indicating that the match
 * is certain enough the use need not confirm manually.
 * 
 * 2) Semi-automated - in which the tool finds candidates for matches for which evidence is not strong enough for
 * the user to accept the match without further inspection.
 * 
 * 3) Manual - in which the user must supply information to supplement that in the user's data file to help find
 * a match in the master data files. 
 * 
 * When two data items have a defined relationship, an instance of a ComparisonProvider is created and written to the TDB.
 * Each ComparisonProvider comparison is associated with an AnnotationProvider (which will often contain multiple Comparisons).
 *
 * @author Tom Transue
 *
 */
package gov.epa.nrmrl.std.lca.ht.dataCuration;