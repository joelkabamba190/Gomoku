package gomoku.gui;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import gomoku.regles.*;
import gomoku.jeu.*;

public class GomokuGUI {

  public GomokuGUI(JoueurAbstrait j1, JoueurAbstrait j2, Plateau plateau) {

    Fenetre f = new Fenetre(plateau);

    JComponent component = f.getDessinPlateau();

    Partie partie = new Partie(j1, j2, plateau, new VisuelGUI(component));

    MouseClic me = new MouseClic(partie);
    component.addMouseListener(me);
  }
}