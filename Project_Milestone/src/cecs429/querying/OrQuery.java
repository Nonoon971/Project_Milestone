package cecs429.querying;

import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import java.util.*;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a
 * union-type operation.
 */
public class OrQuery implements QueryComponent {
    // The components of the Or query.

    private List<QueryComponent> mComponents;

    public OrQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> result = new ArrayList<>();

        // TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
        // unioning the resulting postings.
        //Go through all the components of the Or query
        for (QueryComponent component : mComponents) {

            //Get back the posting list of the term of the query
            List<Posting> componentPostings = component.getPostings(index);

            //Union the postings with those already present in the result.
            for (Posting componentPosting : componentPostings) {
                //We don't want the postings to appear more than once.
                boolean isUnique = true;
                for (Posting resultPosting : result) {
                    if (resultPosting.getDocumentId() == componentPosting.getDocumentId()) {
                        isUnique = false;
                        break;
                    }
                }
                if (isUnique) {
                    result.add(componentPosting);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        // Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
        return "("
                + String.join(" OR ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
                + " )";
    }
}
