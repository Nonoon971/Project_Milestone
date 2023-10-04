package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.*;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessorDerived;

import java.io.*;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class TermDocumentIndexer {

    public static void main(String[] args) {
        //Ask for a directory to load documents from when it begins
        Scanner scanner = new Scanner(System.in);
        System.out.print("What is the path of the corpus ? : ");
        String directoryPath = scanner.nextLine();

        // Cast the string in path object
        Path path = Paths.get(directoryPath);

        // Create a DocumentCorpus to load .txt and .json documents from the project directory.
        DocumentCorpus corpus = DirectoryCorpus.loadDocumentDirectory(path.toAbsolutePath());

        // Index the documents of the corpus.
        Index index = indexCorpus(corpus);

        //Initialisation of user's query
        String userQuery = "";
        
        //The user is asked for a term to search.
        do {
            System.out.println("\nType 'quit' if you want stop the program");
            System.out.print("Type the term you want to search: ");
            Scanner in = new Scanner(System.in);
            userQuery = in.next();
            int numberDoc = 0;
            switch (userQuery) {

                case "quit" ->
                    System.out.println("See you!");

                default -> {
                    System.out.println("\nThe documents which contains the '" + userQuery + "' term are : ");
                    List<Posting> postings = index.getPostingsWithPositions(userQuery.toLowerCase());
                    if (!postings.isEmpty()) {
                        for (Posting p : postings) {
                            System.out.print(corpus.getDocument(p.getDocumentId()).getTitle() + "," + p.getPositionCount());
                            System.out.println(p.getPositions());
                            //Counter of number document found
                            numberDoc += 1;
                        }
                        System.out.println("Number of documents found : " + numberDoc);
                    } else {
                        System.out.println("No documents found with the term '" + userQuery + "'.");
                    }
                }
            }
        } while (!userQuery.equals("quit"));
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        HashSet<String> vocabulary = new HashSet<>();
        TokenProcessorDerived processor = new TokenProcessorDerived();

        // First, build the vocabulary hash set.
        for (Document d : corpus.getDocuments()) {
            System.out.println("Found document " + d.getTitle());

            // Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
            EnglishTokenStream tokenStream = new EnglishTokenStream(d.getContent());

            // Iterate through the tokens in the document 
            for (String token : tokenStream.getTokens()) {
                //processing them using a derived TokenProcessor 
                List<String> processingToken = processor.processToken(token);
                //adding them to the HashSet vocabulary.
                vocabulary.addAll(processingToken);
            }
        }

        // Constuct a inverted index
        PositionalInvertedIndex index = new PositionalInvertedIndex(vocabulary);

        // THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addTerm
        for (int documentId = 0; documentId < corpus.getCorpusSize(); documentId++) {
            Document d = corpus.getDocument(documentId);
            int position = 0; // Initialization of position to 0 to get back the position of term in the actual document.

            // We're doing the same than above when inserting into the HashSet
            EnglishTokenStream tokenStream = new EnglishTokenStream(d.getContent());
            for (String token : tokenStream.getTokens()) {
                for (String processedToken : processor.processToken(token)) {
                    // But instead of adding to the HashSet Vocabulary,
                    // adding the term to the index for the current document
                    index.addTerm(processedToken, documentId, position);                    
                }
                position++; //increment position for the next term
            }
        }
        return index;
    }
}
