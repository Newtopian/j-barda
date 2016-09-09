package net.hit.storage;

/**
 * Describes a type of document.
 * 
 * Different type of document can define their own serialization techniques to and from the basic bytes naturally supported by the DocStore
 * 
 * The Name component will be used to determine which DocType the store will use to write and/or read the documents. For file based store
 * the name is also used to generate the file extension.
 * 
 * @param <T>
 *          the type of object to be consumed/returned by the DocStore.
 */
public abstract class DocType<T> {

	private String								name;
	private DocReaderFunction<T>	fileReader;
	private DocWriterFunction<T>	fileWriter;
	private Class<T>							objectType;

	/**
	 * A short name for this DocType
	 * 
	 * @return the DocType name
	 */
	public String getDocTypeName() {
		return name;
	}

	/**
	 * @return the reader function that converts the bytes to the proper document type
	 */
	public DocReaderFunction<T> getReader() {
		return fileReader;
	}

	/**
	 * @return the Writer function that converts the object into a byte array
	 */
	public DocWriterFunction<T> getWriter() {
		return fileWriter;
	}

	/**
	 * accessor to retain runtime type information lost to type erasure of Java's generic implementation.
	 * 
	 * @return the object type parameterized by <T>
	 */
	public Class<T> getObjectType()
	{
		return this.objectType;
	}

	protected DocType(String docTypeName, DocReaderFunction<T> fileReader, DocWriterFunction<T> fileWriter) {
		super();
		this.name = docTypeName;
		this.fileReader = fileReader;
		this.fileWriter = fileWriter;
	}
}
