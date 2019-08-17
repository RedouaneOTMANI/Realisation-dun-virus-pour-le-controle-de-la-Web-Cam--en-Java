package figuren;
import brett.Brett;
import brett.Position;

/**
 * Eine Dame
 */
public class Dame extends Figur {
	
	public static final int wertigkeit = 9;

	/**
	 * {@inheritDoc}
	 */
	public Dame(Brett brett, Position position, Farbe farbe) {
		super(brett, position, farbe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean istErlaubteBewegung(int x, int y) {
		return istFreieDiagonaleBewegung(x, y) || istFreieGeradeBewegung(x, y);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWertigkeit() {
		return wertigkeit;
	}
}