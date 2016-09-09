/**
 * a Document Builder
 * 
 * @author edaigneault
 * @param <T>
 *          the Document Object type
 *
 */
public class DocBuilder<T extends Object> {

	private IDGenerator<T>					idGenerator;
	private RawDocByteMarshaller<T>	byteGen;

	protected DocBuilder()
	{

	}

	/**
	 * Set the ID generator for the Doc
	 * 
	 * @param idGenerator
	 *          the IdGenerator
	 * @return the DocBuilder
	 */
	public DocBuilder<T> as(IDGenerator<T> idGenerator)
	{
		this.idGenerator = idGenerator;
		return this;
	}

	/**
	 * Set the Marshaler to use when saving the document
	 * 
	 * @param marshaller
	 *          the byte generator for the given object
	 * @return this doc builder
	 */
	public DocBuilder<T> frozen(RawDocByteMarshaller<T> marshaller)
	{
		this.byteGen = marshaller;
		return this;
	}

	/**
	 * Creates the Doc object from this parametrized DocBuilder
	 * 
	 * @param docObject
	 *          the DocObject to store
	 * @return the Doc wrapper
	 */
	public Doc doc(T docObject)
	{
		return new Doc(this.byteGen.marshall(docObject), this.idGenerator.generate(docObject));
	}
}
