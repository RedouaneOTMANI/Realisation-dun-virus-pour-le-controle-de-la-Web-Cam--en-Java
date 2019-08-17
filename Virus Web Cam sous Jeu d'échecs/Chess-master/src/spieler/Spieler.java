package spieler;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import brett.Brett;
import brett.Position;
import brett.Brett.Zug;

import figuren.Dame;
import figuren.Figur;
import figuren.Koenig;
import figuren.Laeufer;
import figuren.Springer;
import figuren.Turm;

/**
 * Ein Mitspieler einer Partie
 */
public class Spieler {
	
	/**
	 * Das Brett auf dem der Spieler spielt.
	 */
	protected Brett brett;

	/**
	 * Die Figuren, die dieser Spieler (noch) besitzt.
	 */
	protected ArrayList<Figur> figuren;

	/**
	 * Eine Referenz auf den Koenig.
	 */
	protected Koenig koenig;

	/**
	 * Ein neuer Spieler.
	 * 
	 * @param brett
	 * 			  Das Brett auf dem der Spieler spielt
	 */
	public Spieler(Brett brett) {
		this.brett = brett;
		this.figuren = new ArrayList<Figur>(16);
	}

	/**
	 * Fuegt eine Figur zur Liste hinzu
	 * 
	 * @param figur
	 *            die hinzuzufuegende Figur
	 */
	public void addFigur(Figur figur) {
		figuren.add(figur);

		if (figur instanceof Koenig) {
			koenig = (Koenig) figur;
		}
	}

	/**
	 * Loescht eine Figur aus der Liste
	 * 
	 * @param figur
	 *            die zu loeschende Figur
	 */
	public void removeFigur(Figur figur) {
		figuren.remove(figur);
	}

	/**
	 * @return eine Liste aller Figuren des Spielers
	 */
	public ArrayList<Figur> getFiguren() {
		return figuren;
	}

	/**
	 * @return den Koenig des Spielers
	 */
	public Koenig getKoenig() {
		return koenig;
	}

	/**
	 * @return die Klasse der Figur, in die sich der Bauer verwandeln soll.
	 */
	public Class<? extends Figur> getNewBauerClass() {

		// RadioButtons erstellen
		JRadioButton dameButton = new JRadioButton("Dame");
		dameButton.setActionCommand("Dame");
		dameButton.setSelected(true);

		JRadioButton turmButton = new JRadioButton("Turm");
		turmButton.setActionCommand("Turm");

		JRadioButton laeuferButton = new JRadioButton("L�ufer");
		laeuferButton.setActionCommand("Laeufer");

		JRadioButton springerButton = new JRadioButton("Springer");
		springerButton.setActionCommand("Springer");

		ButtonGroup group = new ButtonGroup();
		group.add(dameButton);
		group.add(turmButton);
		group.add(laeuferButton);
		group.add(springerButton);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(dameButton);
		panel.add(turmButton);
		panel.add(laeuferButton);
		panel.add(springerButton);

		Object[] paneContent = new Object[] { "Bitte wählen sie eine Figur:",
				panel };

		// Fragedialog anzeigen
		JOptionPane pane = new JOptionPane(paneContent,
				JOptionPane.PLAIN_MESSAGE);
		pane.createDialog("Bauer Verwandlung").setVisible(true);

		// Ergebnis ermitteln
		String result = group.getSelection().getActionCommand();

		if (result.equals("Dame")) {
			return Dame.class;
		} else if (result.equals("Turm")) {
			return Turm.class;
		} else if (result.equals("Laeufer")) {
			return Laeufer.class;
		} else if (result.equals("Springer")) {
			return Springer.class;
		}

		return null;
	}

	/**
	 * Diese Methode sollte von PC-Gegner-Subklassen ueberschrieben werden, und
	 * den PC-Gegner seinen Zug vollziehen lassen.
	 */
	public void notifyTurn() {
		// Nichts tun
	}

	/**
	 * Diese Methode sollte von PC-Gegner-Subklassen ueberschrieben werden.
	 * 
	 * @return ob dieser Spieler menschlich ist.
	 */
	public boolean istMensch() {
		return true;
	}

	/**
	 * @return die Gesamt-Wertigkeit aller Figuren des Spielers
	 */
	public int figurenWertigkeit() {
		int summe = 0;
		for (Figur figur : figuren) {
			summe += figur.getWertigkeit();
		}
		return summe;
	}

	/**
	 * @return eine Liste aller moeglichen Zuege, sortiert nach flacher
	 *         Wertigkeit
	 */
	public ArrayList<Zug> generiereMoeglicheZuege() {
		ArrayList<Zug> result = new ArrayList<Zug>();

		for (int i = 0; i < figuren.size(); i++) {
			Figur figur = figuren.get(i);

			for (int x = 1; x <= 8; x++) {
				for (int y = 1; y <= 8; y++) {
					Position newPos = new Position(x, y);
					
					Zug zug;
					if ((zug = brett.new Zug(figur.getPosition(), newPos))
							.istGueltig()) {
						/*
						 * Der Zug muss hinzugefuegt werden. Berechne die
						 * Position in der Liste.
						 */
						int wertigkeit = zug.wertigkeit();
						int pos = result.size();
						for (int j = 0; j < result.size(); j++) {
							if (wertigkeit > result.get(j).wertigkeit()) {
								pos = j;
								break;
							}
						}

						// Und fuege dort hinzu.
						result.add(pos, zug);
					}
				}
			}
		}
		return result;
	}
}