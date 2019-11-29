package com.ovgu;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchFiles {

	private String indexLoc = "./";
	SearchFiles(String indexLoc) {
		this.indexLoc = indexLoc;
	}

	/**
	 * This function searches for the query and displays the output.
	 * 
	 * @throws Exception
	 */
	public void querySearch() throws Exception {

		String index = indexLoc;
		index=this.indexLoc;
		String queries = null;
		boolean raw = false;
		int hitsPerPage = 15;

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);

		// Stemmer used to stem the query string 
		Analyzer analyzer = new CustomStemmerAnalyzer();

		BufferedReader in = null;
		in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		String[] fields = {"contents", "modified", "title"};
		MultiFieldQueryParser queryParser=new MultiFieldQueryParser(
				fields,
				analyzer);
		queryParser.setDefaultOperator(MultiFieldQueryParser.OR_OPERATOR);

		// prompt the user to enter a query
		while (true) {
			if (queries == null) {                        
				System.out.println("Enter query: ");
			}

			String line = in.readLine();

			line = line.trim();
			if (line.length() == 0) {
				System.out.println("Noting entered!");
				continue;
			}

			Query query = queryParser.parse(line);
			doPagingSearch(in, searcher, query, hitsPerPage, raw);

		}

	}

	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
			int hitsPerPage, boolean raw) throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 10 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = Math.toIntExact(results.totalHits.value);
		System.out.println(numTotalHits + " total matching documents\n");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (numTotalHits != 0) {


			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.format("| %-15s| %-75s| %-35s| %-15s|\n", "Rank", "Path to file", "Last modified", "Score");
			System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {

				Document doc = searcher.doc(hits[i].doc);

				String path = doc.get("path");
				String modified = doc.get("modified").toString();
				String title = doc.get("title");
				String summary = doc.get("summary");
				if (path != null) {

					float score = hits[i].score;
					System.out.format("| %-15s| %-75s| %-35s| %-15s|\n", (i+1), path, modified, Float.toString(score));

					if (doc.get("type").equals("html")) {
						System.out.println(">>\t\tTITLE   : "+ title);
						System.out.println(">>\t\tSUMMARY : "+ summary);
					}
				} else {
					System.out.println((i+1) + ". " + "No path for this document");
				}

			}

			System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.print("\n\nPress ENTER to get the next page of results\n\n\n");
			in.readLine();

			start+=hitsPerPage;
			if (start >= numTotalHits) {
				System.out.println("End of list reached\n\n");
				break;
			}
		}
	}
}

