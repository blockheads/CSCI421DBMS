package ddl.catalog;

import storagemanager.buffermanager.datatypes.ValidDataTypes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Attribute implements Serializable {
    private final String dataType;
    private final String name;
    private final Set<Constraint> constraints;

    public Attribute(String name, String dataType) {
        this(name, dataType, new Constraint[]{});
    }

    public Attribute(String name, String dataType, Constraint... constraint) {
        this.name = name;
        this.dataType = dataType;
        this.constraints = new HashSet<>(){{addAll(Arrays.asList(constraint));}};
    }

    public String getName() {
        return name;
    }

    public void addConstraint(Constraint... constraint) {
        constraints.addAll(Arrays.asList(constraint));
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public boolean hasConstraint(Constraint constraint) {
        return constraints.contains(constraint);
    }

    public String getDataType() {
        return dataType;
    }

    public boolean sameType(String dataType) {
        return dataType.equals(this.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attribute) {
            return ((Attribute) obj).name.equals(name) && ((Attribute) obj).dataType.equals(dataType);
        }
        return false;
    }
}
