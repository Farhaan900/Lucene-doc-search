package com.ovgu;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private static List<Path> filesList = new ArrayList<Path>();

	IndexFiles(String indexLoc, String docsLoc) {
		this.indexLoc = indexLoc;
		this.docsLoc = docsLoc;
	}

	private  static  String[] allowedFiles= {"txt","html"};

	public int generateIndex() {

		String docsPath=this.docsLoc;
		String indexPath=this.indexLoc;

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		try {
			System.out.println("Creating index at >>> '" + indexPath + "'");

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

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}

		// prints the list of all indexed documents
		System.out.println ("\n\nDocuments added to index\n\n");

		Iterator<Path> iterator = filesList.iterator();
		while(iterator.hasNext()) {
			System.out.println (">> "+iterator.next());
		}

		System.out.println("\n\nTotal indexed documents :"+filesList.size()+"\n\n");

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
		return FilePath.toLowerCase().endsWith(allowedFiles[0])||FilePath.toLowerCase().endsWith(allowedFiles[1]);
	}

	static Boolean IsHtmlFile(String FilePath) {
		return FilePath.toLowerCase().endsWith(allowedFiles[1]);
	}


	/**
	 * This method indexes one document.
	 * 
	 * @param writer
	 * @param file
	 * @param lastModified
	 * @throws IOException
	 */
	static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {

		if(IsTextOrHtmlFile(file.toString())) {
			if(true) {
				try (InputStream stream = Files.newInputStream(file)) {
					// make a new, empty document
					Document doc = new Document();

					Field pathField = new StringField("path", file.toString(), Field.Store.YES);
					doc.add(pathField);

					doc.add(new StringField("modified", Utilities.LongToDate(lastModified),Field.Store.YES));

					// if its a html document do the following 
					if(IsHtmlFile(file.toString())) {
						Tidy tidy = new Tidy();
						tidy.setQuiet(true);
						tidy.setShowWarnings(false);
						tidy.setShowErrors(0);
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
						try {
							title=handler.getTitle(rawDoc);							}
						catch(Exception e) {
							title = "Unreadble";	
						}
						try {
							summary = handler.getSummary(rawDoc);
						}
						catch(Exception e) {
							summary = "Unreadble";	
						}

						titleAndBody = title+" "+body; 

						doc.add(new StringField("body", body, Field.Store.YES));
						doc.add(new StringField("summary", summary, Field.Store.YES));
						doc.add(new StringField("title", title, Field.Store.YES));
						InputStream stream3 = new ByteArrayInputStream((titleAndBody).getBytes(StandardCharsets.UTF_8));
						doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream3, StandardCharsets.UTF_8))));
						doc.add(new StringField("type", "html", Field.Store.YES));

					}
					else {
						doc.add(new StringField("type", "txt", Field.Store.YES));
						doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

					}

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						try {
							writer.addDocument(doc);
							filesList.add(file);
						}
						catch (Exception e) {

						}
					} else {
						writer.updateDocument(new Term("path", file.toString()), doc);
					}
				}
			}
		}
	}
}