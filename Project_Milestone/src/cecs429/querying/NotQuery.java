package cecs429.querying;

import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

public class NotQuery implements QueryComponent {
    private QueryComponent mComponent;

    public NotQuery(QueryComponent component) {
        this.mComponent = component;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return mComponent.getPostings(index);
    }

    @Override
    public String toString() {
        return "NOT " + mComponent.toString();
    }

    @Override
    public boolean isPositive() {
        return false;
    }
}
