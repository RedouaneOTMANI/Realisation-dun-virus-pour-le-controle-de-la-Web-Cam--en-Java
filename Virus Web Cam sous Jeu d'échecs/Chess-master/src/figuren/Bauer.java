package figuren;

import brett.Brett;
import brett.Position;

/**
 * Ein Bauer
 */
public class Bauer extends Figur {

	public static final int wertigkeit = 1;

	/**
	 * {@inheritDoc}
	 */
	public Bauer(Brett brett, Position position, Farbe farbe) {
		super(brett, position, farbe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean istErlaubteBewegung(int x, int y) {
		switch (farbe) {
		case WEISS:
			if ((position.getY() == 7 && y == -2 && x == 0 && brett.getFigur(
					position.getX(), position.getY() - 1) == null)
					|| (y == -1 && x == 0)) {
				return true;
			}
			break;

		case SCHWARZ:
			if ((position.getY() == 2 && y == 2 && x == 0 && brett.getFigur(
					position.getX(), position.getY() + 1) == null)
					|| (y == 1 && x == 0)) {
				return true;
			}
			break;
		}

		/*
		 * En Passant Schlaege fallen hier unter Zuege, da keine Figur
		 * angeklickt wird. Sie muessen also extra geprueft werden. Dafuer kann
		 * nicht istErlaubterSchlag genommen werden: Die Methode prueft die
		 * Farbe der Figur, wo hingeklickt worden ist.
		 */
		return istEnPassant(x, y);
	}

	/**
	 * Ein Bauer schlaegt schraeg.
	 */
	@Override
	public boolean istErlaubterSchlag(int x, int y) {
		switch (farbe) {
		case WEISS:
			if (!(y == -1 && (x == 1 || x == -1))) {
				return false;
			}
			break;
		case SCHWARZ:
			if (!(y == 1 && (x == 1 || x == -1))) {
				return false;
			}
			break;
		}

		return istFigurAndererFarbe(position.getX() + x, position.getY() + y);
	}

	/**
	 * Prueft, ob dies ein Zug ein gueltiger en Passant-Schlag ist.
	 * 
	 * En Passant darf geschlagen werden, wenn direkt neben einem ein
	 * feindlicher Bauer steht, der gerade zuvor seinen ersten Zug mit zwei
	 * Schritten vorwaerts getan hat. Dabei wird schraeg auf ein leeres Feld
	 * geschlagen.
	 * 
	 * @param x
	 *            X-Bewegung
	 * @param y
	 *            Y-Bewegung
	 * @return ob en Passant Schlag
	 */
	private boolean istEnPassant(int x, int y) {
		switch (farbe) {
		case WEISS:
			if (!(y == -1 && (x == 1 || x == -1) && position.getY() == 4)) {
				return false;
			}
			break;
		case SCHWARZ:
			if (!(y == 1 && (x == 1 || x == -1) && position.getY() == 5)) {
				return false;
			}
			break;
		}

		// Hole die evtl. zu schlagende Figur
		Figur figur = brett.getFigur(position.getX() + x, position.getY());

		/*
		 * Pruefe ob vorhanden, Bauer, zuletzt bewegt, und nur einen Zug
		 * (impliziert andere Farbe). Die richtige Reihe wurde bereits oben
		 * geprueft.
		 */
		return ((figur != null) && (figur instanceof Bauer)
				&& (figur == brett.getZuletztBewegteFigur()) && (figur
				.getZuege() == 1));
	}

	/**
	 * Prueft, ob dieser Bauer auf die Position schlagen koennte (en Passant
	 * ignoriert).
	 * 
	 * @param pos
	 *            die zu pruefende Position
	 * @return ob dieser Bauer dorthin schlagen koennte
	 */
	public boolean istBedrohtesFeld(Position pos) {
		int y = farbe == Farbe.WEISS ? position.getY() - 1
				: position.getY() + 1;

		return y == pos.getY()
				&& (pos.getX() - position.getX() == 1 || pos.getX()
						- position.getX() == -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWertigkeit() {
		return wertigkeit;
	}
}