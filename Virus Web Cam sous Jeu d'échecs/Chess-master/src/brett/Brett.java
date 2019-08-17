package brett;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import spieler.PCSpieler;
import spieler.Spieler;
import figuren.Bauer;
import figuren.Dame;
import figuren.Figur;
import figuren.Figur.Farbe;
import figuren.Koenig;
import figuren.Laeufer;
import figuren.Springer;
import figuren.Turm;

/**
 * Die Logik des Schachbretts
 */
public class Brett implements Cloneable {
	/**
	 * Verschiedene Situationen auf dem Brett.
	 */
	public enum Situation {
		// Normale Situation
		NORMAL,
		// Eine Partei ist Schachmatt
		MATT,
		// Remis wegen Patt
		PATT,
		// Remis wegen der 50-Zuege-Regel
		R50Z;
	};

	/**
	 * Ein Zug, mit Figur, Ausgangs- und Endfeld, und ggf. geschlagener Figur.
	 * 
	 * Zuege koennen ausgefuehrt und auch wieder rueckgaengig gamcht werden.
	 */
	public class Zug {
		/**
		 * Ausgangsfeld
		 */
		private Position von;

		/**
		 * Endfeld
		 */
		private Position nach;

		/**
		 * Die den Zug vollziehende Figur
		 */
		private Figur figur;

		/**
		 * Eine ggf. geschlagene Figur
		 */
		private Figur geschlageneFigur;

		/**
		 * Bei en Passant wird hier der geschlagene Bauer gespeichert.
		 */
		private Figur enPassantBauer;

		/**
		 * Bei Rochade wird hier der Turm gespeichert.
		 */
		private Figur rochadenTurm;

		/**
		 * Bei Bauerverwandlung wird hier die neue Figur gespeichert.
		 */
		private Figur verwandlungsFigur;

		/**
		 * Ob es sich bei diesem Zug um Bauernverwandlung handelt.
		 */
		private boolean bauernVerwandlung;

		/**
		 * Die vorher zuletzt bewegte Figur.
		 */
		private Figur zuletztBewegteFigurLocal;

		/**
		 * Speichert den vorherigen Wert der gleichnamigen Variable (ohne Local)
		 * des Bretts
		 */
		private int zuegeOhneSchlagOderBauerLocal;

		/**
		 * Speichert, ob der Zug gueltig ist
		 */
		private boolean istGueltig;

		/**
		 * Zum Zeitpunkt der Erstellung muessen die Figuren auch dort stehen, wo
		 * sie ihren Zug vollziehen sollen / geschlagen werden sollen. Der Zug
		 * wird in keinster Weise geprueft, und es kann zu Fehlern kommen, wenn
		 * der Zug ungueltig ist! (vor allem bei Rochade / en Passant)
		 * 
		 * @param von
		 *            Ausgangsfeld
		 * @param nach
		 *            Zielfeld
		 */
		public Zug(Position von, Position nach) {
			this.von = von;
			this.nach = nach;
			this.figur = figuren[von.getX()][von.getY()];
			istGueltig = pruefeGueltigkeit();
		}

		/**
		 * @return Ausgangsfeld
		 */
		public Position getVon() {
			return von;
		}

		/**
		 * @return Endfeld
		 */
		public Position getNach() {
			return nach;
		}

		/**
		 * Initialisiert die Felder des Zuges.
		 */
		private void initialize() {
			this.geschlageneFigur = figuren[nach.getX()][nach.getY()];
			this.zuegeOhneSchlagOderBauerLocal = zuegeOhneSchlagOderBauer;
			this.zuletztBewegteFigurLocal = zuletztBewegteFigur;

			if (figur instanceof Bauer && geschlageneFigur == null
					&& nach.getX() - von.getX() != 0) {
				// Es handelt sich um en Passant.
				enPassantBauer = figuren[nach.getX()][von.getY()];
			} else if (figur instanceof Bauer
					&& (nach.getY() == 1 || nach.getY() == 8)) {
				// Es handelt sich um Bauernverwandlung.
				bauernVerwandlung = true;
			}
		}

		/**
		 * @return die Wertigkeit einer ggf. geschlagenen Figur, sonst 0.
		 *         Wertigkeitsgewinn durch Bauernverwandlung wird
		 *         beruecksichtigt.
		 */
		public int wertigkeit() {
			if (enPassantBauer != null) {
				return enPassantBauer.getWertigkeit();
			}

			int wert = 0;
			if (bauernVerwandlung) {
				wert = Dame.wertigkeit - figur.getWertigkeit();
			}

			if (geschlageneFigur != null) {
				return wert + geschlageneFigur.getWertigkeit();
			} else {
				return wert;
			}
		}

		/**
		 * @return die berechnete Gueltigkeit des Zuges
		 */
		public boolean istGueltig() {
			return istGueltig;
		}

		/**
		 * @return ob es sich um einen gueltigen Zug handelt. Die PrÃ¼fung muss
		 *         dann erfolgen, wenn die Figurenkonstellation auf dem Brett
		 *         auch die gewuenschte fuer die Ausfuehrung ist.
		 */
		private boolean pruefeGueltigkeit() {
			if (!((istFrei(nach) && figur.istErlaubteBewegung(
					nach.getX() - von.getX(), nach.getY() - von.getY())) || figur
					.istErlaubterSchlag(nach.getX() - von.getX(), nach.getY()
							- von.getY()))) {
				return false;
			}
			initialize();

			/*
			 * Im folgenden wird auf eine moegliche eigene Schachstellung nach dem
			 * Zug geprueft. In diesem Fall waere der Zug natuerlich nicht
			 * gueltig.
			 */
			// Setze die neue Konstellation temporaer
			fuehreAus(false);

			// Speichere das Endergebnis zwischen
			boolean result = !istSchach(figur.getFarbe(), false);

			// Stelle den urspruenglichen Zustand wieder her
			rueckgaengig();

			// Gebe das Ergebnis zurueck
			return result;
		}

		/**
		 * Setzt den Zug (temporaer).
		 * 
		 * Die Reihenfolge der Bewegung / Erzeugung / Loeschung der Figuren ist
		 * wichtig, damit beim rueckgaengig machen die Figuren wieder auf ihren
		 * urspruenglichen Positionen landen!
		 * 
		 * @param graphisch
		 *            ob das Brett graphisch veraendert und die Schachnotation
		 *            ausgegeben werden soll
		 * 
		 * @return den Zug
		 */
		public Zug fuehreAus(boolean graphisch) {
			// Zuletzt bewegte Figur neusetzen
			zuletztBewegteFigur = figur;

			// Zuege Ohne Schlag oder Bauer auf 0 oder hoeher setzen.
			zuegeOhneSchlagOderBauer = figur instanceof Bauer
					|| geschlageneFigur != null ? 0
					: zuegeOhneSchlagOderBauer + 1;

			if (geschlageneFigur != null) {
				figurSetPosition(geschlageneFigur, null);
			}

			figurSetPosition(figur, nach);
			figur.incrementZuege();

			/*
			 * Der Zug wird in Schachnotation ausgegeben (wenn endgueltig)
			 */
			String notation = "";
			if (graphisch) {
				if (!(figur instanceof Bauer)) {
					/*
					 * Ausser bei Bauern kommt der Anfangsbuchstabe der Figur an
					 * den Anfang der Notation
					 */
					notation += figur.getClass().getSimpleName().charAt(0);
				}
				// Ausgangsfeld
				notation += koordinatenSchachnotation(von);
				// auf freies Feld oder Schlag
				notation += geschlageneFigur == null ? "-" : "x";
				// Zielfeld
				notation += koordinatenSchachnotation(nach);
			}

			if (enPassantBauer != null) {
				figurSetPosition(enPassantBauer, null);

				if (graphisch) {
					// Es handelt sich um en Passant.
					notation = koordinatenSchachnotation(von) + "x"
							+ koordinatenSchachnotation(nach) + " e.p.";
					/*
					 * Das Feld des bei en Passant geschlagenen Bauern muss auch
					 * aktualisiert werden
					 */
					brettPanel.refresh(new Position(nach.getX(), von.getY()));
				}

			} else if (bauernVerwandlung) {
				// Es handelt sich um Bauernverwandlung
				figurSetPosition(figur, null);

				if (graphisch) {
					/*
					 * Die Klasse der Verwandlungsfigur ermitteln, instanziiert
					 * und gespiechert.
					 */
					Class<? extends Figur> cl = spielerDerFarbe(aktuelleFarbe())
							.getNewBauerClass();

					try {
						verwandlungsFigur = cl.getConstructor(Brett.class,
								Position.class, Figur.Farbe.class).newInstance(
								Brett.this, nach, figur.getFarbe());
					} catch (Exception e) {
						e.printStackTrace();
					}

					notation += figuren[nach.getX()][nach.getY()].getClass()
							.getSimpleName().charAt(0);
				} else {
					/*
					 * Beim testweisen Ausfuehren wird grunsaetzlich von einer
					 * Dame ausgegangen
					 */
					verwandlungsFigur = new Dame(Brett.this, nach,
							figur.getFarbe());
				}

			} else if (figur instanceof Koenig
					&& Math.abs(nach.getX() - von.getX()) == 2) {
				// Es handelt sich um Rochade

				switch (nach.getX() - von.getX()) {
				case 2:
					// kleine Rochade
					rochadenTurm = figuren[8][von.getY()];
					figurSetPosition(rochadenTurm, new Position(6, von.getY()));

					if (graphisch) {
						notation = "0-0";
						brettPanel.refresh(new Position(8, von.getY()));
						brettPanel.refresh(new Position(6, von.getY()));
					}
					break;
				case -2:
					// grosse Rochade
					rochadenTurm = figuren[1][von.getY()];
					figurSetPosition(rochadenTurm, new Position(4, von.getY()));

					if (graphisch) {
						notation = "0-0-0";

						brettPanel.refresh(new Position(1, von.getY()));
						brettPanel.refresh(new Position(4, von.getY()));
					}
					break;
				}
				rochadenTurm.incrementZuege();
			}

			if (graphisch) {
				spielerEinsAmZug = !spielerEinsAmZug;

				// Schach pruefen
				if (istSchach(aktuelleFarbe(), true)) {
					notation += "+";
				}
				MainWindow.print(notation);

				// Grafische Aktualisierung
				brettPanel.refresh(von);
				brettPanel.refresh(nach);
				brettPanel.repaint();

				/*
				 * Pruefe, ob das Spiel zu Ende ist, wegen Matt, Patt oder 50
				 * Zuege-Regel
				 */
				if (pruefeSpielZuEnde()) {
					return this;
				}
				MainWindow.print("\n");

				/*
				 * Ggf. Timer anhalten und fortsetzen. (spielerEinsAmZug wurde
				 * bereits umgestellt!)
				 */
				if (timer[0] != null) {
					if (spielerEinsAmZug) {
						timer[1].stopTimer();
						timer[0].continueTimer();
					} else {
						timer[0].stopTimer();
						timer[1].continueTimer();
					}
				}

				// Ggf. PC-Gegner benachrichtigen, dass er dran ist.
				spielerDerFarbe(aktuelleFarbe()).notifyTurn();
			}
			return this;
		}

		/**
		 * Macht den Zug wieder rueckgaengig.
		 * 
		 * Die Reihenfolge der Bewegung / Erzeugung / Loeschung der Figuren ist
		 * auch hier wichtig!
		 */
		public void rueckgaengig() {
			// Zuletzt bewegte Figur zuruecksetzen
			zuletztBewegteFigur = zuletztBewegteFigurLocal;

			// Zuege ohne Schlag oder Bauer zuruecksetzen
			zuegeOhneSchlagOderBauer = zuegeOhneSchlagOderBauerLocal;

			if (enPassantBauer != null) {
				// Es handelte sich um en Passant
				figurSetPosition(enPassantBauer,
						new Position(nach.getX(), von.getY()));

			} else if (verwandlungsFigur != null) {
				// Es handelte sich um Bauerverwandlung
				figurSetPosition(verwandlungsFigur, null);
			} else if (rochadenTurm != null) {
				// Es handelte sich um Rochade
				switch (nach.getX() - von.getX()) {
				case 2:
					// kleine Rochade
					figurSetPosition(rochadenTurm, new Position(8, von.getY()));
					break;
				case -2:
					// grosse Rochade
					figurSetPosition(rochadenTurm, new Position(1, von.getY()));
					break;
				}
				rochadenTurm.decrementZuege();
			}

			figurSetPosition(figur, von);

			figur.decrementZuege();
			if (geschlageneFigur != null) {
				figurSetPosition(geschlageneFigur, nach);
			}
		}

		/**
		 * @return String-Notation eines Zuges
		 */
		@Override
		public String toString() {
			return figur.getClass().getSimpleName() + " "
					+ figur.getFarbe().toString().toLowerCase() + " "
					+ von.toString() + "-" + nach.toString();
		}
	}

	/**
	 * Das zum Brett gehoerende BrettPanel
	 */
	private BrettPanel brettPanel;

	/**
	 * Die Schachfiguren
	 */
	private Figur[][] figuren;

	/**
	 * Die (zwei) Spieler der Partie. Spieler eins ist weiss, Spieler zwei
	 * schwarz!
	 */
	private Spieler[] spieler;

	/**
	 * Zwei bei Zeitspiel verwendete SchachUhrThreads
	 */
	private SchachUhrThread[] timer;

	/**
	 * Speichert, ob Spieler 1 am Zug ist.
	 */
	private boolean spielerEinsAmZug = true;

	/**
	 * Speichert die zuletzt Bewegte Figur. (Wichtig fuer en Passant)
	 */
	private Figur zuletztBewegteFigur;

	/**
	 * Zaehlt die Zuege ohne einen Schlag oder vorgesetzten Bauer. Nach 100
	 * Halbzuegen gibt es Remis.
	 */
	private int zuegeOhneSchlagOderBauer;

	/**
	 * Erzeugt ein neues Brett.
	 * 
	 * @param brettPanel
	 *            das Panel, auf dem das Brett angezeigt werden soll.
	 */
	public Brett(BrettPanel brettPanel) {
		this.brettPanel = brettPanel;
		spieler = new Spieler[2];
		timer = new SchachUhrThread[2];

		// Stelle 0 wird jeweils ignoriert
		figuren = new Figur[9][9];
	}

	/**
	 * Setzt das Schachfeld auf den Ausgangszustand zurueck, bzw. initialisiert
	 * es.
	 * 
	 * @param spieler1
	 *            Weisser Spieler
	 * @param spieler2
	 *            Schwarzer spieler
	 */
	public void reset(Spieler spieler1, Spieler spieler2) {
		// Ein ggf. noch laufendes Spiel sieger- und grundlos Beenden
		endGame(null, null);

		spielerEinsAmZug = true;
		zuegeOhneSchlagOderBauer = 0;
		figuren = new Figur[9][9];
		MainWindow.clearText();
		brettPanel.initializeGui();

		// Fuegt den MouseListener hinzu
		brettPanel.addMouseListener();

		spieler[0] = spieler1;
		spieler[1] = spieler2;

		Position pos;

		// Setze Einstellungen fuer Schwarz
		Farbe farbe = Farbe.SCHWARZ;
		int[] rows = new int[] { 1, 2 };

		for (int i = 0; i < 2; i++) {
			for (int x = 1; x <= 8; x++) {
				pos = new Position(x, rows[0]);

				switch (x) {
				case 1:
				case 8:
					new Turm(this, pos, farbe);
					break;
				case 2:
				case 7:
					new Springer(this, pos, farbe);
					break;
				case 3:
				case 6:
					new Laeufer(this, pos, farbe);
					break;
				case 4:
					new Dame(this, pos, farbe);
					break;
				case 5:
					new Koenig(this, pos, farbe);
					break;
				}
				brettPanel.refresh(pos);
			}

			// Nun die Bauern
			for (int j = 1; j <= 8; j++) {
				pos = new Position(j, rows[1]);
				new Bauer(this, pos, farbe);
				brettPanel.refresh(pos);
			}

			/*
			 * Jetzt werden fuer den naechsten Schleifendurchlauf die
			 * Einstellungen fuer Weiss gesetzt
			 */
			farbe = Farbe.WEISS;
			rows = new int[] { 8, 7 };
		}
		// Neu zeichnen
		brettPanel.repaint();

		// Ersten Spieler zum Zug auffordern
		spieler[0].notifyTurn();
	}

	/**
	 * Setzt das Schachfeld auf den Ausgangszustand zurueck, bzw. initialisiert
	 * es. Hier handelt es sich um ein Zeitspiel, weshalb zwei SchachUhrThreads
	 * erwartet werden
	 * 
	 * @param spieler1
	 *            Weisser Spieler
	 * @param spieler2
	 *            Schwarzer spieler
	 * @param timer1
	 *            Timer von spieler1
	 * @param timer2
	 *            Timer von spieler2
	 */
	public void reset(Spieler spieler1, Spieler spieler2,
			SchachUhrThread timer1, SchachUhrThread timer2) {
		reset(spieler1, spieler2);

		timer[0] = timer1;
		timer[1] = timer2;
		timer[0].start();
		timer[1].start();
	}

	/**
	 * Prueft, ob ein Feld leer ist. Die Position muss zuvor vom Aufrufer auf
	 * Gueltigkeit geprueft werden, sonst gibt es eine
	 * ArrayIndexOutOfBoundsException.
	 * 
	 * @param position
	 *            Zu pruefende Position
	 * @return ob diese Postion leer ist
	 */
	public boolean istFrei(Position position) {
		return figuren[position.getX()][position.getY()] == null;
	}

	/**
	 * Prueft, ob der Spieler der uebergebenen Farbe im Schach steht. Ist das
	 * so, wird wenn gewuenscht ein Sound abgespielt.
	 * 
	 * @param farbe
	 *            Die Farbe, die auf Schach geprueft werden soll.
	 * @param sound
	 *            ob ein Sound abgespielt werden soll
	 * 
	 * @return ob Schach geboten ist.
	 */
	private boolean istSchach(Farbe farbe, boolean sound) {
		Position koenigsPos = spielerDerFarbe(farbe).getKoenig().getPosition();

		if (!(istBedroht(koenigsPos, farbe))) {
			return false;
		}

		// Es ist Schach geboten
		if (sound) {
			MainWindow.playSound("Schach");
		}

		return true;
	}

	/**
	 * Gibt die Figur an dieser Stelle zurueck.
	 * 
	 * @param x
	 *            X-Koordinate
	 * @param y
	 *            Y-Koordinate
	 * @return die Figur an dieser Stelle
	 */
	public Figur getFigur(int x, int y) {
		return figuren[x][y];
	}

	/**
	 * @return die zuletzt bewegte Figur.
	 */
	public Figur getZuletztBewegteFigur() {
		return zuletztBewegteFigur;
	}

	/**
	 * @return die Farbe die gerade am Zug ist.
	 */
	public Farbe aktuelleFarbe() {
		if (spielerEinsAmZug) {
			return Farbe.WEISS;
		} else {
			return Farbe.SCHWARZ;
		}
	}

	/**
	 * Gibt den Spieler dieser Farbe.
	 * 
	 * @param farbe
	 *            die Farbe des Spielers
	 * @return den dazugehoerigen Spieler
	 */
	public Spieler spielerDerFarbe(Farbe farbe) {
		switch (farbe) {
		case WEISS:
			return spieler[0];
		case SCHWARZ:
			return spieler[1];
		default:
			return null;
		}
	}

	/**
	 * Prueft, ob ein Feld vom Gegner bedroht ist.
	 * 
	 * @param pos
	 *            die zu pruefende Position
	 * @param farbe
	 *            die Farbe des Spielers, der pruefen moechte
	 * @return ob das Feld bedroht ist
	 */
	public boolean istBedroht(Position pos, Farbe farbe) {
		// Hole den Gegenspieler
		Spieler sp = null;
		switch (farbe) {
		case WEISS:
			sp = spieler[1];
			break;
		case SCHWARZ:
			sp = spieler[0];
			break;
		}

		ArrayList<Figur> figs = sp.getFiguren();

		for (Figur fig : figs) {
			if (fig.istBedrohtesFeld(pos)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Prueft, ob das Spiel wegen Matt, Patt oder 50-Zuege-Regel zu Ende ist und
	 * beendet es in diesem Fall.
	 * 
	 * @return ob das Spiel zu Ende ist.
	 */
	private boolean pruefeSpielZuEnde() {
		switch (brettSituation(spielerDerFarbe(aktuelleFarbe()))) {
		case NORMAL:
			return false;
		case R50Z:
			endGame(null, "50 trains règle");
			return true;
		case MATT:
			Farbe siegerFarbe = aktuelleFarbe().andereFarbe();
			// Schachnotation vervollstaendigen
			MainWindow.print("+\n");
			endGame(siegerFarbe, "échec et mat");
			return true;
		case PATT:
			MainWindow.print("\n");
			endGame(null, "impasse");
			return true;
		default:
			return false;
		}
	}

	/**
	 * Gibt die aktuelle Brettsituation des pruefenden Spielers
	 * 
	 * @param pruefer
	 *            Der pruefende Spieler
	 * @return die aktuelle Brettsituation
	 */
	public Situation brettSituation(Spieler pruefer) {
		return brettSituation(pruefer, pruefer.generiereMoeglicheZuege());
	}

	/**
	 * Gibt die aktuelle Brettsituation des pruefenden Spielers mit uebergebenen
	 * moeglichen Zuegen. Diese Methode berechnet die Zuege nicht neu, und ist
	 * daher schneller, wenn diese bereits berechnet wurden. (Wie in
	 * MyPCSpieler).
	 * 
	 * @param pruefer
	 *            Der pruefende Spieler
	 * @return die aktuelle Brettsituation
	 */
	public Situation brettSituation(Spieler pruefer, ArrayList<Zug> zuege) {
		if (zuegeOhneSchlagOderBauer >= 100) {
			// 50-Zuege-Regel tritt in Kraft
			return Situation.R50Z;
		}

		if (!zuege.isEmpty()) {
			// Es gibt mindestens einen erlaubten Zug
			return Situation.NORMAL;
		}

		// Es gibt keine erlaubten Zuege!
		if (istBedroht(pruefer.getKoenig().getPosition(), pruefer.getKoenig()
				.getFarbe())) {
			// Der Koenig steht im Schach, also Matt
			return Situation.MATT;
		} else {
			// Der Koenig steht nicht im Schach, also Patt
			return Situation.PATT;
		}
	}

	/**
	 * Beendet dasSpiel
	 * 
	 * @param siegerFarbe
	 *            Die Fare des Siegers, null wenn Remis
	 * @param grund
	 *            warum das Spiel zu Ende ist. wenn null, wird keine MsgBox
	 *            angezeigt
	 */
	public synchronized void endGame(Farbe siegerFarbe, String grund) {
		// MouseListener entfernen und resetten
		brettPanel.removeMouseListener();

		// ggf. Schachuhren anhalten
		for (SchachUhrThread t : timer) {
			if (t != null) {
				// Timer endgueltig stoppen
				t.interrupt();
			}
		}

		// ggf. noch rechnende PCSpieler unterbrechen
		for (Spieler sp : spieler) {
			if (sp instanceof PCSpieler) {
				((PCSpieler) sp).interrupt();
			}
		}

		// Wenn kein Grund angegeben hier aufhoeren
		if (grund == null) {
			return;
		}

		if (siegerFarbe == null) {
			grund = "Nul " + grund + "!";
		} else {
			grund = siegerFarbe.toString() + " Gagne " + grund + "!";
			// Sieg- oder Niederlagensound abspielen
			if (spielerDerFarbe(siegerFarbe) instanceof PCSpieler
					&& !(spielerDerFarbe(siegerFarbe.andereFarbe()) instanceof PCSpieler)) {
				MainWindow.playSound("Niederlage");
			} else {
				MainWindow.playSound("Sieg");
			}
		}

		/*
		 * FIXME
		 * Wichtig, da sonst die MsgBox teilweise nicht angezeigt wird. Weiss
		 * der Henker warum.
		 */
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
		}

		JOptionPane.showMessageDialog(null, grund, "Jeu terminé!",
				JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Gibt fuer die hiesigen Koordinaten die entsprechenden in der
	 * Schachnotation als String.
	 * 
	 * @param pos
	 *            Die Position
	 * @return Schachnotations-Koordinaten
	 */
	private static String koordinatenSchachnotation(Position pos) {
		char x = (char) (pos.getX() + 'a' - 1);
		return (x + "") + (8 - pos.getY() + 1);
	}

	/**
	 * Holt einen Spieler.
	 * 
	 * @param pruefer
	 *            Der pruefende Spieler
	 * @param sich
	 *            Ob er sich (true) oder seinen Gegner (false) haben will
	 * @return den Spieler
	 */
	public Spieler getSpieler(Spieler pruefer, boolean sich) {
		if ((pruefer == spieler[0] && sich) || (pruefer == spieler[1] && !sich)) {
			// Spieler 1
			return spieler[0];
		} else {
			// Spieler 2
			return spieler[1];
		}
	}
	
	/**
	 * Holt einen Spieler.
	 * 
	 * @param farbe
	 * 			  Die Farbe des Spielers
	 * @return den Spieler
	 */
	public Spieler getSpieler(Farbe farbe) {
		switch(farbe) {
		case WEISS:
			return spieler[0];
		case SCHWARZ:
			return spieler[1];
		default:
			return null;
		}
	}

	/**
	 * Setzt eine Figur auf ein Feld und aktualisiert alles noetige.
	 * 
	 * @param figur
	 *            die zu bewegende Figur
	 * @param newPos
	 *            die neue Position
	 */
	public void figurSetPosition(Figur figur, Position newPos) {
		Position oldPos = figur.getPosition();

		if (oldPos == null) {
			if (newPos == null) {
				return;
			}
			spielerDerFarbe(figur.getFarbe()).addFigur(figur);
		} else {
			figuren[oldPos.getX()][oldPos.getY()] = null;
		}

		if (newPos == null) {
			spielerDerFarbe(figur.getFarbe()).removeFigur(figur);
		} else {
			figuren[newPos.getX()][newPos.getY()] = figur;
		}

		figur.setPosition(newPos);
	}
	
	/**
	 * Klont ein Brett. Der Klon ist mit keinem BrettPanel verbunden und darf daher
	 * auch nur fÃ¼r Berechnungen (von PC-Gegnern) benutzt werden. Es werden 2 neue
	 * Spieler erstellt und alle derzeit auf dem Brett befindlichen Figuren geklont.
	 */
	@Override
	public Brett clone() {
		Brett clone = null;
		try {
			clone = (Brett) super.clone();
		} catch(CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		
		clone.brettPanel = null;
		clone.spieler = new Spieler[2];
		clone.timer = new SchachUhrThread[2];
		clone.figuren = new Figur[9][9];
		
		clone.spieler[0] = new Spieler(clone);
		clone.spieler[1] = new Spieler(clone);
		
		for(Figur[] fLine : figuren) {
			for(Figur figur : fLine) {
				if(figur != null) {
					Figur fClone = figur.clone(clone);
					if(figur == zuletztBewegteFigur) {
						clone.zuletztBewegteFigur = fClone;
					}
				}
			}
		}
		
		return clone;
	}
}