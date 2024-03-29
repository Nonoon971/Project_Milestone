package cecs429.indexing;
import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {
	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 */
	List<Posting> getPostings(String term);
	
	/**
	 * A sorted list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();
        
        /**
	 * Retrieves a list of Postings of documents that contain the given term with the position of the term in the document.
	 */
        List<Posting> getPostingsWithPositions(String term); // Nouvelle méthode

}
