import java.io.*;
public class ArffBuilder {
	public static void main(String args[]) {
		ArffBuild arffFile = new ArffBuild();
		try {
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
		catch(IOException exception){
			
			}
		finally {
			
		}
		}
		
}
