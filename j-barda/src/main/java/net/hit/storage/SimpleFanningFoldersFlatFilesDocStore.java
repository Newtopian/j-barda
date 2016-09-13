package net.hit.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores documents in a folder hierarchy from the DocStore Root.
 * 
 * at the root a first set of folders denotes Cabinets. Cabinets are named sub-storage area so to allow multiple stores to live under the
 * same roof.
 * 
 * Then storage units are created by fanning folders based on the document's key hashcode.
 * 
 * each eight bits of the hashcode starting with the most significant create a sub-folder named after the hex code of these 8 bits. The
 * document is stored using it's UUID key as a document name inside the leaves folder. this tree is therefore always 4 level deep (5 if we
 * include the cabinet name).
 * 
 * It will create a very sparse folder tree and is quite typical to get many more folders then there are actual documents.
 * 
 * This should alleviate problems when trying to load from a folder that contains a large amount of files in it as the most sub-folders for
 * any given folder is naturally limited at 255. The leaves folder do not have a set limit but it is hoped that the fanning out will allow a
 * very large amount of document to be stored before we need to worry with the file system performance.
 * 
 * Keyed access are very fast too as we can always re-create the exact path of the file using just the key and the cabinet name no search
 * necessary.
 * 
 * Searches however tend to be much slower as we need to walk up and down a very sparse tree. Perhaps some optimization can be done here
 * later.
 * 
 * Right not only binary form is supported, thus all files receive the .bytes extension
 * 
 *
 */
public class SimpleFanningFoldersFlatFilesDocStore implements DocStore {

	private Path						storeRoot;

	private DocType<byte[]>	bytesDocType	= new DocType<byte[]>("bytes",
																						new DocReaderFunction<byte[]>() {

																							@Override
																							public byte[] convert(byte[] in) {
																								return in;
																							}
																						},
																						new DocWriterFunction<byte[]>() {

																							@Override
																							public byte[] convert(byte[] objectToWrite) {
																								return objectToWrite;
																							}
																						}
																						)
																						{};

	/**
	 * Create an instance at the given root directory.
	 * 
	 * 
	 * @param storeRoot
	 *          the store's root path
	 * @throws IOException
	 *           if the path cannot be created if it does not exist
	 */
	public SimpleFanningFoldersFlatFilesDocStore(Path storeRoot) throws IOException {
		this.storeRoot = storeRoot;
		Files.createDirectories(this.storeRoot);
	}

	@Override
	public void storeDocument(String cabinetName, UUID docId, byte[] document) {
		this.storeDocument(cabinetName, docId, document, this.bytesDocType);
	}

	@Override
	public byte[] getDocumentBytes(String cabinetName, UUID docID) {
		return this.getDocument(cabinetName, docID, this.bytesDocType);
	}

	@Override
	public <IN> void storeDocument(String cabinetName, UUID docId, IN document, DocType<IN> docType) {
		Path filePath = this.locateFile(cabinetName, docId, docType.getDocTypeName());
		try {
			Files.createDirectories(filePath.getParent());
			Files.write(filePath, docType.getWriter().convert(document));
		}
		catch (IOException e) {
			// TODO change this to a proper exception and make part of the DocStore contract
			throw new RuntimeException(String.format("Could not write the file [%s] to the store", filePath.toAbsolutePath()), e);
		}
	}

	@Override
	public <OUT> OUT getDocument(String cabinetName, UUID docID, DocType<OUT> docType) {
		Path filePath = this.locateFile(cabinetName, docID, "bytes");

		try {
			return docType.getReader().convert(Files.readAllBytes(filePath));
		}
		catch (IOException e) {
			// TODO change this to a proper exception and make part of the DocStore contract
			throw new RuntimeException(String.format("Could not read the file [%s] from the store", filePath.toAbsolutePath()), e);
		}
	}

	protected Path locateBucket(String cabinetName, UUID key) {
		int bucketPathID = key.hashCode();

		String part4 = String.format("%02x", (byte) bucketPathID);
		String part3 = String.format("%02x", (byte) (bucketPathID >> 8));
		String part2 = String.format("%02x", (byte) (bucketPathID >> 16));
		String part1 = String.format("%02x", (byte) (bucketPathID >> 24));

		return storeRoot.resolve(cabinetName).resolve(part1).resolve(part2).resolve(part3).resolve(part4);
	}

	protected Path locateFile(String cabinetName, UUID messageID, String extention) {
		if (extention == null) extention = "";
		else if (!extention.isEmpty() && !extention.startsWith(".")) extention = "." + extention;

		return this.locateBucket(cabinetName, messageID).resolve(messageID.toString() + extention).toAbsolutePath();
	}
}
