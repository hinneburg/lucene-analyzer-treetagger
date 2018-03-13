### lucene-analyzer-treetagger

#### Description

A small natural language processing library (NLP) that combines the white-space tokenizer of [Apache Lucene](http://lucene.apache.org/core/) with the java wrapper [tt4j](https://reckart.github.io/tt4j/) for [TreeTagger](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/). Given an input text, the output yields the sequence of tokens with offset positions (Apache Lucene) combined with [part-of-speech (POS) tags](https://en.wikipedia.org/wiki/Part-of-speech_tagging) and [lemmatized tokens](https://en.wikipedia.org/wiki/Lemmatisation) (TreeTagger).

#### Usage
The api of lucene-analyzer-treetagger follows the general interface of lucene analyzers that consists of the TokenStream class and different types of tokenattributes.

```
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import cc.topicexplorer.lucene.analyzer.treetagger.TreeTaggerAnalyzer;
```
The next three lines construct the `TreeTaggerAnalyzer` with a german language model and create the `TokenStream` object for a string with sentence as text input.
```
TreeTaggerAnalyzer analyzer = new TreeTaggerAnalyzer(treeTaggerPath, "/german-utf8.par");
String sentence= "Otto hat heute keine Lust zum Arbeiten."
TokenStream tokenStream = analyzer.tokenStream("myLuceneIndexField", sentence);
```
The `TokenStream` can be inspected by different attributes that are added to the `TokenStream` object. Then the `TokenStream` is iterated by calling `tokenStream.incrementToken()` and inspecting the attributes.
```
OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

tokenStream.reset();
while (tokenStream.incrementToken()) {
  Integer startOffset = offsetAttribute.startOffset();
  Integer endOffset = offsetAttribute.endOffset();
  String type = typeAttribute.type();
  String term = charTermAttribute.toString();
  System.out.println ("[" + term + ":" + startOffset + "->" + endOffset + ":" + type + "] ");
}
```
The printed output is an alternating token sequence that gives the original token with POS tag followed by the lemmatized version same offset positions.
```
[Otto:0->4:Token:NE]
[Otto:0->4:Lemma:NE]
[hat:5->8:Token:VAFIN]
[haben:5->8:Lemma:VAFIN]
[heute:9->14:Token:ADV]
[heute:9->14:Lemma:ADV]
[keine:15->20:Token:PIAT]
[keine:15->20:Lemma:PIAT]
[Lust:21->25:Token:NN]
[Lust:21->25:Lemma:NN]
[zum:26->29:Token:APPRART]
[zu:26->29:Lemma:APPRART]
[Arbeiten:30->39:Token:NN]
[Arbeit:30->39:Lemma:NN" }
```

#### Installation
To install lucene-analyzer-treetagger you can add it as a maven dependency. Until the binaries are hosted on a public maven repository, it is recommended to clone this repository, checkout the latest release and install it to your local maven repository by executing.
```
mvn clean install -Dmaven.test.skip=true
```

Remove the switch `-Dmaven.test.skip=true`, if you want to check your local TreeTagger installation. For this, copy the `src/test/reources/treetagger-path.default.properties` to `src/test/resources/treetagger-path.local.properties` and modify the property `tree-tagger-path` such that it points to your local TreeTagger directory.  Note that in this folder also the TreeTagger model file `german-utf8.par` is expected in the sub-folder `/models`. Note that the model file is expected in `<tree-tagger-path>/models/german-utf8.par`. In some TreeTagger installations, you need to manually create the sub-directory `models/` and set links to the model files in sub-directory `lib/`.   
