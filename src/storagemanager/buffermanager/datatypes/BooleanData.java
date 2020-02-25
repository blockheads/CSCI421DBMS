package storagemanager.buffermanager.datatypes;

import java.nio.ByteBuffer;

public class BooleanData extends StaticData<Boolean> {
    protected BooleanData() {
        super(ValidDataTypes.BOOLEAN);
    }

    @Override
    public byte[] toByteArray(Boolean attribute) {
        ByteBuffer b = ByteBuffer.allocate(type.sizeInBytes);
        b.put((attribute)?(byte)1:(byte)0);
        return b.array();
    }

    @Override
    public Boolean toObject(byte[] attributes, int start) {
        return attributes[start] == 1;
    }


    public Object parseData(String data) throws DataTypeException {
        data = data.toLowerCase().trim();
        if(data.equals("true"))
            return Boolean.TRUE;
        else if(data.equals("false"))
            return Boolean.FALSE;

        throw new DataTypeException("Invalid boolean value. " + data);
    }

}
