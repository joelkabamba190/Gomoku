/**
 * @author julien Stechele et Thomas Ruchon
 * @version 1.0
 */

package gomoku.jeu;

import java.util.Set;
import gomoku.regles.Variante;
import gomoku.regles.RegleCoup;
import gomoku.regles.RegleAlignement;
import gomoku.jeu.Plateau;
import gomoku.jeu.Grille;
import gomoku.jeu.PierreCoordonnees;

/** Classe représentant une partie */
public class Partie {

  /** Joueur NOIR */
  private JoueurAbstrait jNoir;

  /** Joueur BLANC */
  private JoueurAbstrait jBlanc;

  /** Le plateau de la partie */
  private Plateau plateau;

  /** Booléen représentant si on est au premier coup.  */
  private boolean premierCoup = true;

  /** Le joueur ayant la main.  */
  private int doisJouer = Joueur.NOIR;

  /** Le joueur ayant gagné.  */
  private int gagnant;

  /** Le mode d'affichage de la partie */
  private String CLIouGUI;

  /** le style d'affichage de la partie */
  private Visuel visualiser;

  /** Instancie une partie avec deux joueurs, le plateau, et le visuel. */
  public Partie(JoueurAbstrait jNoir, JoueurAbstrait jBlanc, Plateau plateau,
      Visuel visualiser) {
    this.jNoir = jNoir;
    this.jBlanc = jBlanc;
    this.plateau = plateau;
    this.visualiser = visualiser;
  }

  /** Retourne la couleur du joueur si il y a un gagnant.
   * @return int */
  private int getGagnant() {
    return this.gagnant;
  }

  /** Le jeux continue si il y a des cases encore vide. */
  private void continueSiCaseJouables() {
    int cpt = 0;
    for (int x = 0; x < this.plateau.largeur() ; x++)
      for (int y = 0; y < this.plateau.hauteur() ; y++)
        if (this.plateau.contenu(new PierreCoordonnees(x, y)) == Joueur.VIDE)
          cpt++;
    if (cpt == 0)
      this.visualiser.laPartieEstNulle();
  }

  /** Méthode principale du jeu.
   * @param c La coordonnée au clic si c'est en GUI
   * sinon on passe null. */
  public void jouer(Coordonnees c) {
    this.continueSiCaseJouables();
    if (c == null)
      this.CLIouGUI = "CLI";
    else
      this.CLIouGUI = "GUI";
    String str;
    if (this.coupAjouer()) {
      if(this.demanderDeJouer(c))
        this.visualiser.afficherLaGrille();
      if (this.gagnant != 0) {
        if (this.getGagnant() == Joueur.NOIR)
          str = this.jNoir.couleurIntToString();
        else
          str = this.jBlanc.couleurIntToString();
        this.visualiser.leJoueurAGagne(str);
      } else {
        if (this.CLIouGUI.equals("CLI"))
          this.jouer(null);
      }
    } else {
      this.visualiser.laPartieEstNulle();
    }
  }

  /** Cette méthode vérifie s'il reste des coups à jouer
   * @return true ou false */
  private boolean coupAjouer() {
    if (!(this.jNoir.getNbCoups() == 0
          && this.jBlanc.getNbCoups() == 0))
      return true;
    return false;
  }

  /** Cette méthode demande les coordonnées
   *  au joueur qui a la main.
   * @return une coordonnées */
  private Coordonnees demanderCoor() {
    if (this.aLaMain(Joueur.NOIR))
      return this.jNoir.demanderCoorJoueur(this);
    else
      return this.jBlanc.demanderCoorJoueur(this);
  }

  /** Permet de savoir si le joueur a la main
   * @param couleur La couleur du joueur
   * @return true si la couleur correspond au
   * joueur qui à la main false sinon. */
  private boolean aLaMain(int couleur) {
    return couleur == this.doisJouer ? true : false;
  }

  /** Méthode principale de demande de placement
   * @param Coordonnees Si est null, c'est que le
   * jeu est en mode CLI. Sinon la Coordonnees vien
   * de la souris. C'est ici que je vérifie si la
   * Coordonnees est valide via RegleCoup et si
   * l'emplacement est vide. */
  private boolean demanderDeJouer(Coordonnees c) {
    Variante v = ((Grille)plateau).getVariante();
    RegleCoup r = v.verifCoup();
    if (this.premierCoup) {
      this.premierCoup = false;
      if (c == null)
        c = this.demanderCoor();
      this.joueurJoue(c);
      return true;
    } else {
      if (c == null)
        c = this.demanderCoor();
      if (r.estValide(c, plateau) &&
          this.plateau.contenu(c) == Joueur.VIDE)
      {
        this.joueurJoue(c);
        return true;
      }
    }
    return false;
  }

  /** Méthode de placement du joueur qui à la main.
   * Si L'IA n'as pas trouvé de Coordonnees ou placer
   * la prochaine pierre, la partie est terminée.
   * @param Coordonnees la Coordonnees du joueur. */
  private void joueurJoue(Coordonnees c) {
    if (this.aLaMain(Joueur.NOIR)) {
      this.plateau.placer(c, this.jNoir.couleur());
      this.jNoir.joueUnePierre();
    }
    else {
      if (c == null)
        this.visualiser.laPartieEstNulle();
      this.plateau.placer(c, this.jBlanc.couleur());
      this.jBlanc.joueUnePierre();
    }
    this.verifierCoupGagnant();
  }

  /** Permet de donner la main au joueur suivant */
  private void donnerLaMain() {
    this.doisJouer = this.aLaMain(Joueur.NOIR) ?
      Joueur.BLANC : Joueur.NOIR;
  }

  /** Vérifie si le coup du joueur courant est gagnant.
   * Si le joueur blanc est une IA, je le fais jouer
   * automatiquement. */
  private void verifierCoupGagnant() {
    Coordonnees c;
    Variante v = ((Grille)this.plateau).getVariante();
    RegleAlignement regle = v.verifAlignement();
    Set<Alignement> align = plateau.rechercherAlignements(
        this.doisJouer,
        regle.tailleMin());
    for (Alignement a: align) {
      if (regle.estGagnant(a, this.plateau))
        this.gagnant = this.doisJouer;
    }
    if (this.gagnant == 0) {
      this.donnerLaMain();
      if (this.aLaMain(Joueur.BLANC))
        if (this.blancEstUneIA()) {
          c = this.demanderCoor();
          this.joueurJoue(c);
        }
    }
  }

  /** Permet de savoir si le joueur est une Intelligence
   * Artificielle.
   * @return true si oui, false sinon */
  private boolean blancEstUneIA() {
    return this.jBlanc.getClass().getName() ==
      "gomoku.jeu.JoueurCybernetique" ? true : false;
  }

  /** Permet de récupérer le plateau
   * @return le plateau correspondant à la partie.  */
  public Plateau getPlateau() {
    return this.plateau;
  }
}
