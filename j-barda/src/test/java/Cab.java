import java.nio.file.Path;

/**
 * A cabinet instance where Documents are actually stored against
 * 
 * @author edaigneault
 *
 */
public class Cab {

	private Path	cabinetRoot;

	protected Cab(Path cabinetRoot)
	{
		this.cabinetRoot = cabinetRoot;
	}

	// public Cab store(Doc document) {
	//
	// }
}
