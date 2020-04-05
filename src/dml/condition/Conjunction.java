package dml.condition;

import ddl.catalog.Attribute;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents Conjunction
 */
public class Conjunction implements Resolvable {

    /**
     * The clause on the LHS
     */
    private final Resolvable LHS;

    /**
     * The clause on the RHS
     */
    private final Resolvable RHS;

    Conjunction(Resolvable lhs, Resolvable rhs) {
        LHS = lhs;
        RHS = rhs;
    }

    @Override
    public Set<Object[]> resolveAgainst(Set<Object[]> records) {
        Set<Object[]> lhsResult = LHS.resolveAgainst(records);
        return RHS.resolveAgainst(lhsResult);
    }

    @Override
    public Set<Attribute> getUsedAttributes() {
        Set<Attribute> usedAttr = LHS.getUsedAttributes();
        usedAttr.addAll(RHS.getUsedAttributes());
        return usedAttr;
    }
}
