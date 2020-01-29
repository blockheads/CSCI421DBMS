package buffermanager.Datatype;

public class IntegerData extends StaticData<Integer> {
    protected IntegerData() {
        super(ValidDataTypes.INTEGER);
    }

    @Override
    public Integer resolveData() {
        return null;
    }
}
