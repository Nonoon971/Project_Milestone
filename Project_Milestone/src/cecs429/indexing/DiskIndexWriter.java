/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cecs429.indexing;

import java.util.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.*;

/**
 *
 * @author CYBER19
 */
public class DiskIndexWriter {

    public void writeIndex(PositionalInvertedIndex index, String pathFiles) throws FileNotFoundException, IOException, SQLException {
        // use the RandomAccessFile class to open the file
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + pathFiles + "/vocab.db"); 
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS vocabulary (term TEXT PRIMARY KEY, byte_position INTEGER)");

            try (RandomAccessFile postingsFile = new RandomAccessFile(pathFiles + "/postings.bin", "rw"); 
                 //In order to get back the position where postings begin in the file
                 FileChannel postingsChan = postingsFile.getChannel()){

                for (String term : index.getVocabulary()) {
                    // Write postings to disk and get the byte position
                    List<Posting> postingsTerm = index.getPostings(term);
                    // Get the byte position where postings begin
                    long bytePosition = postingsChan.position();

                    // Store  the # of documents containing the term (dft)
                    int dft = postingsTerm.size();
                    postingsFile.writeInt(dft);

                    int prevDocId = 0;

                    // Loop through postings and write to disk
                    for (Posting posting : postingsTerm) {
                        int docIdGap = posting.getDocumentId() - prevDocId;
                        postingsFile.writeInt(docIdGap); //write gap between doc ids
                        postingsFile.writeInt(posting.getPositionCount()); // write tf_{t,d}, the number of times t occurs in the doc                     
                        
                        int prevPosition = 0;
                        for (int position : posting.getPositions()) {
                            int positionGap = position - prevPosition;
                            postingsFile.writeInt(positionGap); //write the gap between positions of t in d

                            prevPosition = position; //Update prevPositon to the next position
                        }

                        prevDocId = posting.getDocumentId(); //Update the prevDoc
                    }

                    // Insert term and byte position into the vocabulary table
                    String sql = "INSERT INTO vocabulary (term, byte_position) VALUES (?, ?)";

                    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                        pstmt.setString(1, term);
                        pstmt.setLong(2, bytePosition);
                        pstmt.executeUpdate();
                    }

                }              
            }
        }
    }
}
