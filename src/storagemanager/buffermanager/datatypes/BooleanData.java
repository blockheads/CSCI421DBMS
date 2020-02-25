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


    public boolean validData(String data){
        data = data.toLowerCase().trim();
        return data.equals("true") || data.equals("false");
    }
}
