/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cecs429.indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CYBER19
 */
public class DiskPositionalIndex implements Index {

    private String pathFiles; // Path to the folder containing index files

    public DiskPositionalIndex(String pathFiles) {
        this.pathFiles = pathFiles;
    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> results = new ArrayList<>();

        try (RandomAccessFile postingsFile = new RandomAccessFile(pathFiles + "/postings.bin", "r"); FileChannel postingsChan = postingsFile.getChannel()) {

            // Load the byte position of where the term's postings begin
            Long bytePosition = loadBytePositionDb(term);

            if (bytePosition != null) {
                // Seek to the position of the term
                postingsChan.position(bytePosition);

                //  Using readInt to read dft, then docid gap, then tftd, then position gap
                int dft = postingsFile.readInt();

                int realDocument = 0;

                for (int i = 0; i < dft; i++) {
                    int docIdGap = postingsFile.readInt();
                    realDocument += docIdGap;
                    int tftd = postingsFile.readInt();
                    List<Integer> positions = new ArrayList<>();

                    int realPosition = 0;
                    // Read position gaps
                    for (int j = 0; j < tftd; j++) {
                        int positionGap = postingsFile.readInt();
                        realPosition += positionGap;
                        positions.add(realPosition);
                    }

                    // Construct Posting object and add to the list
                    Posting posting = new Posting(realDocument, positions);
                    results.add(posting);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;

    }

    /**
     * Use your database to load the byte position of where the term's postings
     * begin.
     */
    private Long loadBytePositionDb(String term) {
        Long bytePosition = null;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + pathFiles + File.separator + "vocab.db"); PreparedStatement statement = connection.prepareStatement("SELECT byte_position FROM vocabulary WHERE term = ?")) {
            statement.setString(1, term);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    bytePosition = resultSet.getLong("byte_position");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }

        return bytePosition;
    }

    @Override
    public List<String> getVocabulary() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
