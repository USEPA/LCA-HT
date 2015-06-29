The files in this folder include:
- electricity.zip an input file (such as might be produced by openLCA) in need of harmonization.
- variousOutputs.zip - containing the following (chronologically sequential files whose names explain)
  
  tdb_pre_data.json.jsonld
  tdb_pre_data.n3
  tdb_pre_commit_with_electricity.jsonld
  tdb_pre_commit_with_electricity.n3
  tdb_post_commit_with_electricity.jsonld
  tdb_post_commit_with_electricity.n3
  tdb_post_matching_2_flowables_1_context_with_electricity.jsonld
  tdb_post_matching_2_flowables_1_context_with_electricity.n3
  electricity_harmonized_2015-06-29a.json
  electricity_harmonized_2015-06-29a.ttl
  
  Note that the hamonization step is currently not working completely correctly, but here are some pointers:
  
  The original file contains 27 distinct Flows each with a different UUID (used as the IRI and assuming the olca namespace). 
  
  The .json is parsed into an import graph so that each item can have a triple added of the form ?s eco:hasDataSource ?datasetIRI
  where ?datasetIRI is a blank node containing information about the data set including references to file info and contact info.
  A query is then run to generate a table in which each row represents a flow, and columns represent fields associated with the Flow:
    1) UUID of the Flow
    2) Flowable Name
    3) Flowable CAS number
    4) Flowable formula
    5) Flow Context: General
    6) Flow Context: Specific
    7) Flow Property: Unit
    8) Flow Property: Property 
    
 The contents of this table are then checked for format issues.  Once none are found, the data are then re-parsed into RDF
 following the standards of the LCA-HT (which models Flowables separately, and Flow Properties differently).  During the
 parsing, the Flowables, Flow Contexts, and Flow Properties are matched automatically if criteria are sufficient.  Following
 parsing, the those that match all three are compared to master Flows.  In this version a "owl:sameAs" relationship is
 created, but soon, a separate instance of a "Comparison" will be made for each comparison.  The matching of Flow Contexts
 and Flow Properties also use "owl:sameAs" and will be changed to create "Comparison" instances.
  ---
  In the file: electricity_harmonized_2015-06-29a.json
  
  Lines 7739 to 7752 contain a "Flow" (with @id == IRI: olca:37236b2f-b18d-35a7-9860-d9149c1763f1)
  which has been harmonized to a master Flow (with UUID: 20185046-64bb-4c09-a8e7-e8a9e144ca98
  
  Lines 6446 to 6456 contain some information associated with the same Flow after the strings have been parsed