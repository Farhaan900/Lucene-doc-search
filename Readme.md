### LUCENE PROJECT

## add the following jar files into your project -

* jtidy-r938.jar
* lucene-analyzers-common-8.3.0.jar
* lucene-core-8.3.0.jar
* lucene.queries-8.3.0.jar
* lucene-queryparser-8.3.0.jar

#### You can find all the "./jar files" folder 

The code needs a single argument to run and that argument should be a path to the documents folder.


You can run the jar file with the following command 
```
java -jar indexerAndSearcher.jar <path_to_documents_folder>
```

The output is in a tabular format. You might need a wide screen to view it in proper format.
For html files, it also displays the title and a summary in two new lines. If the html file doesnt have a summary tag then it prints the first 100 characters of its body.

#### You can find files for testing in the "./test-docs" folder