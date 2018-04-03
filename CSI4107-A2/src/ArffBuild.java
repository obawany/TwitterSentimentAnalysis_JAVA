import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import java.io.*;
import java.util.*;

public class ArffBuild {
	Set<String> stopWordsSet;
	Set<String> positiveSet;
	Set<String> negativeSet;
	Set<String> positiveothers;
	Set<String> negativeothers;
	
	Map<String, Map<String, Integer>> bagOfWords;
	HashMap<String, Integer> vocabulary;
	HashMap<String, String> opinionMap;
	HashMap<String, Boolean> exclamationMap;
	HashMap<String, Boolean> questionMap;
	HashMap<String, Boolean> hashtagMap;
	HashMap<String, Boolean> happyEmoteMap;
	HashMap<String, Boolean> EmoteMap;
	HashMap<String, Integer> positive;
	HashMap<String, Integer> negative;
	Vector<String> vocabVector;
	
	File stopWords;
	File positive;
	File negative;
	File file;
	
//	ArrayList<String> atts;
//	ArrayList<String> attributeValues;
//	ArrayList<String> excalmation;
//	ArrayList<String> question;
//	ArrayList<String> hashtag;
//	ArrayList<String> happy;
//	ArrayList<String> sad;
	

	FastVector atts;
	FastVector attributeValues;
	FastVector excalmation;
	FastVector question;
	FastVector hashtag;
	FastVector happy;
	FastVector sad;
	
	Instances instance;
	
	
	public ArffBuild() {
		Set<String> stopWordsSet = new HashSet<String>();
		Set<String> positiveSet = new HashSet<String>();
		Set<String> negativeSet = new HashSet<String>();
		Set<String> positiveothers = new HashSet<String>();
		Set<String> negativeothers = new HashSet<String>();
		
		Map<String, Map<String, Integer>> bagOfWords = new HashMap<String, Map<String, Integer>>();
		HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
		HashMap<String, String> opinionMap = new HashMap<String, String>();
		HashMap<String, Boolean> exclamationMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> questionMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> hashtagMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> happyEmoteMap = new HashMap<String, Boolean>();
		HashMap<String, Boolean> EmoteMap = new HashMap<String, Boolean>();
		HashMap<String, Integer> positive = new HashMap<String, Integer>();
		HashMap<String, Integer> negative = new HashMap<String, Integer>();
		Vector<String> vocabVector = new Vector<String>();
		
		File stopWords = new File("./StopWords.txt");
		File positive = new File("./Positive.txt");
		File negative = new File("./Negative.txt");
		File file = new File("./semeval_twitter_data.txt");

//		ArrayList<String> atts = new ArrayList<String>();
//		ArrayList<String> attributeValues = new ArrayList<String>();
//		ArrayList<String> excalmation = new ArrayList<String>();
//		ArrayList<String> question = new ArrayList<String>();
//		ArrayList<String> hashtag = new ArrayList<String>();
//		ArrayList<String> happy = new ArrayList<String>();
//		ArrayList<String> sad = new ArrayList<String>();

		

	}
	public static void main(String args[]) throws Exception {
		ArffBuild arffFile = new ArffBuild();
		arffFile.removeStopWords();
		arffFile.positiveSetAndPrefixes();
		arffFile.negativeSetAndPrefixes();
		arffFile.usingEmoticons();
		arffFile.rareWords();
		arffFile.bagOfWords();
		arffFile.arffFile();
		arffFile.fillWithData();
		arffFile.saveArff();
	}
	
	
	public File removeStopWords() throws IOException{
		try (BufferedReader br = new BufferedReader(new FileReader(stopWords))) {
			for (String line; (line = br.readLine()) != null;) {
				stopWordsSet.add(line);
			}
		}
		return stopWords;
	}
	public File positiveSetAndPrefixes() throws IOException{
		try (BufferedReader br = new BufferedReader(new FileReader(positive))) {
			for (String line; (line = br.readLine()) != null;) {
				String w = line.toLowerCase();
				if (w.contains("*")) {
					positiveothers.add(w.replace("*", ""));
				} else {
					positiveSet.add(w);
				}
		}
		}
		return positive;
	}
	public File negativeSetAndPrefixes() throws IOException{
		try (BufferedReader bri = new BufferedReader(new FileReader(negative))) {
			for (String line; (line = bri.readLine()) != null;) {
				String w = line.toLowerCase();
				if (w.contains("*")) {
					negativeothers.add(w.replace("*", ""));
				} else {
					negativeSet.add(w);
					}
				}
			}
		return negative;
	}
	public void usingEmoticons() throws IOException{
		Pattern happyEmoticons = Pattern.compile(".*(:\\)|;\\)|\\(:|\\(;|♥|♡|☺).*");
		Pattern Emoticons = Pattern.compile(".*(:\\(|;\\(|\\):|\\);|\\>:\\||\\|:\\<|:@).*");
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(line).useDelimiter("\\t");
				s.next();
				String tweetID = s.next();
				String opinion = s.next().replace("\"", "");
				String sentence = s.next();

				opinionMap.put(tweetID, opinion);
				exclamationMap.put(tweetID, sentence.contains("!"));
				questionMap.put(tweetID, sentence.contains("?"));
				hashtagMap.put(tweetID, sentence.contains("#"));
				happyEmoteMap.put(tweetID, happyEmoticons.matcher(sentence).matches());
				EmoteMap.put(tweetID, Emoticons.matcher(sentence).matches());
				String[] words = sentence.split(" ");

				for (String word : words) {
					String wordCompare = word.replaceAll("[^a-zA-z]", "");
					wordCompare = wordCompare.toLowerCase();
					if (!stopWordsSet.contains(wordCompare)
							&& !wordCompare.isEmpty() && wordCompare.length()>2) {
						Integer count = vocabulary.get(wordCompare);
						if (count == null) {
							vocabulary.put(wordCompare, new Integer(1));
						} else {
							count++;
							vocabulary.put(wordCompare, count);
						}
					}

				}
			}
		}
	}
	public void rareWords() {
		Iterator it = vocabulary.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry w = (HashMap.Entry) it.next();
			if ((int) w.getValue() < 3) {
				it.remove();
			} else {
				vocabVector.add((String) w.getKey());
			}
		}
	}
	public File bagOfWords() throws IOException{
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(line).useDelimiter("\\t");
				s.next();
				String tweetID = s.next();
				s.next();
				String sentence = s.next();

				String[] words = sentence.split(" ");
				HashMap<String, Integer> wordsMap = new HashMap<String, Integer>();

				int negativeCount = 0;
				int positiveCount = 0;
				for (String word : words) {
					String wordCompare = word.replaceAll("[^a-zA-z]", "");
					wordCompare = wordCompare.toLowerCase();
					if (vocabulary.containsKey(wordCompare)) {
						Integer count = wordsMap.get(wordCompare);
						if (count == null) {
							wordsMap.put(wordCompare, new Integer(1));
						} else {
							count++;
							wordsMap.put(wordCompare, count);
						}

						if (positiveSet.contains(wordCompare)) {
							positiveCount += 1;
						} else {
							String current = wordCompare;
							if (positiveothers.stream().anyMatch(w -> current.startsWith(w))) {
								positiveCount += 1;
							}
						}
						
						if (negativeSet.contains(word)) {
							negativeCount += 1;
						} else {
							String current = wordCompare;
							if (negativeothers.stream().anyMatch(w -> current.startsWith(w))) {
								negativeCount += 1;
							}
						}
					}
				}
				if (negativeCount == 0) {
					positiveCount = positiveCount * 2 + 1;
				}
				positive.put(tweetID, positiveCount);
				negative.put(tweetID, negativeCount);
				bagOfWords.put(tweetID, wordsMap);
			}
		}
		return file;
	}
	public void arffFile() {
//		attributeValues.add("positive");
//		attributeValues.add("negative");
//		attributeValues.add("neutral");
//		attributeValues.add("objective");
//		//FIX THIS
//		//atts.add(new Attribute("OpinionCategory", attributeValues));
//		
//		excalmation.add("Y");
//		excalmation.add("N");
//		//FIX THIS
//		//atts.add(new Attribute("ExclamationMark", excalmation));
//		
//		question.add("Y");
//		question.add("N");
//		//FIX THIS
//		//atts.addElement(new Attribute("QuestionMark", question));
//		
//		hashtag.add("Y");
//		hashtag.add("N");
//		//FIX THIS
//		//atts.addElement(new Attribute("HashTag", hashtag));
//		
//
//		happy.add("Y");
//		happy.add("N");
//
//		sad.add("Y");
//		sad.add("N");
//		//FIX THIS
//		//atts.addElement(new Attribute("PositiveEmoticon", happy));
//		//atts.addElement(new Attribute("NegativeEmoticon", sad));
//		
//		// FIX THIS
//		//atts.add(new Attribute("PositiveWords"));
//		
//		// FIX THIS
//		//atts.add(new Attribute("NegativeWords"));
//		
//		for (String word : vocabVector) {
//			// FIX THIS
//		//	atts.add(new Attribute(word));
//		}
//
//		//instance = new Instances("Opinion", atts, 0);

		atts = new FastVector();
		
		attributeValues = new FastVector();
		attributeValues.addElement("positive");
		attributeValues.addElement("negative");
		attributeValues.addElement("neutral");
		attributeValues.addElement("objective");
		atts.addElement(new Attribute("OpinionCategory", attributeValues));
		
		excalmation = new FastVector();
		excalmation.addElement("Y");
		excalmation.addElement("N");
		atts.addElement(new Attribute("ExclamationMark", excalmation));
		
		question = new FastVector();
		question.addElement("Y");
		question.addElement("N");
		atts.addElement(new Attribute("QuestionMark", question));
		
		hashtag = new FastVector();
		hashtag.addElement("Y");
		hashtag.addElement("N");
		atts.addElement(new Attribute("HashTag", hashtag));
		
		happy = new FastVector();
		happy.addElement("Y");
		happy.addElement("N");
		sad = new FastVector();
		sad.addElement("Y");
		sad.addElement("N");
		atts.addElement(new Attribute("PositiveEmoticon", happy));
		atts.addElement(new Attribute("NegativeEmoticon", sad));
		
		atts.addElement(new Attribute("PositiveWords"));
		
		atts.addElement(new Attribute("NegativeWords"));
		
		for (String word : vocabVector) {
			atts.addElement(new Attribute(word));
		}

		instance = new Instances("Opinion", atts, 0);
	}
	public void fillWithData() {
		for (String tweetIDKey : bagOfWords.keySet()) {			
			Map<String, Integer> sentence = bagOfWords.get(tweetIDKey);
			
			List<Double> values = Arrays.asList(
					(double) attributeValues.indexOf(opinionMap.get(tweetIDKey)), 
					(double) excalmation.indexOf(exclamationMap.get(tweetIDKey) ? "Y" : "N"),
					(double) question.indexOf(questionMap.get(tweetIDKey) ? "Y" : "N"), 
					(double) hashtag.indexOf(hashtagMap.get(tweetIDKey) ? "Y" : "N"), 
					(double) happy.indexOf(happyEmoteMap.get(tweetIDKey) ? "Y" : "N"),
					(double) sad.indexOf(EmoteMap.get(tweetIDKey) ? "Y" : "N"),
					(double) positive.get(tweetIDKey),
					(double) negative.get(tweetIDKey)
			);

			List<Double> vocab = vocabVector.stream().map(key -> (double) (sentence.containsKey(key) ? sentence.get(key) : 0))
					.collect(Collectors.toList());
			List<Double> union = Stream.concat(values.stream(), vocab.stream()).collect(Collectors.toList());
			double[] valueArray = union.stream().mapToDouble(d -> d).toArray();
		}
	}
	public void saveArff() throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instance);
		saver.setFile(new File("/semeval_twitter_data.arff"));
		saver.writeBatch();
	}

}
