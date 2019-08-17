package figuren;

import brett.Brett;
import brett.Position;

/**
 * Ein Koenig
 */
public class Koenig extends Figur {
	
	public static final int wertigkeit = 10;

	/**
	 * {@inheritDoc}
	 */
	public Koenig(Brett brett, Position position, Farbe farbe) {
		super(brett, position, farbe);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean istErlaubteBewegung(int x, int y) {
		if (x == 0 && y == 0) {
			return false;
		}

		// Normaler Zug
		if (x <= 1 && x >= -1 && y <= 1 && y >= -1) {
			return true;
		}

		/*
		 *  Auf Rochade muss noch geprueft werden.
		 *  Es muss nicht super.istErlaubteBewgung aufgerufen werden,
		 *  da istRochade selbst bereits prueft, ob der Koenig danach
		 *  bedroht waere.
		 */
		return istRochade(x, y);
	}

	/**
	 * Prueft, ob dies ein gueltiger Rochade-Zug ist. Bei einer Rochade bewegt
	 * sich der Koenig zwei Felder in Richtung Turm. Dieser springt dann ueber
	 * den Koenig auf das naechste Feld.
	 * 
	 * Eine Rochade kann nur dann ausgeführt werden, wenn
	 * 
	 * 1. der König noch nicht gezogen wurde,
	 * 
	 * 2. der beteiligte Turm noch nicht gezogen wurde,
	 * 
	 * 3. zwischen dem König und dem beteiligten Turm keine andere Figur steht,
	 * 
	 * 4. der König  über kein Feld ziehen muss, das durch eine feindliche Figur
	 * bedroht wird,
	 * 
	 * 5. der König vor und nach Ausführung der Rochade nicht im Schach steht,
	 * 
	 * 6. Turm und Koenig auf der gleichen Reihe stehen.
	 * 
	 * @param x
	 *            X-Bewegung
	 * @param y
	 *            Y-Bewegung
	 * @return ob Rochade Zug
	 */
	private boolean istRochade(int x, int y) {
		if (!(y == 0 && zuege == 0)) {
			return false;
		}
		
		int yPos = position.getY();
		Figur turm;
		
		/*
		 * kleine Rochade nach rechts
		 */
		if (x == 2
				&& (turm = brett.getFigur(8, yPos)) != null
				&& turm.getZuege() == 0
				&& turm.getFarbe() == farbe) {
			// Bedingungen 1., 2. und 6. sind erfuellt
			
			for(int xPos = position.getX() + 1; xPos <= 7; xPos++) {
				if(!(brett.istFrei(new Position(xPos, yPos)))) {
					return false;
				}
			}
			// Bedingung 3. ist erfuellt
			
			for(int xPos = position.getX(); xPos <= 7; xPos++) {
				if(brett.istBedroht(new Position(xPos, yPos), farbe)) {
					return false;
				}
			}
			// Bedingungen 4. und 5. sind erfuellt
			return true;
		}

		/*
		 * grosse Rochade nach links
		 */
		if (x == -2
				&& (turm = brett.getFigur(1, yPos)) != null
				&& turm.getZuege() == 0
				&& turm.getFarbe() == farbe) {
			// Bedingungen 1., 2. und 6. sind erfuellt
			
			for(int xPos = position.getX() - 1; xPos >= 2; xPos--) {
				if(!(brett.istFrei(new Position(xPos, yPos)))) {
					return false;
				}
			}
			// Bedingung 3. ist erfuellt
			
			for(int xPos = position.getX(); xPos >= 3; xPos--) {
				if(brett.istBedroht(new Position(xPos, yPos), farbe)) {
					return false;
				}
			}
			// Bedingungen 4. und 5. sind erfuellt
			return true;
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWertigkeit() {
		return wertigkeit;
	}
}