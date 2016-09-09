import java.util.UUID;

/**
 * wraps a stored document
 */
public class Doc {

	protected Doc(byte[] docContent, UUID docID)
	{
		this.docContent = docContent;
		this.docID = docID;
	}

	private byte[]	docContent;

	/**
	 * @return the Raw doc content
	 */
	public byte[] getDocContent()
	{
		return docContent;
	}

	private UUID	docID;

	/**
	 * @return the Doc storage ID
	 */
	public UUID getDocID()
	{
		return docID;
	}
}
