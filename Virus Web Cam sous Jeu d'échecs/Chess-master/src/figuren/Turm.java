package figuren;
import brett.Brett;
import brett.Position;

/**
 * Ein Turm
 */
public class Turm extends Figur {
	
	public static final int wertigkeit = 5;
	
	/**
	 * {@inheritDoc}
	 */
	public Turm(Brett brett, Position position, Farbe farbe) {
		super(brett, position, farbe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean istErlaubteBewegung(int x, int y) {
		return istFreieGeradeBewegung(x, y);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWertigkeit() {
		return wertigkeit;
	}
}