package storagemanager.buffermanager.diskUtils;

import java.io.*;
/**
 * Object saver is used to save objects
 *
 * @author Nicholas R. Chieppa
 */
public abstract class ObjectSaver {
    /**
     * save an object
     *
     * @param object the object to save
     * @param path   the file path
     */
    public static void save(Object object, String path, boolean useMinPageSize) {
        File file = new File(path);
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file)); RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            stream.writeObject(object);

            if (useMinPageSize && file.length() < DataManager.getPageSize()) {
                raf.setLength(DataManager.getPageSize());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * load an object
     *
     * @param location the object to load
     * @return the object loaded or null if not found
     */
    public static Object load(String location) throws FileNotFoundException, IOException{
        Object obj = null;
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(location))) {
            return stream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }
}