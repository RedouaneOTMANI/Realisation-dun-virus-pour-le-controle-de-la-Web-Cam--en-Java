package brett;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import brett.Brett.Zug;
import figuren.Figur;

/**
 * Die Grafik des Schachbretts
 */
@SuppressWarnings("serial")
public class BrettPanel extends JPanel {
	/**
	 * Die Kantenlaenge eines Schachfeldes in Pixeln
	 */
	private static int feldSize;
	static {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		if (screenSize.height / 8 * 0.85 > 80
				&& screenSize.width / 8 * 0.85 > 80) {
			feldSize = 80;
		} else {
			feldSize = screenSize.height < screenSize.width ? (int) (screenSize.height / 8 * 0.85)
					: (int) (screenSize.width / 8 * 0.85);
		}
	}

	/**
	 * Die Farben des Schachbretts
	 */
	private static final Color dunkel = new Color(139, 69, 19);
	private static final Color hell = new Color(255, 165, 79);

	/**
	 * Das graphisch angezeigte Brett.
	 */
	private Brett brett;
	
	/**
	 * Das angezeigte Bild
	 */
	private Image bild;

	/**
	 * Das dazugehoerige Graphics-Element
	 */
	private Graphics graphics;
	
	/**
	 * Das Bild, welches ein Feld hervorhebt, wenn es ausgewaehlt wurde.
	 */
	private BufferedImage highlightBild;
	
	/**
	 * Der MouseListener dieser Klasse
	 */
	private BrettMouseListener mouseListener = new BrettMouseListener();
	
	/**
	 * Der (bei menschlichen Spielern) auf Mausklicks reagierende MouseListener
	 * des Bretts.
	 */
	private class BrettMouseListener implements MouseListener {

		/**
		 * Die Position der evtl. selektierten Figur.
		 */
		private Position selektiertePosition;

		/**
		 * Resettet den Mouselistener
		 */
		public void reset() {
			selektiertePosition = null;
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			/*
			 * Wenn ein PC-Spieler am Zug ist, koennen Mausklicks igoriert
			 * werden.
			 */
			if (!brett.spielerDerFarbe(brett.aktuelleFarbe()).istMensch()) {
				return;
			}

			// Koordinaten des angeklickten Feldes
			Position clickedPos = new Position((event.getX() / feldSize) + 1,
					(event.getY() / feldSize) + 1);
			Figur figur = brett.getFigur(clickedPos.getX(), clickedPos.getY());

			if (figur != null && figur.getFarbe() == brett.aktuelleFarbe()) {
				if (selektiertePosition != null) {
					// alte selektierte Position unhighlighten
					refresh(selektiertePosition);
				}
				if (clickedPos.equals(selektiertePosition)) {
					// Deselektion
					selektiertePosition = null;
				} else {
					// Selektion einer Figur (muss selbe Farbe haben)
					selektiertePosition = clickedPos;
					highlight(clickedPos);
				}
			} else {
				// leeres Feld oder feindliche Figur angeklickt
				if (selektiertePosition != null) {
					/*
					 *  Vollziehe den gewaehlten Zug einer vorher selektierten Figur.
					 */
					Zug zug = brett.new Zug(selektiertePosition, clickedPos);
					if (zug.istGueltig()) {
						zug.fuehreAus(true);
					} else {
						MainWindow.playSound("Aktion_verboten");
					}

					if(selektiertePosition != null) {
						refresh(selektiertePosition);
						selektiertePosition = null;
					}
				} else {
					MainWindow.playSound("Aktion_verboten");
				}
			}
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}
	}

	/**
	 * Erzeugt das BrettPanel und ein dazugehoeriges Brett.
	 */
	public BrettPanel() {
		brett = new Brett(this);

		// Groesse der Canvas
		setPreferredSize(new Dimension(8 * feldSize, 8 * feldSize));

		// HighlightBild setzen
		URL path = getClass().getResource("/img/Highlight."  + Figur.imgEnding);
		try {
			// JPG / PNG
			highlightBild = ImageIO.read(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialisiert die GUI (nur das Brett, ohne Figuren)
	 */
	public void initializeGui() {
		bild = createImage(8 * feldSize, 8 * feldSize);
		graphics = bild.getGraphics();

		// Dunkleres als Hintergrundfarbe
		graphics.setColor(dunkel);
		graphics.fillRect(0, 0, 8 * feldSize, 8 * feldSize);

		// Hellere Quadrate darauf
		graphics.setColor(hell);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				int y = i * feldSize;
				int x = (2 * j + (i % 2)) * feldSize;

				graphics.fillRect(x, y, feldSize, feldSize);
			}
		}
	}
	
	/**
	 * Aktualisiert ein Feld auf dem Brett. repaint() muss allerdings noch
	 * aufgerufen werden
	 * 
	 * @param pos
	 *            die zu aktualisierende Position
	 */
	public void refresh(Position pos) {
		// mit Hintergrundfarbe fuellen
		graphics.setColor((pos.getX() + pos.getY()) % 2 == 0 ? hell
				: dunkel);
		graphics.fillRect((pos.getX() - 1) * feldSize, (pos.getY() - 1)
				* feldSize, feldSize, feldSize);

		// ggf. auch das Bild einer Figur zeichnen.
		Figur figur;
		if ((figur = brett.getFigur(pos.getX(), pos.getY())) != null) {
			BufferedImage img = figur.getBild();
			graphics.drawImage(img, (pos.getX() - 1) * feldSize,
					(pos.getY() - 1) * feldSize, feldSize, feldSize, null);
		}
	}

	/**
	 * Hebt ein Feld hervor. Unhighlight kann mittels refresh getan werden. Repaint muss
	 * noch aufgerufen werden.
	 * 
	 * @param pos
	 *            die Position des Feldes
	 */
	public void highlight(Position pos) {
		graphics.drawImage(highlightBild, (pos.getX() - 1)
				* feldSize, (pos.getY() - 1) * feldSize, feldSize, feldSize,
				null);
	}
	
	/**
	 * @return das angezeigt Brett
	 */
	public Brett getBrett() {
		return brett;
	}
	
	/**
	 * Fuegt den MouseListener hinzu.
	 */
	public void addMouseListener() {
		addMouseListener(mouseListener);
	}
	
	/**
	 * Entfernt und resettet den Mouselistener.
	 */
	public void removeMouseListener() {
		removeMouseListener(mouseListener);
		mouseListener.reset();
	}

	/**
	 * Diese Methode muss ueberschrieben werden, da sonst nichts (!) gezeichnet
	 * wird.
	 * 
	 * @param g
	 *            zu zeichnende Grafik
	 */
	public void paint(Graphics g) {
		g.drawImage(bild, 0, 0, null);
	}
}