package figuren;
import brett.Brett;
import brett.Position;

/**
 * Ein Springer
 */
public class Springer extends Figur {
	
	public static final int wertigkeit = 3;

	/**
	 * {@inheritDoc}
	 */
	public Springer(Brett brett, Position position, Farbe farbe) {
		super(brett, position, farbe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean istErlaubteBewegung(int x, int y) {
		return ((x == 2 || x == -2) && (y == 1 || y == -1))
				|| ((y == 2 || y == -2) && (x == 1 || x == -1));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWertigkeit() {
		return wertigkeit;
	}
}