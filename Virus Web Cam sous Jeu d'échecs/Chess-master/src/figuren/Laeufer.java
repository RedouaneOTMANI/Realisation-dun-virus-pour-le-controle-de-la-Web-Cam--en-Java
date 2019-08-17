package figuren;
import brett.Brett;
import brett.Position;

/**
 * Ein Laeufer
 */
public class Laeufer extends Figur {
	
	public static final int wertigkeit = 3;
	
	/**
	 * {@inheritDoc}
	 */
	public Laeufer(Brett brett, Position position, Farbe farbe) {
		super(brett, position, farbe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean istErlaubteBewegung(int x, int y) {
		return istFreieDiagonaleBewegung(x, y);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWertigkeit() {
		return wertigkeit;
	}
}