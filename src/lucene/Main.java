package lucene;

public class Main {
	
	public static void main(String args[]) {
		
		String indexLoc = "./index5";
		String docLoc = "D:\\testLuciene";
		//get the document path
		
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
