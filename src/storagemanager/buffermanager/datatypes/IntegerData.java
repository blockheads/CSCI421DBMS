package storagemanager.buffermanager.datatypes;

import java.nio.ByteBuffer;

public class IntegerData extends StaticData<Integer> {
    protected IntegerData() {
        super(ValidDataTypes.INTEGER);
    }

    @Override
    public byte[] toByteArray(Integer attribute) {
        ByteBuffer b = ByteBuffer.allocate(type.sizeInBytes);
        b.putInt(attribute);
        return b.array();
    }

    @Override
    public Integer toObject(byte[] attributes, int start) {
        ByteBuffer b = ByteBuffer.wrap(attributes, start, nextIndex());
        return b.getInt();
    }
}
