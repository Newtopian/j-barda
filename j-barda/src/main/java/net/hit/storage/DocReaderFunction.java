package net.hit.storage;

/**
 * Function that converts a byte array into the parameterized type instance
 * 
 * Essentially reverts the operation done by a {@link DocWriterFunction}.
 *
 * @param <Out>
 *          the type to convert to
 */
public interface DocReaderFunction<Out> {

	/**
	 * Convert a byte array into an instance of parametersized type.
	 * 
	 * @param in
	 *          the byte array
	 * @return an instance of Type <Out>
	 */
	public Out convert(byte[] in);
}
