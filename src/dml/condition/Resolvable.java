package dml.condition;

import java.util.Set;

/**
 * Something is resolvable if it is part of the where clause in a query
 *
 * @author Nicholas Chieppa
 */
public interface Resolvable {

    /**
     * Resolve a proposition against a table segment
     * @param records the records segment to check
     * @return the remaining records
     */
    public Set<Object[]> resolveAgainst(Set<Object[]> records);

}
