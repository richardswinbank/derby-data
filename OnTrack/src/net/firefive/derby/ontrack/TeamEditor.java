package net.firefive.derby.ontrack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import net.firefive.derby.boutmodel.BoutConfigurationException;
import net.firefive.derby.boutmodel.BoutObserver;
import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.Skater;
import net.firefive.derby.boutmodel.Team;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.SkaterAddedEvent;
import net.firefive.derby.boutmodel.events.SkaterRemovedEvent;
import net.firefive.derby.boutmodel.events.TeamUpdatedEvent;

@SuppressWarnings("serial")
public class TeamEditor extends JPanel implements BoutObserver {

  private UpdateTeamPanel teamPanel;

  private BoutStateModel model;
  private TeamId teamId;
  private Roster roster;

  private TeamEditor self; // required reference for dialog parent

  public TeamEditor(TeamId teamId, BoutStateModel model) {
    self = this;
    this.teamId = teamId;
    this.model = model;

    teamPanel = new UpdateTeamPanel();
    teamPanel.update();
    roster = new Roster();

    setLayout(new BorderLayout());
    add(teamPanel, BorderLayout.NORTH);
    add(new JScrollPane(new RosterTable()), BorderLayout.CENTER);
    add(new AddSkaterPanel(), BorderLayout.SOUTH);

    model.addObserver(this);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(500, 400);
  }

  @Override
  public void handleBoutEvent(AbstractBoutEvent evt) {
    if (evt instanceof TeamUpdatedEvent)
      if (((TeamUpdatedEvent) evt).getTeamId() == teamId)
        teamPanel.update();
    if (evt instanceof SkaterAddedEvent) {
      Skater s = ((SkaterAddedEvent) evt).getSkater();
      if (s.getTeamId() == teamId)
        roster.add(s);
    }
    if (evt instanceof SkaterRemovedEvent) {
      Skater s = ((SkaterRemovedEvent) evt).getSkater();
      if (s.getTeamId() == teamId)
        roster.remove(s);
    }
  }

  private class UpdateTeamPanel extends JPanel {

    private JTextField teamName;
    private JButton bg;
    private JButton fg;

    public UpdateTeamPanel() {
      add(new JLabel(teamId == TeamId.HOME ? "Home team" : "Visiting team"));
      teamName = new JTextField(20);
      teamName.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          Team team = model.getTeam(teamId);
          if (!team.getName().equals(teamName.getText()))
            model.updateTeam(teamId, teamName.getText(), team.getPrimaryColour(), team.getSecondaryColour());
        }
      });
      add(teamName);

      bg = new JButton();
      bg.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          Team team = model.getTeam(teamId);
          Color newColor = JColorChooser.showDialog(self, team.getName() + " - Main colour", team.getPrimaryColour());
          // System.out.println(TextBox.contrastRatio(newColor, Color.WHITE));
          model.updateTeam(teamId, teamName.getText(), newColor, team.getSecondaryColour());
        }
      });
      add(bg);

      fg = new JButton();
      fg.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          Team team = model.getTeam(teamId);
          Color newColor = JColorChooser.showDialog(self, team.getName() + " - Secondary colour",
              team.getPrimaryColour());
          model.updateTeam(teamId, teamName.getText(), team.getPrimaryColour(), newColor);
        }
      });
      add(fg);
    }

    public void update() {
      Team team = model.getTeam(teamId);
      teamName.setText(team.getName());
      bg.setBackground(team.getPrimaryColour());
      fg.setBackground(team.getSecondaryColour());
    }
  }

  private class AddSkaterPanel extends JPanel {

    private JTextField name;
    private JTextField number;

    public AddSkaterPanel() {
      add(new JLabel("Number"));
      number = new MaxLengthField(4); // rule 3.7.4.3
      add(number);
      add(new JLabel("Name"));
      name = new MaxLengthField(20);
      add(name);

      JButton button = new JButton("Add skater");
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (name.getText().length() > 0 && number.getText().length() > 0)
            try {
              model.addSkater(number.getText(), name.getText(), teamId);
              number.setText("");
              name.setText("");
            } catch (BoutConfigurationException ex) {
              JOptionPane.showMessageDialog(self, ex.getMessage());
            }
        }
      });
      add(button);
      setFocusTraversalPolicy(new AddSkaterPanelTabOrder(number, name, button));
    }
  }

  private class MaxLengthField extends JTextField {

    private int maxLength;

    public MaxLengthField(int length) {
      super(length);
      this.maxLength = length;
      setDocument(new PlainDocument() {
        @Override
        public void insertString(int off, String s, AttributeSet attrSet) throws BadLocationException {
          if (s != null && getLength() + s.length() <= maxLength)
            super.insertString(off, s, attrSet);
        }
      });
    }

  }

  private class AddSkaterPanelTabOrder extends FocusTraversalPolicy {
    ArrayList<Component> order;

    public AddSkaterPanelTabOrder(JTextField number, JTextField name, JButton button) {
      this.order = new ArrayList<Component>(3);
      order.add(number);
      order.add(name);
      order.add(button);
    }

    @Override
    public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
      int idx = order.indexOf(aComponent) - 1;
      if (idx < 0) {
        idx = order.size() - 1;
      }
      return order.get(idx);
    }

    @Override
    public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
      int idx = (order.indexOf(aComponent) + 1) % order.size();
      return order.get(idx);
    }

    @Override
    public Component getFirstComponent(Container focusCycleRoot) {
      return order.get(0);
    }

    @Override
    public Component getLastComponent(Container focusCycleRoot) {
      return order.get(order.size() - 1);
    }

    @Override
    public Component getDefaultComponent(Container focusCycleRoot) {
      return order.get(0);
    }
  }

  private class RosterTable extends JTable {

    private JPopupMenu popup;
    private int selectedRow;

    public RosterTable() {
      super(roster);

      popup = new JPopupMenu();
      JMenuItem remove = new JMenuItem("Remove");
      remove.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          Skater s = roster.get(selectedRow);
          model.removeSkater(s.getSkaterId());
        }
      });
      popup.add(remove);

      getColumnModel().getColumn(0).setWidth(100);
      getColumnModel().getColumn(1).setWidth(50);

      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          selectedRow = rowAtPoint(e.getPoint());
          ListSelectionModel model = getSelectionModel();
          model.setSelectionInterval(selectedRow, selectedRow);
          if (SwingUtilities.isRightMouseButton(e))
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
      });
    }
  }

  private class Roster extends ArrayList<Skater>implements TableModel {

    private Comparator<Skater> comparator;

    public Roster() {
      comparator = new Comparator<Skater>() {

        @Override
        public int compare(Skater s1, Skater s2) {
          return s1.getNumber().compareTo(s2.getNumber());
        }
      };
    }

    private void sort() {
      this.sort(comparator);
    }

    @Override
    public boolean add(Skater s) {
      boolean b = super.add(s);
      sort();
      if (listener != null)
        listener.tableChanged(null);
      return b;
    }

    @Override
    public boolean remove(Object o) {
      Skater s = (Skater) o;
      for (Iterator<Skater> it = iterator(); it.hasNext();)
        if (it.next().getSkaterId() == s.getSkaterId())
          it.remove();
      sort();
      if (listener != null)
        listener.tableChanged(null);
      return true;
    }

    private TableModelListener listener;

    /*
     * implementation of TableModel
     */
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public int getRowCount() {
      return size();
    }

    @Override
    public Object getValueAt(int row, int col) {
      Skater s = get(row);
      if (col == 0)
        return s.getNumber();
      else if (col == 1)
        return s.getName();
      else
        return "BUTTON";
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
      listener = l;
    }

    @Override
    public Class<?> getColumnClass(int col) {
      return String.class;
    }

    @Override
    public String getColumnName(int col) {
      if (col == 0)
        return "Number";
      else if (col == 1)
        return "Name";
      else
        return "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
  }
}
