package lucene;

public class Main {
	
	public static void main(String args[]) {
		
		String indexLoc = "./index5";
		String docLoc = "./";
		
		//get the document path
		
		if(args.length <= 0) {
			System.out.println ("ERROR :: invalid arguments : enter the path to the document directory");
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
