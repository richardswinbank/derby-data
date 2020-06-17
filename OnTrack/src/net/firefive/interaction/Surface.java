package net.firefive.interaction;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Surface extends JPanel implements MouseListener, MouseMotionListener, Iterable<Drawable> {

  // drawable elements placed onto the surface
  private Drawable root;

  // variables relating to display
  private Container frameContentPane;
  private Font font;

  // variables related to dragging an element
  private Drawable held; // element being dragged
  private Point pickupElementLocation; // held's location when picked up
  private Drawable heldParent;
  private Point pickupOffset; // offset from location of pickup click event

  private Region initialRegion;
  private MouseEvent lastMouseMove;

  public Surface(JFrame frame, Dimension size, Font f) {
    // setup the content pane first - it's used for region calculation for
    // drawing
    this.frameContentPane = frame.getContentPane();
    frameContentPane.setSize(size);
    frameContentPane.add(this);
    frameContentPane.setPreferredSize(size);
    frame.pack();

    this.font = f;

    initialRegion = getRegion();
    root = new RootDrawable(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    setDoubleBuffered(true);
  }

  Region getInitialRegion() {
    return initialRegion;
  }

  @Override
  public Font getFont() {
    return font;
  }

  public Font getFont(int size) {
    return new Font(font.getName(), font.getStyle(), size);
  }

  public Region getRegion() {
    Dimension d = frameContentPane.getSize();
    // System.out.println(d);
    return new Region(0, 0, d.getWidth(), d.getHeight());
  }

  /*
   * management of collection of Drawables
   */
  public void addDrawable(Drawable d) {
    root.addChild(d);
  }

  public Drawable removeDrawable(Drawable d) {
    return root.removeDescendant(d);
  }

  public boolean isHeld(Drawable e) {
    return e == held;
  }

  @Override
  public Iterator<Drawable> iterator() {
    ArrayList<Drawable> members = root.getMembers();
    if (held != null)
      members.add(held);
    return members.iterator();
  }

  public void print() {
    root.print();
  }

  /*
   * event management
   */
  // publish a reported event to all elements
  public void publishEvent(Event evt) {
    root.notifyEvent(evt);
  }

  // handle interaction between two elements
  private void generateInteraction(Drawable actor, Drawable recipient) {
    if (actor == null || recipient == null || actor == recipient)
      return;
    handleInteraction(actor, recipient);
  }

  // handle interaction between two elements
  public void handleInteraction(Drawable actor, Drawable recipient) {
    // implementers wishing to handle Drawable interactions
    // should override this method
  }

  // handle event (typically just those that aren't
  // the responsibility of any specific Drawable).
  public void handleEvent(Event evt) {
    // implementers wishing the Surface to receive events
    // should override this method
  }

  /*
   * graphics handling
   */
  public final void paintComponent(Graphics g) {
    super.paintComponent(g);

    root.drawAll(g);
    if (held != null)
      held.drawAll(g); // redraw the held element last so it's on top
    else if (lastMouseMove != null) {
      Drawable underMouse = getUnderMouse(lastMouseMove);
      if (!underMouse.popupVisible())
        underMouse.drawTooltip(g, new Point(lastMouseMove.getX(), lastMouseMove.getY()));
    }
  }

  /*
   * implementation of mouse listeners
   */
  @Override
  public final void mousePressed(MouseEvent e) {
    Drawable underMouse = getUnderMouse(e);
    underMouse.notifyMousePress();

    // are we picking up an element?
    if (held == null && underMouse.getMobility() != Mobility.FIXED) {
      held = underMouse;
      heldParent = removeDrawable(held);
      pickupElementLocation = held.getRegion().getPosition();
      pickupOffset = new Point(pickupElementLocation.x - e.getX(), pickupElementLocation.y - e.getY());
    }

    repaint();
  }

  private Drawable getUnderMouse(MouseEvent evt) {
    return root.getTopmostContainer(new Point(evt.getX(), evt.getY()));
  }

  @Override
  public final void mouseReleased(MouseEvent e) {
    Drawable underMouse = getUnderMouse(e);

    // are we putting down an element?
    if (held != null) {

      // add the Drawable back to its *original* parent
      heldParent.addChild(held);
      if (held.getMobility() == Mobility.BOUND)
        held.setPosition(pickupElementLocation);

      // act on the element we've landed on - if the recipient 
      // needs to take ownership of the drawable, it can do so now
      // (using an appropriate implementation of handleEvent()).
      generateInteraction(held, underMouse);

      heldParent = null;
      held = null;
      underMouse = getUnderMouse(e);
      //System.out.println(underMouse);
    }

    if (e.isPopupTrigger())
      underMouse.showPopupMenu(e.getX(), e.getY());
    else
      underMouse.notifyMouseRelease(true);

    root.notifyMouseRelease(false);
    repaint();
    //print();
  }

  @Override
  public final void mouseClicked(MouseEvent evt) {
  }

  @Override
  public final void mouseDragged(MouseEvent evt) {
    if (held != null) {
      // Offset draw position by difference between held's original
      // location and the location of the pickup click (prevents
      // jerk on pickup as held moves to click location)
      held.setPosition(new Point(evt.getX() + pickupOffset.x, evt.getY() + pickupOffset.y));
      held.notifyMouseDrag();
      repaint();
    }
  }

  @Override
  public final void mouseMoved(MouseEvent e) {
    lastMouseMove = e;
  }

  @Override
  public final void mouseEntered(MouseEvent e) {
  }

  @Override
  public final void mouseExited(MouseEvent e) {
  }

  private class RootDrawable extends Drawable {

    private Surface surface;

    public RootDrawable(Surface surface) {
      super(surface, surface.getInitialRegion(), Mobility.FIXED);
      this.surface = surface;
    }

    @Override
    public void handleEvent(Event evt) {
      surface.handleEvent(evt);
    }

    @Override
    public void draw(Graphics g) {
      Region r = getRegion();
      g.setColor(Color.WHITE);
      g.fillRect((int) r.x, (int) r.y, (int) r.width, (int) r.height);
    }

    @Override
    public boolean contains(Point p) {
      return true;
    }

    @Override
    public Region getRegion() {
      return surface.getRegion();
    }
  }
}
