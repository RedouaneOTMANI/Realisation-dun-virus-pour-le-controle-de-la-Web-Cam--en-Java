package figuren;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import brett.Brett;
import brett.Position;

/**
 * Eine Schachfigur
 */
public abstract class Figur implements Cloneable {
	public enum Farbe {
		WEISS, SCHWARZ;

		public Farbe andereFarbe() {
			if (this == WEISS) {
				return SCHWARZ;
			} else {
				return WEISS;
			}
		}
	};

	public static String imgEnding = "png";

	/**
	 * Position
	 */
	protected Position position;

	/**
	 * Speichert die Zuege dieser Figur. Wichtig fuer Zuge wie en Passant oder
	 * Rochade.
	 */
	protected int zuege;

	/**
	 * Die Farbe der Spielfigur
	 */
	protected Farbe farbe;

	/**
	 * Das Bild der Figur
	 */
	private BufferedImage bild;
	
	/**
	 * Das Brett, auf dem die Figur steht.
	 */
	protected Brett brett; //TODO

	/**
	 * @param position
	 *            die Position der Figur
	 * @param farbe
	 *            die Farbe der Figur
	 */
	protected Figur(Brett brett, Position position, Farbe farbe) {
		this.brett = brett;
		this.farbe = farbe;
		this.zuege = 0;
		brett.figurSetPosition(this, position);
	}

	/** 
	 * @return Wertigkeit der Figur
	 */
	public abstract int getWertigkeit();

	/**
	 * @return Farbe der Figur
	 */
	public Farbe getFarbe() {
		return farbe;
	}

	/**
	 * Gibt das Bild der Figur. Ist dieses noch nicht initialisiert, so
	 * geschieht das hier.
	 * 
	 * @return Bild der Figur
	 */
	public BufferedImage getBild() {
		if (bild == null) {
			URL path = getClass().getResource("/img/" + getClass().getSimpleName() + "_"
					+ farbe.toString().toLowerCase() + "."  + Figur.imgEnding);
			try {
				// JPG / PNG
				bild = ImageIO.read(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bild;
	}

	/**
	 * @return Position der Figur
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Setzt die Position der Figur
	 * 
	 * @param newPos
	 *            die neue Position der Figur
	 */
	public void setPosition(Position newPos) {
		position = newPos;
	}

	/**
	 * @return Anzahl der Zuege dieser Figur
	 */
	public int getZuege() {
		return zuege;
	}

	/**
	 * Erhoeht die Zuganzahl
	 */
	public void incrementZuege() {
		zuege++;
	}

	/**
	 * Senkt die Zuganzahl
	 */
	public void decrementZuege() {
		zuege--;
	}

	/**
	 * Gibt zurueck, ob eine Bewegung (zu einem leeren Feld) erlaubt ist.
	 * Erwartet werden NICHT die neuen Koordinaten, sondern die Differenz zu den
	 * jetzigen.
	 * 
	 * @param x
	 *            x_then - x_now
	 * @param y
	 *            y_then - y_now
	 * @return ob Bewegung erlaubt
	 */
	public abstract boolean istErlaubteBewegung(int x, int y);

	/**
	 * Gibt zurueck, ob ein Schlag erlaubt ist. Erwartet werden NICHT die neuen
	 * Koordinaten, sondern die Differenz zu den jetzigen.
	 * 
	 * Wenn Bewegung und Schlag anders geregelt sind, muss diese Methode von
	 * Subklassen ueberschrieben werden.
	 * 
	 * @param x
	 *            x_then - x_now
	 * @param y
	 *            y_then - y_now
	 * @return ob Bewegung erlaubt
	 */
	public boolean istErlaubterSchlag(int x, int y) {
		if (!(istFigurAndererFarbe(position.getX() + x, position.getY() + y))) {
			return false;
		}

		return istErlaubteBewegung(x, y);
	}

	/**
	 * Prueft ob die Figur an dieser Stelle anderer Farbe ist
	 * 
	 * @param x
	 *            X-Koordinate
	 * @param y
	 *            Y-Koordinate
	 * @return ob Figur (wenn da) anderer Farbe ist.
	 */
	protected boolean istFigurAndererFarbe(int x, int y) {
		Figur figur;
		if ((figur = brett.getFigur(x, y)) == null) {
			return false;
		}

		return figur.getFarbe() != farbe;
	}

	/**
	 * Prueft, ob eine gerade Bewegung zu einem Feld vollzogen werden soll, und
	 * sich keine Figuren dazwischen Befinden.
	 * 
	 * @param x
	 *            x_then - x_now
	 * @param y
	 *            y_then - y_now
	 * @return ob Bewegung gerade und felder Frei
	 */
	protected boolean istFreieGeradeBewegung(int x, int y) {
		if (x == 0 && y == 0) {
			return false;
		}

		if (x == 0) {
			// Vertikale Bewegung, y-Richtung als 1 oder -1 speichern
			int yMod = y / Math.abs(y);

			for (int yAct = position.getY() + yMod; yAct != y + position.getY(); yAct += yMod) {
				if (brett.getFigur(position.getX(), yAct) != null) {
					return false;
				}
			}
			return true;

		} else if (y == 0) {
			// Horizontale Bewegung, x-Richtung als 1 oder -1 speichern
			int xMod = x / Math.abs(x);

			for (int xAct = position.getX() + xMod; xAct != x + position.getX(); xAct += xMod) {
				if (brett.getFigur(xAct, position.getY()) != null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Prueft, ob eine diagonale Bewegung zu einem Feld vollzogen werden soll,
	 * und sich keine Figuren dazwischen Befinden.
	 * 
	 * @param x
	 *            x_then - x_now
	 * @param y
	 *            y_then - y_now
	 * @return ob Bewegung diagonal und felder Frei
	 */
	protected boolean istFreieDiagonaleBewegung(int x, int y) {
		if (x == 0 && y == 0) {
			return false;
		}

		if (!(x == y || x == -y)) {
			return false;
		}

		// Richtung als 1 oder -1 speichern
		int xMod = x / Math.abs(x);
		int yMod = y / Math.abs(y);

		// Von aktueller zu ggf. neuer Position alle Felder pruefen
		int yAct = position.getY() + yMod;
		for (int xAct = position.getX() + xMod; xAct != x + position.getX(); xAct += xMod, yAct += yMod) {
			if (brett.getFigur(xAct, yAct) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prueft, ob diese Figur auf diese Position schlagen koennte.
	 * 
	 * Diese Methode funktioniert fuer alle Figuren bis auf Bauern. Sie muss
	 * dort also ueberschrieben werden!
	 * 
	 * @param pos
	 *            die zu pruefende Position
	 * @return ob die Figur dorthin schlagen koennte
	 */
	public boolean istBedrohtesFeld(Position pos) {
		/**
		 * Hier wird Schach ignoriert, da eine den Koenig bedrohende Figur, die
		 * an sich nicht ziehen koennte, da der eigene Koenig ins Schach
		 * rutscht, dennoch den anderen Koenig zur Deckung zwingt, denn dieser
		 * steht im Schach.
		 */
		return istErlaubteBewegung(pos.getX() - position.getX(), pos.getY()
				- position.getY());
	}
	
	/**
	 * Klont eine Figur und weist ihr das uebergebene Brett zu. Sie wird dabei auch
	 * auf dem uebergebenen Brett plaziert. Diese Methode soll im Laufe des Klonens
	 * eines Brettes aufgerufen werden und ist daher nicht geeignet, um Figuren auf
	 * demselben Brett zu klonen.
	 * 
	 * @param brett
	 * 			  das neue Brett des Klons
	 */
	public Figur clone(Brett brett) {
		Figur clone = null;
		try {
			clone = (Figur) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		
		clone.brett = brett;
		/*
		 * Position in lokale Variable verschieben, damit der Klon in
		 * figurSetPosition dem Spieler hinzugefuegt wird.
		 */
		Position pos = clone.position;
		clone.position = null;
		brett.figurSetPosition(clone, pos);
		
		return clone;
	}

	/**
	 * @return String-Notation einer Figur
	 */
	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " "
				+ farbe.toString().toLowerCase() + " ";
		if (position != null) {
			result += position.toString();
		} else {
			result += "pos:null";
		}
		return result;
	}
}