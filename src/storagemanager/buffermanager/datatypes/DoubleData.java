package storagemanager.buffermanager.datatypes;

import java.nio.ByteBuffer;

public class DoubleData extends StaticData<Double> {

    protected DoubleData() {
        super(ValidDataTypes.DOUBLE);
    }

    @Override
    public byte[] toByteArray(Double attribute) {
        ByteBuffer b = ByteBuffer.allocate(type.sizeInBytes);
        b.putDouble(attribute);
        return b.array();
    }

    @Override
    public Double toObject(byte[] attributes, int start) {
        ByteBuffer b = ByteBuffer.wrap(attributes, start, nextIndex());
        return b.getDouble();
    }

    @Override
    public Object parseData(String data) throws DataTypeException {
        try {
            return Double.parseDouble(data);
        } catch(NumberFormatException | NullPointerException e) {
            throw new DataTypeException("Invalid Double value. " + data);
        }

    }
}
