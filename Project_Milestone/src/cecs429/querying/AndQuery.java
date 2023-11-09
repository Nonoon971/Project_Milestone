package cecs429.querying;

import java.util.*;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {

    private List<QueryComponent> mComponents;

    public AndQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    public List<QueryComponent> getComponents() {
        return mComponents;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> result = new ArrayList<>();

        //get the postings of the first query components contained in the AndQuery
        List<Posting> firstQuery = mComponents.get(0).getPostings(index);
        for (int i = 1; i < mComponents.size(); i++) 
        {
            //get the next postings of the next query
            List<Posting> nextQuery = mComponents.get(i).getPostings(index);

            //Verfiy if the second component in the merge is a "negative" component 
            if (mComponents.get(i) instanceof NotQuery) {
                // Remove documentID that match the "NOT" query component from the result posting list
                if (result.isEmpty()) {
                    result = subtractPostings(firstQuery, nextQuery);
                } else {
                    result = subtractPostings(result, nextQuery);
                }
            } else {
                if (result.isEmpty()) {
                    result = mergeAndQuery(firstQuery, nextQuery);
                } else {
                    result = mergeAndQuery(result, nextQuery);
                }
            }
        }
        return result;
    }

    //And query intersection book algorithm
    private List<Posting> mergeAndQuery(List<Posting> p1, List<Posting> p2) {
        List<Posting> result = new ArrayList<>();
        int k = 0;
        int j = 0;
        while (k < p1.size() && j < p2.size()) {
            Posting firstQueryPosting = p1.get(k);
            Posting nextQueryPosting = p2.get(j);

            if (firstQueryPosting.getDocumentId() == nextQueryPosting.getDocumentId()) 
            {                
                result.add(firstQueryPosting);
                k++;
                j++;
            } else if (firstQueryPosting.getDocumentId() > nextQueryPosting.getDocumentId()) {
                j++;
            } else if (firstQueryPosting.getDocumentId() < nextQueryPosting.getDocumentId()) {
                k++;
            }
        }
        return result;
    }

    // Skip postings of a "NOT" query component from a result set.
    private List<Posting> subtractPostings(List<Posting> result, List<Posting> notQuery) {
        List<Posting> newResult = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < result.size() && j < notQuery.size()) {
            Posting resultPosting = result.get(i);
            Posting notQueryPosting = notQuery.get(j);

            // If we have the same document ID, we skip this document and not consider it.
            if (resultPosting.getDocumentId() == notQueryPosting.getDocumentId()) {
                //Move forward in the posting list of result and the query
                i++;
                j++;
            } else if (resultPosting.getDocumentId() < notQueryPosting.getDocumentId()) {
                // If the result docID is smaller than the notQuery docID, we include it in new result.
                newResult.add(resultPosting);
                i++; // Move the result pointer.
            } else {
                // If the notQuery has a smaller docID, we don't consider it and move forward in his the posting list.
                j++;
            }
        }

        //We add the rest of the element of result to not loose the initial docID
        while (i < result.size()) {
            newResult.add(result.get(i));
            i++;
        }

        return newResult;
    }

    @Override
    public String toString() {
        return String.join(" AND ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
