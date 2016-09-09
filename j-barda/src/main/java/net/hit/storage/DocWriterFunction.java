package net.hit.storage;

/**
 * Function that converts a given object in a byte array.
 *
 * Essentially reverts the operation done by {@link DocReaderFunction}
 * 
 * @param <IN>
 *          the type this converter can use
 */
public interface DocWriterFunction<IN> {
	/**
	 * @param objectToWrite
	 *          the Object to be converted in byte[]
	 * @return the byte array this object converts to
	 */
	public byte[] convert(IN objectToWrite);
}
