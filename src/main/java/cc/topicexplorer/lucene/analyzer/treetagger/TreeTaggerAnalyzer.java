package cc.topicexplorer.lucene.analyzer.treetagger;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

public class TreeTaggerAnalyzer extends Analyzer {

	private String treeTaggerPath;
	private String model;

	/**
	 * Constructs a Lucene compatible analyzer that uses the Lucene
	 * WhitespaceTokenizer for tokenization and TreeTagger for POS tagging and
	 * lemmatization. Words with hypens are POS tagged but not lemmatized. 
	 * 
	 * @param treeTaggerPath
	 *            points to the TreeTagger installation, e.g.
	 *            "/home/user/TreeTagger"
	 * @param model
	 *            specifies the TreeTagger model that is used, e.g.
	 *            "/german-utf8.par". Note that the model file is expected in
	 *            treeTaggerPath+"/models"+model, e.g.
	 *            "/home/user/TreeTagger/models/german-utf8.par". In some
	 *            TreeTagger installations, you need to manually create
	 *            subdirectory "models/" and set links to the model files in
	 *            subdirectory "lib/".
	 */
	public TreeTaggerAnalyzer(String treeTaggerPath, String model) {
		this.treeTaggerPath = treeTaggerPath;
		this.model = model;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		Tokenizer source = new WhitespaceTokenizer(Version.LUCENE_47, reader);
		TokenStream filter = new TreeTaggerFilter(source, treeTaggerPath, model);
		return new TokenStreamComponents(source, filter);
	}

}
