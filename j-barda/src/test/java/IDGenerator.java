import java.util.UUID;

/**
 * Generate a UUID on the given Object
 * 
 * @author edaigneault
 *
 * @param <T>
 *          the Object Type
 */
public interface IDGenerator<T extends Object> {

	/**
	 * Generate the UUID
	 * 
	 * @param object
	 *          the object to generate the ID for
	 * @return the Generated ID
	 */
	UUID generate(T object);
}
