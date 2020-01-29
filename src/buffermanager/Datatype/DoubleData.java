package buffermanager.Datatype;

public class DoubleData extends StaticData<Double> {

    protected DoubleData() {
        super(ValidDataTypes.DOUBLE);
    }

    @Override
    public Double resolveData() {
        return null;
    }
}
