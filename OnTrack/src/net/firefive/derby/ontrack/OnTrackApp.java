package net.firefive.derby.ontrack;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import net.firefive.derby.boutmodel.BoutObserver;
import net.firefive.derby.boutmodel.BoutStateModel;
import net.firefive.derby.boutmodel.PeriodPlayState;
import net.firefive.derby.boutmodel.TeamId;
import net.firefive.derby.boutmodel.events.AbstractBoutEvent;
import net.firefive.derby.boutmodel.events.TeamUpdatedEvent;
import net.firefive.derby.boutmodel.impl.BoutStateModelFactory;
import net.firefive.interaction.Region;

@SuppressWarnings("serial")
public class OnTrackApp extends JFrame implements BoutObserver {

  public static void main(String[] args) throws Exception {
    BoutStateModel bm = BoutStateModelFactory
        .getModel(new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "OnTrack");
    OnTrackApp app = new OnTrackApp(bm);
    // bm.addObserver(new ConsoleOutput());
    bm.recoverState();
    //app.arena.print();
    app.setSize(900, 625);
    app.setVisible(true);
  }

  private Arena arena;

  private OnTrackApp app; // self-reference for inner classes
  private java.awt.Point location;
  private Dimension lastNonMaximisedSize;
  private BoutStateModel boutModel;
  private RosterEditor rosterEditor;

  public OnTrackApp(BoutStateModel bsm) {
    super();
    app = this;

    // window listener to tidy up on close
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // key listener to handle F11 to toggle full screen
    addKeyListener(new KeyListener() {
      public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_F11) // toggle full screen
          if (app.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            app.setSize(lastNonMaximisedSize);
            app.setLocation(location);
            app.setExtendedState(JFrame.NORMAL);
          }
          else {
            lastNonMaximisedSize = app.getSize();
            location = app.getLocation();
            app.setExtendedState(JFrame.MAXIMIZED_BOTH);
          }
      }

      public void keyTyped(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
      }
    });

    boutModel = bsm;
    boutModel.addObserver(this); // listen to reflect team changes in title bar

    rosterEditor = new RosterEditor(boutModel);
    rosterEditor.setVisible(false);
    setJMenuBar(initialiseMenus(boutModel));

    initialiseArena(boutModel);
    setLocation(new java.awt.Point(175, 15));
    setVisible(false);
  }

  @Override
  public void handleBoutEvent(AbstractBoutEvent evt) {
    if (evt instanceof TeamUpdatedEvent)
      setTitle(evt.getEventTime());
  }

  private void setTitle(long time) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    String title = "OnTrack - " + boutModel.getTeam(TeamId.HOME).getName() + " vs "
        + boutModel.getTeam(TeamId.VISITORS).getName() + " - " + dateFormatter.format(time);
    setTitle(title);
  }

  private JMenuBar initialiseMenus(BoutStateModel model) {
    // menu bar
    JMenuBar menuBar = new JMenuBar();
    JMenu boutOptions = new JMenu("Bout options");

    // start new period
    JMenuItem newPeriod = new JMenuItem("Start new period");
    newPeriod.addActionListener(new OnTrackMenuListener(model) {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int dialogResult = JOptionPane.showConfirmDialog(app,
            "Clicking 'Yes' will start a new period **now**! Continue?", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult != JOptionPane.YES_OPTION)
          return;

        BoutStateModel m = getModel();
        // cycle through states in order
        if (m.getPeriodState().getPlayState() == PeriodPlayState.RUNNING)
          m.officialTimeout();
        ;
        if (m.getPeriodState().getPlayState() == PeriodPlayState.PAUSED)
          m.endPeriod();
        if (m.getPeriodState().getPlayState() == PeriodPlayState.AFTER)
          m.newPeriod();
        m.startPlay();
      }
    });
    boutOptions.add(newPeriod);

    // edit roster
    JMenuItem roster = new JMenuItem("Edit roster");
    roster.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        rosterEditor.setLocation(app.getLocation().x + 70, app.getLocation().y + 50);
        rosterEditor.setVisible(true);
      }
    });
    boutOptions.add(roster);

    // create new bout
    JMenuItem newBout = new JMenuItem("Create new bout");
    newBout.addActionListener(new OnTrackMenuListener(model) {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int dialogResult = JOptionPane.showConfirmDialog(app,
            "Clicking 'Yes' will remove **all** existing bout setup! Continue?", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
          getModel().newBout();
          rosterEditor.setVisible(true);
        }
      }
    });
    boutOptions.add(newBout);

    menuBar.add(boutOptions);
    return menuBar;
  }

  private void initialiseArena(BoutStateModel model) {
    arena = new Arena(model, this, new Dimension(900, 625), new Dimension(40, 40),
        new Font("SansSerif", Font.BOLD, 100));

    // skater containers
    arena.addDrawable(new Bench(arena, new Region(20, 10, 260, 160), TeamBench.LEFT));
    arena.addDrawable(new PenaltyBox(arena, new Region(295, 10, 310, 160)));
    arena.addDrawable(new Bench(arena, new Region(620, 10, 260, 160), TeamBench.RIGHT));

    // track
    arena.addDrawable(new Blockers(arena, new Region(75, 210, 400, 200)));
    arena.addDrawable(new PivotGroup(TeamBench.LEFT, arena, new Region(475, 210, 140, 100)));
    arena.addDrawable(new PivotGroup(TeamBench.RIGHT, arena, new Region(475, 310, 140, 100)));
    arena.addDrawable(new JammerGroup(TeamBench.LEFT, arena, new Region(625, 210, 200, 100)));
    arena.addDrawable(new JammerGroup(TeamBench.RIGHT, arena, new Region(625, 310, 200, 100)));
    arena.addDrawable(new Tape(arena, new Region(74, 200, 752, 10)));
    arena.addDrawable(new Tape(arena, new Region(74, 410, 752, 10)));
    arena.addDrawable(new Tape(arena, new Region(615, 200, 10, 220)));

    arena.addDrawable(new ScorePanel(TeamBench.LEFT, arena, new Region(120, 445, 315, 40)));
    arena.addDrawable(new ScorePanel(TeamBench.RIGHT, arena, new Region(465, 445, 315, 40)));

    // review/timeout controls
    arena.addDrawable(new TimeoutPanel(TeamBench.LEFT, arena, new Region(142, 495, 216, 30)));
    arena.addDrawable(new TimeoutPanel(TeamBench.RIGHT, arena, new Region(542, 495, 216, 30)));

    arena.addDrawable(new LapCounter(arena, new Region(800, 445, 75, 80)));

    // clocks and score board
    arena.addDrawable(new PeriodCounter(arena, new Region(20, 560, 100, 40)));
    arena.addDrawable(new PeriodClock(arena, new Region(140, 545, 150, 70)));
    arena.addDrawable(new ScoreBoard(arena, new Region(320, 545, 260, 70)));
    arena.addDrawable(new JamClock(arena, new Region(610, 545, 150, 70)));
    arena.addDrawable(new JamCounter(arena, new Region(780, 560, 100, 40)));
  }

  public abstract class OnTrackMenuListener implements ActionListener {

    private BoutStateModel model;

    public OnTrackMenuListener(BoutStateModel model) {
      this.model = model;
    }

    public BoutStateModel getModel() {
      return model;
    }
  }
}
