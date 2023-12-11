package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.*;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessorDerived;

import java.io.*;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TermDocumentIndexer {

    public static void main(String[] args) {
        try {
            //Ask for a directory to load documents from when it begins
            Scanner scanner = new Scanner(System.in);
            System.out.print("What is the path of the corpus ? : ");
            String directoryPath = scanner.nextLine();
            
            // Cast the string in path object
            Path path = Paths.get(directoryPath);
            
            // Create a DocumentCorpus to load .txt and .json documents from the project directory.
            DocumentCorpus corpus = DirectoryCorpus.loadDocumentDirectory(path.toAbsolutePath());
            
            // Index the documents of the corpus.
            Index index = indexCorpus(corpus, directoryPath);
            
            //Initialization of user's query
            String userQuery = "";
            
            //The user is asked for a term to search.
            do {
                System.out.println("\nType 'quit' if you want stop the program");
                System.out.print("Type the term you want to search: ");
                Scanner in = new Scanner(System.in);
                userQuery = in.nextLine();
                int numberDoc = 0;
                switch (userQuery) {
                    
                    case "quit" ->
                        System.out.println("See you!");
                        
                    default -> {
                        System.out.println("\nThe documents which contains the '" + userQuery + "' term are : ");
                        QueryComponent queryComponent = BooleanQueryParser.parseQuery(userQuery);
                        
                        if (queryComponent != null) {
                            List<Posting> postings = queryComponent.getPostings(index);
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
                        } else {
                            System.out.println("We are sorry, we didn't understand your query.");
                        }
                    }
                }
            } while (!userQuery.equals("quit"));
        } catch (IOException | SQLException ex) {
            Logger.getLogger(TermDocumentIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Index indexCorpus(DocumentCorpus corpus, String directoryPath) throws IOException, FileNotFoundException, SQLException {
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
        DiskIndexWriter writerIndex = new DiskIndexWriter();

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
        
        writerIndex.writeIndex(index, directoryPath);
        
        DiskPositionalIndex diskIndex = new DiskPositionalIndex(directoryPath);

        // Compute document lengths and write to docWeights.bin
        double[] documentLengths = new double[corpus.getCorpusSize()];

        for (String term : index.getVocabulary()) {
            List<Posting> postingsTerm = index.getPostings(term);

            // Calculate L_d for each document that contains the term
            for (Posting posting : postingsTerm) {
                int documentId = posting.getDocumentId();
                int tf_td = posting.getPositionCount();

                // Calculate (tf_{d,t})² and add it to the sum
                double termFrequencySquared = Math.pow(tf_td, 2);
                // Update the document length for the current document
                documentLengths[documentId] += termFrequencySquared;
            }
        }

        // Now, compute the square root of the sum to get the Euclidean length
        try (RandomAccessFile docWeightsFile = new RandomAccessFile(directoryPath + "/docWeights.bin", "rw")) {
            for (int documentId = 0; documentId < documentLengths.length; documentId++) {
                double documentWeight = Math.sqrt(documentLengths[documentId]);
                // Write the document weight to the docWeights.bin file
                docWeightsFile.writeDouble(documentWeight);
            }
        } catch (IOException e) {
        }

        return diskIndex;
    }
}
