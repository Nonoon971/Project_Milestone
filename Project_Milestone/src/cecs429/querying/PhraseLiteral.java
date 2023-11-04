package cecs429.querying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.Query;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import static java.lang.Math.abs;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {
    // The list of individual terms in the phrase.

    private List<QueryComponent> mComponents = new ArrayList<>();

    /**
     * Constructs a PhraseLiteral with the given individual phrase terms.
     */
    public PhraseLiteral(Collection<QueryComponent> terms) {
        mComponents.addAll(terms);
    }

    @Override
    public List<Posting> getPostings(Index index) {

        // TODO: program this method. Retrieve the postings for the individual terms in the phrase,
        // and positional merge them together.
        int k = 1;
        // Retrieve postings for the first two components in the phrase
        List<Posting> firstComponent = mComponents.get(0).getPostings(index);
        //List<Posting> secondComponent = mComponents.get(1).getPostings(index);

        List<Posting> result = null;

        // Get postings for additional components if you have more than 2.
        for (int i = 1; i < mComponents.size(); i++) {
            List<Posting> nextComponentPostings = mComponents.get(i).getPostings(index);

            // Effectuez l'intersection positionnelle avec le composant actuel et mettez à jour le résultat.
            if (result == null) {
                result = positionalIntersect(firstComponent, nextComponentPostings, i);
            } else {
                result = positionalIntersect(result, nextComponentPostings, i);
            }
        }

        return result;

    }

    ////Algorithm from the book
    public static List<Posting> positionalIntersect(List<Posting> p1, List<Posting> p2, int k) {
        List<Posting> result = new ArrayList<>();

        int i = 0;
        int j = 0;
        while (i < p1.size() && j < p2.size()) {
            Posting firstComponentPosting = p1.get(i);
            Posting secondComponentPosting = p2.get(j);
            if (firstComponentPosting.getDocumentId() == secondComponentPosting.getDocumentId()) {
                List<Integer> list = new ArrayList<>();
                List<Integer> listFinal = new ArrayList<>();
                List<Integer> firstComponentPosition = firstComponentPosting.getPositions();
                List<Integer> secondComponentPosition = secondComponentPosting.getPositions();

                int firstPosition = 0;
                int secondPosition = 0;
                while (firstPosition < firstComponentPosition.size()) {

                    while (secondPosition < secondComponentPosition.size()) {
                        int distance = secondComponentPosition.get(secondPosition) - firstComponentPosition.get(firstPosition);

                        if (distance == k) {
                            list.add(secondComponentPosition.get(secondPosition));
                        } else if (secondComponentPosition.get(secondPosition) > firstComponentPosition.get(firstPosition)) {
                            break;
                        }

                        secondPosition++;
                    }

                    while (!list.isEmpty() && abs(list.get(0) - firstComponentPosition.get(firstPosition)) > k) {
                        list.remove(0);
                    }
                    if (!list.isEmpty()) {
                        list.add(0, firstComponentPosition.get(firstPosition));
                        listFinal.addAll(list);
                    }
                    firstPosition++;
                }
                if (!listFinal.isEmpty()) {
                    result.add(new Posting(firstComponentPosting.getDocumentId(), listFinal));

                }
                i++;
                j++;

            } else if (firstComponentPosting.getDocumentId() < secondComponentPosting.getDocumentId()) {
                i++;
            } else {
                j++;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        String terms
                = mComponents.stream()
                        .map(c -> c.toString())
                        .collect(Collectors.joining(" "));
        return "\"" + terms + "\"";
    }
}
