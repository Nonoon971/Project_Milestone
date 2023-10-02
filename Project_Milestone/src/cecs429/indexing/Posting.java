package cecs429.indexing;

import java.util.*;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

    private int mDocumentId;
    int positionsCount;
    List<Integer> positions;

    public Posting(int documentId) {
        mDocumentId = documentId;
        //Initialization of the frequency of the term and the list of positions of the term
        positionsCount = 0;
        positions = new ArrayList<>();
    }

    public int getDocumentId() {
        return mDocumentId;
    }
    
    public int getPositionCount() {
        return positionsCount;
    }
    
    public List<Integer> getPositions() {
        return positions;
    }

    public void addPosition(int position) {
        positions.add(position);
        //Increment the number of occurence of the term 
        positionsCount++;
    }
}
