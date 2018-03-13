package cc.topicexplorer.lucene.analyzer.treetagger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * @author Alexander Hinneburg
 * 
 */
public final class TreeTaggerFilter extends TokenFilter {

	private LinkedList<String> tokenQueue;
	private LinkedList<String> tt_tokenQueue;
	private LinkedList<String> tt_posQueue;
	private LinkedList<String> tt_lemmaQueue;
	private LinkedList<AttributeSource.State> stateQueue;
	private TreeTaggerWrapper<String> tt;
	private final CharTermAttribute termAtt;
	private final PositionIncrementAttribute posIncrAtt;
	private final TypeAttribute typeAttribute;
	private static Pattern wordPattern = Pattern
			.compile("\\P{L}*([-\\p{L}]+).*");
	private static Pattern firstLemmaPattern = Pattern.compile("(\\p{L}+).*");
	private static Pattern hyphenPattern = Pattern.compile(".*-.*");
	private static Matcher matcher;

	/**
	 * @param input
	 *            TokenStream to read from. Usually some Tokenizer is used.
	 *            Lucene's WhitespaceTokenizer works well, as it does not
	 *            changes cases of tokens
	 * @param treeTaggerPath
	 *            points to the TreeTagger installation, e.g.
	 *            "/home/user/TreeTagger"
	 * 
	 * @param model
	 *            specifies the TreeTagger model that is used, e.g.
	 *            "/german-utf8.par". Note that the model file is expected in
	 *            treeTaggerPath+"/models"+model, e.g.
	 *            "/home/user/TreeTagger/models/german-utf8.par". In some
	 *            TreeTagger installations, you need to manally create
	 *            subdirectory "models/" and set links to the model files in
	 *            subdirectory "lib/".
	 */
	protected TreeTaggerFilter(TokenStream input, String treeTaggerPath,
			String model) {
		super(input);
		System.setProperty("treetagger.home", treeTaggerPath);
		this.tt = new TreeTaggerWrapper<String>();
		try {
			this.tt.setModel(model);
			this.tt.setHandler(new TokenHandler<String>() {
				public void token(String token, String pos, String lemma) {
					tt_tokenQueue.addLast(token);
					tt_posQueue.addLast(pos);
					String lemmaToInsert = new String(lemma);
					matcher = firstLemmaPattern.matcher(lemma);
					if (matcher.find()) {
						lemmaToInsert = matcher.group(1);
					}
					matcher = hyphenPattern.matcher(token);
					if (matcher.find()) {
						lemmaToInsert = token;
					}
					tt_lemmaQueue.addLast(lemmaToInsert);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.tokenQueue = new LinkedList<String>();
		this.tt_tokenQueue = new LinkedList<String>();
		this.tt_posQueue = new LinkedList<String>();
		this.tt_lemmaQueue = new LinkedList<String>();
		this.stateQueue = new LinkedList<AttributeSource.State>();
		this.termAtt = addAttribute(CharTermAttribute.class);
		this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		this.typeAttribute = addAttribute(TypeAttribute.class);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (stateQueue.isEmpty()) {
			readInputTokenstreamUntilTheEnd();
			runTreeTaggerOnTokenQueue();
			return writeOutTokenIfPossible();
		} else {
			return writeOutTokenIfPossible();
		}
	}

	private void readInputTokenstreamUntilTheEnd() throws IOException {
		while (input.incrementToken()) {
			String token = termAtt.toString();
			Matcher matcher = wordPattern.matcher(token);
			if (matcher.find()) {
				tokenQueue.addLast(matcher.group(1));
				stateQueue.addLast(captureState());
			}
		}
	}

	private void runTreeTaggerOnTokenQueue() throws IOException {
		try {
			tt.process(tokenQueue);
			// The handler, which is set in the constructor,
			// fills tt_tokenQueue, tt_posQueue and tt_lemmaQueue
		} catch (TreeTaggerException e) {
			e.printStackTrace();
		}
	}

	private boolean writeOutTokenIfPossible() {
		if (stateQueue.size() > 0) {
			// When there are some tokens in the queue
			if (tokenQueue.size() == stateQueue.size()) {
				// token is written first
				// restore state and leave it in queue
				restoreState(stateQueue.getFirst());
				String token = tokenQueue.removeFirst();
				char buffer[] = termAtt.buffer();
				termAtt.resizeBuffer(token.length());
				termAtt.setLength(token.length());
				System.arraycopy(token.toCharArray(), 0, buffer, 0,
						token.length());
				typeAttribute.setType("Token:" + tt_posQueue.getFirst());
			} else {
				// lemma is written second
				// restore state and remove it from queue
				restoreState(stateQueue.removeFirst());
				tt_tokenQueue.removeFirst();
				String lemma = tt_lemmaQueue.removeFirst();
				char buffer[] = termAtt.buffer();
				termAtt.resizeBuffer(lemma.length());
				termAtt.setLength(lemma.length());
				System.arraycopy(lemma.toCharArray(), 0, buffer, 0,
						lemma.length());
				posIncrAtt.setPositionIncrement(0);
				typeAttribute.setType("Lemma:" + tt_posQueue.removeFirst());

			}
			return true;

		}
		// No token left, thus, return false
		return false;
	}

}
