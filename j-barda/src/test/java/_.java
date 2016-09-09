import java.nio.file.Path;

/**
 * @author edaigneault
 *
 */
public class _ {

	/**
	 * Creates a file based store rooted at the given Path.
	 * 
	 * @param path
	 *          the store's root path
	 * @return a store builder Instance
	 */
	public static _ rootedAt(Path path)
	{
		return new _(path);
	}

	// public static DocBuilder $(byte[] docContent)
	// {
	//
	// }

	private Path	storeRoot;

	private _(Path path)
	{
		this.storeRoot = path;
	}

	/**
	 * Switches the context to the provided Cabinet
	 * 
	 * @param cabinetName
	 *          the cabinetName to switch to
	 * @return the Store's Cabinet with the provided name
	 */
	public Cab inCab(String cabinetName) {
		return new Cab(this.storeRoot.resolve(cabinetName));
	}
}
