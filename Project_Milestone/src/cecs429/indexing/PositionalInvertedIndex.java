package cecs429.indexing;

import java.util.*;

/**
 * Implements an Index using an inverted Index. Requires knowing the full corpus
 * vocabulary.
 */
public class PositionalInvertedIndex  implements Index {

    private final HashMap<String, List<Posting>> invertedIndex = new HashMap<>();
    private final List<String> mVocabulary;

    /**
     * Constructs an empty index with with given vocabulary set
     *
     * @param vocabulary a collection of all terms in the corpus vocabulary.
     */
    public PositionalInvertedIndex(Collection<String> vocabulary) {
        mVocabulary = new ArrayList<String>();
        mVocabulary.addAll(vocabulary);

        Collections.sort(mVocabulary);
    }

    /**
     * Associates the given documentId with the given term in the index and his
     * positions in the documents.
     */
    public void addTerm(String term, int documentId, int position) {
        int vIndex = Collections.binarySearch(mVocabulary, term);
        if (vIndex < 0) {
            mVocabulary.add(term);
        }

        //get the list of postings associated with the term
        List<Posting> postings = invertedIndex.get(term);

        // If there is no list for this term, we create an empty list
        if (postings == null) {
            postings = new ArrayList<>();
            invertedIndex.put(term, postings);
        }

        //If we just created the empty posting list there is no document so we can add it
        if (postings.isEmpty()) {
            Posting newPosting = new Posting(documentId);
            newPosting.addPosition(position);
            postings.add(newPosting);

        } //If the last element of the sorted list of posting is higher or equal 
        //that means the document is already present in the list
        else {
            boolean isDistinctDocumentId = true;

            // Get back the last posting in the list
            Posting lastPosting = postings.get(postings.size() - 1);
            if (lastPosting.getDocumentId() >= documentId) {
                isDistinctDocumentId = false;
                
                //If the document is already present in the list, we add the position in posting position list
                lastPosting.addPosition(position);
            }
            if (isDistinctDocumentId) {
                Posting newPosting = new Posting(documentId);
                newPosting.addPosition(position);
                postings.add(newPosting);
            }
        }

    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> results = new ArrayList<>();

        //get the list of postings associated with the term
        List<Posting> postings = invertedIndex.get(term);

        //Add the posting result in the list
        if (postings != null) {
            results.addAll(postings);
        }

        return results;
    }

    @Override
    public List<String> getVocabulary() {
        return Collections.unmodifiableList(mVocabulary);
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        List<Posting> results = new ArrayList<>();

        //get the list of postings associated with the term
        List<Posting> postings = invertedIndex.get(term);

        //Add the posting result in the list
        if (postings != null) {
            results.addAll(postings);
        }

        return results;
    }
}
