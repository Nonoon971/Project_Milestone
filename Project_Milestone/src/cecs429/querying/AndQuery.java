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
        System.out.println("MERGE EN ACTION");
        // TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
        // intersecting the resulting postings.
        
        //get the postings of the first query components contained in the AndQuery
        List<Posting> firstQuery = mComponents.get(0).getPostings(index);
        for(int i=1; i<mComponents.size(); i++)
        {
            //get the next postings of the next query
            List<Posting> nextQuery = mComponents.get(i).getPostings(index);
            
            //merge them with the algorithm from lecture
            int k = 0;
            int j = 0;
            
            while(k < firstQuery.size() && j < nextQuery.size())
            {
                Posting firstQueryPosting = firstQuery.get(k);
                Posting nextQueryPosting = nextQuery.get(j);
                
                if(firstQueryPosting.getDocumentId() == nextQueryPosting.getDocumentId())
                {
                    //A ADAPTER POUR TRIER LES POSITIONS
                    //DEMANDER AUX PROFS SI C NECESSAIRE PCQ G LA FLEMME DE FAIRE UN TRI A BULLE.
                    result.add(firstQueryPosting);
                    k++;
                    j++;
                }
                else if(firstQueryPosting.getDocumentId() > nextQueryPosting.getDocumentId())
                    j++;
                else if(firstQueryPosting.getDocumentId() < nextQueryPosting.getDocumentId())
                    k++;                  
            }
            
        }
        return result;
    }

    @Override
    public String toString() {
        return String.join(" AND ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
