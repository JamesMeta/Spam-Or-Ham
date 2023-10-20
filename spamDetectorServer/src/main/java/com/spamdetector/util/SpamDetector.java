package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.net.URL;
import java.util.*;


public class SpamDetector {

    double accuracy;
    double precision;
    ArrayList<TestFile> spamObj;

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public ArrayList<TestFile> getSpamObj() {
        return this.spamObj;
    }

    public void setSpamObj(ArrayList<TestFile> spamObj) {
        this.spamObj = spamObj;
    }

    public boolean validWord(String word, HashSet<String> dictionary) {

        if(dictionary.contains(word)){  //using a hashset to check if the word is in the dictionary
            return true;                //return true if the word is in the dictionary
        }
        else{
            return false;               //return false if the word is not in the dictionary
        }
    }

    //function to load the dictionary used to check if a word is an english word
    public HashSet<String> loadDictionary(String filename) {
        HashSet<String> dictionary = new HashSet<String>();

        try {                                                   //Try to load the dictionary
            FileReader fileReader = new FileReader(filename);
            Scanner scanner = new Scanner(fileReader);

            while (scanner.hasNextLine()) {                     //While there are lines in the file
                String line = scanner.nextLine();
                dictionary.add(line);                           //add the line to the dictionary as it is a single word

            }
            scanner.close();

        } catch (IOException e) {                               //Failed to load the dictionary
            e.printStackTrace();
        }
        return dictionary;                                      //return the dictionary
    }
    //function to create maps of the words and their frequencies as well as the total number of files in the directory
    public HashMap<String, Integer> loadWordCount(String directoryName, HashSet<String> dictionary) throws IOException {
        File directory = new File(directoryName);               //load the directory
        File[] files = directory.listFiles();                   //array of files in the directory
        HashMap<String,Integer> wordFreq = new HashMap<String,Integer>();   //hashmap of the words and their frequencies
        wordFreq.put("$length", files.length);                //add the length of the directory to the hashmap under a unique key

        for (File file : files) {                               //for each file in the directory

            HashSet<String> wordUsed = new HashSet<String>();   //hashset used to store used words in the file so no duplicate words are added to the map

            if (!file.isDirectory()) {                          //checks if the file is not a directory and is indeed a file

                try (FileInputStream inputStream = new FileInputStream(file);   //try to open the file for reading
                     Scanner scanner = new Scanner(inputStream)) {

                    while (scanner.hasNextLine()) {                             //while there are lines in the file
                        String line = scanner.nextLine();
                        String[] wordsper = line.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split("\\s+");  //remove all non-alphanumeric characters
                        //make all the words lowercase
                        //split the line into words
                        for (String word : wordsper) {          //for each word in the line

                            if(validWord(word, dictionary)){    //if the word is a valid english word

                                if(!wordUsed.contains(word)){   //if the word has not already been counted in the file

                                    if (wordFreq.containsKey(word)) {   //if the word has already existed in the directory
                                        wordFreq.put(word, wordFreq.get(word) + 1); //add 1 to the word frequency

                                    } else {
                                        wordFreq.put(word, 1);   //add word to the map
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return wordFreq;
    }

    public TreeMap<String, Double> PrWGivenS(HashMap<String, Integer> S, double size){
        // Calculate probability of each word given a sentence
        TreeMap<String, Double> ProbWGivenS = new TreeMap<String, Double>();
        for (String key : S.keySet()) {
            if (key != "$length") { // Ignore the special key "$length"
                ProbWGivenS.put(key, S.get(key) / size); // Calculate probability of each word
            }
        }
        return ProbWGivenS; // Return TreeMap of probabilities
    }

    public TreeMap<String, Double> PrWGivenH(HashMap<String, Integer> H, double size){
        // Calculate probability of each word given a history
        TreeMap<String, Double> ProbWGivenH = new TreeMap<String, Double>();
        for (String key : H.keySet()) {
            if (key != "$length") { // Ignore the special key "$length"
                ProbWGivenH.put(key, H.get(key) / size); // Calculate probability of each word
            }
        }
        return ProbWGivenH; // Return TreeMap of probabilities
    }

    public TreeMap<String, Double> PrSGivenW(TreeMap<String, Double> S, TreeMap<String, Double> H){
        // Calculate probability of each word being a signal word given the probabilities of each word given a sentence and a history
        TreeMap<String, Double> ProbSGivenW = new TreeMap<String, Double>();
        for (String key : S.keySet()) {
            if (H.containsKey(key)) { // If the word is in the history
                ProbSGivenW.put(key, S.get(key) / (S.get(key) + H.get(key))); // Calculate probability of the word being a signal word
            }
            else { // If the word is not in the history
                ProbSGivenW.put(key, 1.0); // Assume the word is always a signal word
            }
        }
        return ProbSGivenW; // Return TreeMap of probabilities
    }


    public HashMap<String, Integer> combineHashMap(HashMap<String, Integer> A, HashMap<String, Integer> B){
        HashMap<String, Integer> C = new HashMap<String, Integer>();
        // Loop through keys in A
        for (String key : A.keySet()) {
            // If key exists in B, add the values together in C and remove key from B
            if(B.containsKey(key)){
                C.put(key, A.get(key) + B.get(key));
                B.remove(key);
            }
            // If key only exists in A, add its corresponding value to C
            else{
                C.put(key,A.get(key));
            }
        }
        // Loop through remaining keys in B
        for (String key : B.keySet()) {
            // If key exists in A, print a warning message to console
            if(A.containsKey(key)){
                System.out.print("Something's wrong");
            }
            // If key only exists in B, add its corresponding value to C
            else{
                C.put(key,B.get(key));
            }
        }
        return C;
    }

    public double probFileisSpam(File file, HashSet<String> dictionary, TreeMap<String, Double> ProbSGivenW){
        double n = 0;
        ArrayList<String> cleanedWords = new ArrayList<String>();
        try (FileInputStream inputStream = new FileInputStream(file);   //try to open the file for reading
             Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {                             //while there are lines in the file
                String line = scanner.nextLine();
                String[] wordsper = line.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split("\\s+");
                for (String word : wordsper) {          //for each word in the line
                    if(validWord(word, dictionary)){
                        cleanedWords.add(word);
                    }
                }
            }
            for(String word : cleanedWords){
                if(ProbSGivenW.containsKey(word)){
                    n+=findN(ProbSGivenW.get(word));
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            // Handle the exception here, or re-throw it as an unchecked exception
        } catch (IOException e) {
            // Handle the exception here, or re-throw it as an unchecked exception
        }
        return ProbSGivenF(n);
    }

    /**
     * Finds the number of samples required to achieve a given probability of success.
     *
     * @param pr The probability of success.
     * @return The number of samples required.
     */
    public double findN(double pr){
        double n = 0;
        n = (Math.log(1-pr)-Math.log(pr)); // Calculate n using the given formula
        return n;
    }

    /**
     * Calculates the probability of success given the number of samples.
     *
     * @param n The number of samples.
     * @return The probability of success.
     */
    public double ProbSGivenF(double n){
        double decimal = 1/(1+Math.pow(Math.E,n)); // Calculate the probability using the given formula
        return decimal;
    }

    /**
     * Calculates the accuracy of a classification algorithm.
     *
     * @param truePos The number of true positives.
     * @param trueNeg The number of true negatives.
     * @param numFiles The total number of files classified.
     * @return The accuracy of the algorithm.
     */
    public double calcAccuracy(double truePos, double trueNeg, double numFiles){
        return ((double)(truePos+trueNeg)/numFiles); // Calculate the accuracy using the given formula
    }

    /**
     * Calculates the precision of a classification algorithm.
     *
     * @param truePos The number of true positives.
     * @param falsePos The number of false positives.
     * @return The precision of the algorithm.
     */
    public double calcPrecision(double truePos, double falsePos){
        return ((double)(truePos/(truePos+falsePos))); // Calculate the precision using the given formula
    }



    public void trainAndTest(File mainDirectory) throws IOException {
        // Create an ArrayList to hold TestFile objects
        ArrayList<TestFile> fileObjects = new ArrayList<TestFile>();

        // Define paths to the training and testing directories
        String directoryNameTrainSpam = mainDirectory.getPath()+"/train/spam";
        String directoryNameTrainHam = mainDirectory.getPath()+"/train/ham/";
        String directoryNameTrainHam2 = mainDirectory.getPath()+"/train/ham2/";
        String directoryNameTestSpam = mainDirectory.getPath()+"/test/spam/";
        String directoryNameTestHam = mainDirectory.getPath()+"/test/ham/";

        // Load the dictionary of words to use for spam detection
        HashSet<String> dictionary = loadDictionary(mainDirectory.getPath()+"/dictionary/dictionary.txt");

        // Load the word frequency counts for spam and ham training files
        HashMap<String,Integer> wordFreqSpam = loadWordCount(directoryNameTrainSpam, dictionary);
        HashMap<String,Integer> wordFreqHam = combineHashMap(loadWordCount(directoryNameTrainHam, dictionary),loadWordCount(directoryNameTrainHam2, dictionary));

        // Remove the length key from the word frequency counts
        double sizeSpam = wordFreqSpam.get("$length");
        double sizeHam = wordFreqHam.get("$length");
        wordFreqSpam.remove("$length");
        wordFreqHam.remove("$length");

        // Calculate the probabilities of words given spam and ham
        TreeMap<String, Double> ProbWGivenS = PrWGivenS(wordFreqSpam, sizeSpam);
        TreeMap<String, Double> ProbWGivenH = PrWGivenH(wordFreqHam, sizeHam);

        // Calculate the probability of spam given each word
        TreeMap<String, Double> ProbSGivenW = PrSGivenW(ProbWGivenS, ProbWGivenH);

        // Load the testing files for spam and ham
        File directorySpam = new File(directoryNameTestSpam);
        File[] filesSpam = directorySpam.listFiles();
        File directoryHam = new File(directoryNameTestHam);
        File[] filesHam = directoryHam.listFiles();

        // Calculate the probability of each testing file being spam and create a TestFile object for each file
        double spamProb;
        for(File file2 : filesSpam){
            spamProb=probFileisSpam(file2, dictionary, ProbSGivenW);
            TestFile newFile = new TestFile(file2.getName(),spamProb,"Spam");
            fileObjects.add(newFile);
        }

        // Calculate the probability of each testing file being ham and create a TestFile object for each file
        for(File file3 : filesHam){
            spamProb=probFileisSpam(file3, dictionary, ProbSGivenW);
            TestFile newFile = new TestFile(file3.getName(),spamProb,"Ham");
            fileObjects.add(newFile);
        }

        // Calculate the true positive, true negative, and false positive rates for the TestFile objects
        double truePos=0;
        double trueNeg=0;
        double falsePos=0;

        for(TestFile obj: fileObjects){
            if (obj.getSpamProbability()>0.99&&obj.getActualClass().equals("Spam")){
                truePos++;
            }
            else if (obj.getSpamProbability()<0.99&&obj.getActualClass().equals("Ham")){
                trueNeg++;
            }
            else if (obj.getSpamProbability()<0.99&&obj.getActualClass().equals("Spam")){
                ;
            }
            else if (obj.getSpamProbability()>0.99&&obj.getActualClass().equals("Ham")){
                falsePos++;
            }
        }
        setAccuracy(calcAccuracy(truePos,trueNeg,fileObjects.size()));
        setPrecision(calcPrecision(truePos, falsePos));
        setSpamObj(fileObjects);
    }
}
