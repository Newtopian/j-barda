/**
 * Generates the raw bytes for the given document
 * 
 * @param <T>
 *          the Object type to marshal
 * 
 *
 */
public interface RawDocByteMarshaller<T extends Object> {

	/**
	 * Marshal the object as a byte array
	 * 
	 * @param object
	 *          the object to marshal
	 * @return the byte array
	 */
	byte[] marshall(T object);
}
