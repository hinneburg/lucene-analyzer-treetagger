package cc.topicexplorer.lucene.analyzer.treetagger;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import cc.topicexplorer.lucene.analyzer.treetagger.TreeTaggerAnalyzer;

public class TreeTaggerAnalyzerTest {

	@Test
	public void testTreeTaggerAnalyzerWithGermanTestCases() throws IOException {

		InputStream propertyDefaultInput = TreeTaggerAnalyzerTest.class
				.getResourceAsStream("/treetagger-path.default.properties");
		InputStream propertyLocalInput = TreeTaggerAnalyzerTest.class
				.getResourceAsStream("/treetagger-path.local.properties");

		if ((propertyDefaultInput == null) && (propertyLocalInput == null)) {
			RuntimeException loadException = new RuntimeException(
					"treetagger-path.default.properties and treetagger-path.local.properties properties not found.\n"
							+ "Put at least one of the files have into the class path to run the unit-test.\n"
							+ "Settings in the local file overwrite the default.");
			throw loadException;
		}

		InputStream propertyInput = null;
		if (propertyLocalInput != null) {
			propertyInput = propertyLocalInput;
		} else {
			propertyInput = propertyDefaultInput;
		}
		Properties treetaggerProperties = new Properties();
		try {
			treetaggerProperties.load(propertyInput);
		} catch (IOException e) {
			RuntimeException runtimeIOException = new RuntimeException("treetagger properties could not be loaded.", e);
			throw runtimeIOException;
		}
		
		String treeTaggerPath = treetaggerProperties.getProperty("tree-tagger-path");
		if (treeTaggerPath == null)
		{
			RuntimeException loadPropertyException = new RuntimeException(
					"treetagger properties file does not contain property tree-tagger-path.");
			throw loadPropertyException;
		}
		
		TreeTaggerAnalyzer analyzer = new TreeTaggerAnalyzer(treeTaggerPath, "/german-utf8.par");

		String testCases[] = { "Das ist ein Satz.",
				"Hier kommt ein zweiter Satz, der den schwierigen Straßennamen Von-Seckendorff-Platz enthält, und erst spät endet.",
				"Otto hat heute keine Lust zum Arbeiten." };
		String expectedResults[][][] = {
				{ { "Das", "0", "3", "Token:PDS" }, { "die", "0", "3", "Lemma:PDS" },
						{ "ist", "4", "7", "Token:VAFIN" }, { "sein", "4", "7", "Lemma:VAFIN" },
						{ "ein", "8", "11", "Token:ART" }, { "eine", "8", "11", "Lemma:ART" },
						{ "Satz", "12", "17", "Token:NN" }, { "Satz", "12", "17", "Lemma:NN" } },
				{ { "Hier", "0", "4", "Token:ADV" }, { "hier", "0", "4", "Lemma:ADV" },
						{ "kommt", "5", "10", "Token:VVFIN" }, { "kommen", "5", "10", "Lemma:VVFIN" },
						{ "ein", "11", "14", "Token:ART" }, { "eine", "11", "14", "Lemma:ART" },
						{ "zweiter", "15", "22", "Token:ADJA" }, { "zweit", "15", "22", "Lemma:ADJA" },
						{ "Satz", "23", "28", "Token:NN" }, { "Satz", "23", "28", "Lemma:NN" },
						{ "der", "29", "32", "Token:ART" }, { "die", "29", "32", "Lemma:ART" },
						{ "den", "33", "36", "Token:ART" }, { "die", "33", "36", "Lemma:ART" },
						{ "schwierigen", "37", "48", "Token:ADJA" }, { "schwierig", "37", "48", "Lemma:ADJA" },
						{ "Straßennamen", "49", "61", "Token:NN" }, { "Straßenname", "49", "61", "Lemma:NN" },
						{ "Von-Seckendorff-Platz", "62", "83", "Token:NN" },
						{ "Von-Seckendorff-Platz", "62", "83", "Lemma:NN" }, { "enthält", "84", "92", "Token:VVFIN" },
						{ "enthalten", "84", "92", "Lemma:VVFIN" }, { "und", "93", "96", "Token:KON" },
						{ "und", "93", "96", "Lemma:KON" }, { "erst", "97", "101", "Token:ADV" },
						{ "erst", "97", "101", "Lemma:ADV" }, { "spät", "102", "106", "Token:ADJD" },
						{ "spät", "102", "106", "Lemma:ADJD" }, { "endet", "107", "113", "Token:VVFIN" },
						{ "enden", "107", "113", "Lemma:VVFIN" } },
				{ { "Otto", "0", "4", "Token:NE" }, { "Otto", "0", "4", "Lemma:NE" },
						{ "hat", "5", "8", "Token:VAFIN" }, { "haben", "5", "8", "Lemma:VAFIN" },
						{ "heute", "9", "14", "Token:ADV" }, { "heute", "9", "14", "Lemma:ADV" },
						{ "keine", "15", "20", "Token:PIAT" }, { "keine", "15", "20", "Lemma:PIAT" },
						{ "Lust", "21", "25", "Token:NN" }, { "Lust", "21", "25", "Lemma:NN" },
						{ "zum", "26", "29", "Token:APPRART" }, { "zu", "26", "29", "Lemma:APPRART" },
						{ "Arbeiten", "30", "39", "Token:NN" }, { "Arbeit", "30", "39", "Lemma:NN" } }

		};

		ArrayList<ArrayList<ArrayList<String>>> computedTotalResult = new ArrayList<ArrayList<ArrayList<String>>>();

		for (Integer testCaseIdx = 0; testCaseIdx < testCases.length; testCaseIdx++) {
			final TokenStream tokenStream = analyzer.tokenStream("testField", testCases[testCaseIdx]);
			ArrayList<ArrayList<String>> computedResultTestCase = new ArrayList<ArrayList<String>>();

			OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				Integer startOffset = offsetAttribute.startOffset();
				Integer endOffset = offsetAttribute.endOffset();
				String type = typeAttribute.type();
				String term = charTermAttribute.toString();
				ArrayList<String> computedResultToken = new ArrayList<String>();
				computedResultToken.add(term);
				computedResultToken.add(startOffset.toString());
				computedResultToken.add(endOffset.toString());
				computedResultToken.add(type);
				computedResultTestCase.add(computedResultToken);
			}
			tokenStream.close();
			computedTotalResult.add(computedResultTestCase);

		}
		analyzer.close();

		for (Integer i = 0; i < expectedResults.length; i++) {
			for (Integer j = 0; j < expectedResults[i].length; j++) {
				for (Integer k = 0; k < expectedResults[i][j].length; k++) {
					assertEquals("Testcase " + i + ", Token " + j + ", Attribute " + k + ":", expectedResults[i][j][k],
							computedTotalResult.get(i).get(j).get(k));
				}
			}
		}

	}

}
