package com.ovgu;

public class Main {

	public static void main(String args[]) {

		String indexLoc = "./index";
		String docLoc = "./";

		//get the document path from command line 
		if(args.length <= 0) {
			System.out.println ("ERROR :: invalid arguments : enter the path to the document directory");
			System.exit(0);
		}
		else if (args.length > 1) {
			System.out.println ("ERROR :: too many arguments : enter only the path to the document directory");
		}
		else {
			docLoc = args[0];
		}

		//call the indexer
		IndexFiles indexer = new IndexFiles(indexLoc,docLoc);

		//finish indexing
		indexer.generateIndex();

		//show query prompt
		SearchFiles searcher = new SearchFiles(indexLoc);

		try {
			searcher.querySearch();
		}
		catch (Exception e) {
			System.out.println("Error occured while searching : "+e);
		}

	}
}
