package com.ovgu;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Element;
import org.w3c.tidy.Tidy;

public class IndexFiles {

	private String indexLoc = "./";
	private String docsLoc  = "./";

	IndexFiles(String indexLoc, String docsLoc) {
		this.indexLoc = indexLoc;
		this.docsLoc = docsLoc;
	}

	private  static  String[] allowedFiles= {"txt","html","htm"};

	public int generateIndex() {

		String docsPath=this.docsLoc;
		String indexPath=this.indexLoc;

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			
			// CustomStemmerAnalyzer stems the words in the document before adding them to the index 
			
			IndexWriterConfig iwc = new IndexWriterConfig(new CustomStemmerAnalyzer());

			iwc.setOpenMode(OpenMode.CREATE);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			// IGNORE THIS !!
			// NOTE: if you want to maximize search performance,
			//
			// writer.forceMerge(1);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * This part of the code indexes the documents 
	 * 
	 * @param writer
	 * @param path
	 * @throws IOException
	 */
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}
	
	
	
	static Boolean IsTextOrHtmlFile(String FilePath) {
		return FilePath.toLowerCase().endsWith(allowedFiles[0])||FilePath.toLowerCase().endsWith(allowedFiles[1])||FilePath.toLowerCase().endsWith(allowedFiles[2]);
	}

	static Boolean IsHtmlFile(String FilePath) {
		return FilePath.toLowerCase().endsWith(allowedFiles[1])||FilePath.toLowerCase().endsWith(allowedFiles[2]);
	}
	
	
	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {

		if(IsTextOrHtmlFile(file.toString())) {
			if(true) {
				try (InputStream stream = Files.newInputStream(file)) {
					// make a new, empty document
					Document doc = new Document();

					Field pathField = new StringField("path", file.toString(), Field.Store.YES);
					doc.add(pathField);

//					System.out.println(Utilities.LongToDate(lastModified));
					doc.add(new StringField("modified", Utilities.LongToDate(lastModified),Field.Store.YES));

					// if its a html document do the following 
					if(IsHtmlFile(file.toString())) {
						Tidy tidy = new Tidy();
						tidy.setQuiet(true);
						tidy.setShowWarnings(false);
						org.w3c.dom.Document root = tidy.parseDOM(stream, null);
						Element rawDoc = root.getDocumentElement();
						JTidyHTMLHandler handler=new JTidyHTMLHandler();

						String summary;
						String body = "";
						String title;
						String titleAndBody;
						
						try {
						body = handler.getBody(rawDoc);
						}
						catch(Exception e) {
						body = "Unreadble";	
						}
						title=handler.getTitle(rawDoc);
						summary = handler.getSummary(rawDoc);
						
						titleAndBody = body; 
						
						System.out.println(summary);
						doc.add(new StringField("body", body, Field.Store.YES));
						doc.add(new StringField("summary", summary, Field.Store.YES));
						doc.add(new StringField("title", title, Field.Store.YES));
//						InputStream stream2 = new ByteArrayInputStream((title).getBytes(StandardCharsets.UTF_8));
//						//doc.add(new TextField("contents",new BufferedReader(str), Field.Store.YES));
//						doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream2, StandardCharsets.UTF_8))));
						
						InputStream stream3 = new ByteArrayInputStream((titleAndBody).getBytes(StandardCharsets.UTF_8));
						//doc.add(new TextField("contents",new BufferedReader(str), Field.Store.YES));
						doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream3, StandardCharsets.UTF_8))));
						
						doc.add(new StringField("type", "html", Field.Store.YES));
						
					}
					else {
						doc.add(new StringField("type", "txt", Field.Store.YES));
						doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

					}

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// New index, so we just add the document (no old document can be there):
						System.out.println("adding " + file);
						try {
						writer.addDocument(doc);
						}
						catch (Exception e) {
							System.out.println("ERROR ADDING FILE");
						}
					} else {

						System.out.println("updating " + file);
						writer.updateDocument(new Term("path", file.toString()), doc);
					}
				}
			}
		}
	}
}