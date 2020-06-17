package net.firefive.interaction.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.firefive.interaction.Mobility;
import net.firefive.interaction.Region;
import net.firefive.interaction.Surface;
import net.firefive.interaction.components.Label;
import net.firefive.interaction.components.Timer;

@SuppressWarnings("serial")
public class DemoApp extends JFrame {

  public static void main(String[] args) throws Exception {
    new DemoApp();
  }

  public DemoApp() {
    super();
    setTitle(getClass().getSimpleName());
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    setLocation(40, 40);

    Surface surface = new Surface(this, new Dimension(600, 400), new Font("SansSerif", Font.BOLD, 100));

    surface.addDrawable(new Blob(Color.RED, surface, new Region(0, 0, 40, 40), Mobility.FIXED));
    surface.addDrawable(new Blob(Color.BLUE, surface, new Region(100, 200, 40, 40), Mobility.FREE));
    surface.addDrawable(new Blob(Color.ORANGE, surface, new Region(200, 100, 40, 40), Mobility.BOUND));
    surface.addDrawable(
        new FlashyBlob(Color.YELLOW, Color.GREEN, surface, new Region(300, 150, 40, 40), Mobility.FREE));

    surface.addDrawable(new Label("DemoApp", surface, new Region(50, 300, 150, 40)));
    surface.addDrawable(new ShowButton(surface, new Region(400, 300, 150, 40)));
    surface.addDrawable(
        new Timer(120000, Timer.DECREASING, surface, new Region(450, 50, 100, 40), Color.WHITE, Mobility.FIXED));

    getContentPane().add(surface);
    setVisible(true);
  }
}
