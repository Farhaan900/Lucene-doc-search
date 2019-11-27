package lucene;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

class CustomAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
      Tokenizer source = new StandardTokenizer();
      TokenStreamComponents returnable = new TokenStreamComponents(source, new LowerCaseFilter(new PorterStemFilter(source)));
      System.out.println(returnable);
      return returnable;
    }
  }