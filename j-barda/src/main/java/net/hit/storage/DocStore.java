package net.hit.storage;

import java.util.UUID;

/**
 * A DocStore is a simple document storage that can quickly store and retrieve documents based on ID (UUID).
 * 
 * The identifier is usually provided as a UUID. The document content is provided as string or as object and a document serializer can be
 * registered for a given type.
 */
public interface DocStore {

	/**
	 * Store the provided bytes as a document under the id and cabinet
	 * 
	 * @param docId
	 *          the Document's ID
	 * @param cabinetName
	 *          the cabinet's Name
	 * @param document
	 *          the bytes to store.
	 */
	void storeDocument(String cabinetName, UUID docId, byte[] document);

	/**
	 * Store the provided bytes as a document under the id and cabinet
	 * 
	 * @param docId
	 *          the Document's ID
	 * @param cabinetName
	 *          the cabinet's Name
	 * @param document
	 *          the bytes to store.
	 * @param docType
	 *          the docType to describe the object
	 */
	<IN> void storeDocument(String cabinetName, UUID docId, IN document, DocType<IN> docType);

	/**
	 * @param docID
	 * @param cabinetName
	 * @return the document Bytes
	 */
	byte[] getDocumentBytes(String cabinetName, UUID docID);

	/**
	 * Draws a document converting it to type OUT automatically.
	 * 
	 * For this to work you must have registered a DocType beforehand.
	 * 
	 * @param cabinetName
	 * @param docID
	 * @param docType
	 * @return an instance
	 */
	<OUT> OUT getDocument(String cabinetName, UUID docID, DocType<OUT> docType);
}
