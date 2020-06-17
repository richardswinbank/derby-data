package net.firefive.derby.ontrack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.TeamId;

@SuppressWarnings("serial")
public class RosterEditor extends JFrame {

  public RosterEditor(BoutStateModel model) {
    super("Edit roster");
    setSize(600, 400);

    JPanel content = new JPanel();
    content.add(new TeamEditor(TeamId.HOME, model));
    content.add(new TeamEditor(TeamId.VISITORS, model));
    getContentPane().add(content);
    pack();
  }
}
