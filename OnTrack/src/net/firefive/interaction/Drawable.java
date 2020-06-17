package net.firefive.interaction;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public abstract class Drawable implements Runnable // for flashing features
    , ActionListener // for selection of popup menu items
    , Iterable<Drawable> // for immediate children
    , PopupMenuListener {
  private Surface surface;
  private CopyOnWriteArrayList<Drawable> children;

  private Region region; // region relative to parent component
  private Mobility mobility;
  private boolean visible;

  private boolean pressed;
  private ArrayList<String> popupOptions;
  private boolean popupVisible;

  public Drawable(Surface surface, Region r, Mobility mobility) {
    this.surface = surface;
    children = new CopyOnWriteArrayList<Drawable>();

    Region parent = surface.getInitialRegion();
    this.region = new Region(r.x / parent.width, r.y / parent.height, r.width / parent.width, r.height / parent.height);
    this.mobility = mobility;
    setVisible(true);

    pressed = false;
    popupOptions = new ArrayList<String>();
    popupVisible = false;

    alternateDrawingMode = false;
    if (isTimeDriven()) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void print() {
    print(0);
  }

  private void print(int indent) {
    String s = "";
    for (int i = 0; i < indent; i++)
      s += "  ";
    s += "|- " + this;
    System.out.println(s);
    for (Drawable child : children)
      child.print(indent + 1);
  }

  /*
   * collection management
   */
  public void addChild(Drawable child) {
    surface.removeDrawable(child); // make sure the child has only one parent
    children.add(child);
  }

  // returns removed Drawable's *parent*
  Drawable removeDescendant(Drawable d) {
    if (children.remove(d))
      return this;
    Drawable parent = null;
    for (Drawable child : children) {
      parent = child.removeDescendant(d);
      if (parent != null)
        return parent;
    }
    return parent;
  }

  boolean hasDrawable(Drawable d) {
    if (this == d)
      return true;
    for (Drawable child : children)
      if (child.hasDrawable(d))
        return true;
    return false;
  }

  public Iterator<Drawable> iterator() {
    return children.iterator();
  }

  ArrayList<Drawable> getMembers() {
    ArrayList<Drawable> members = new ArrayList<Drawable>();
    getMembers(members);
    return members;
  }

  private void getMembers(ArrayList<Drawable> members) {
    for (Drawable child : children)
      child.getMembers(members);
    members.add(this);
  }

  /*
   * event management
   */
  void notifyEvent(Event evt) {
    this.handleEvent(evt);
    for (Drawable child : children)
      child.notifyEvent(evt);
  }

  // handle event published by the surface
  public void handleEvent(Event evt) {
    // implementers wishing to handle events should override this method
  }

  /*
   * geometric properties
   */

  public void setPosition(Point p) {
    Region r = surface.getRegion();
    region.x = p.x / r.width;
    region.y = p.y / r.height;
  }

  public Region getRegion() {
    Region r = surface.getRegion();
    return new Region(region.x * r.width, region.y * r.height, region.width * r.width, region.height * r.height);
  }

  // returns the topmost Drawable containing a Point
  Drawable getTopmostContainer(Point p) {
    // if the point is contained in any descendants, return that one

    // we paint the list in forward order, so the topmost element is last
    for (ListIterator<Drawable> li = children.listIterator(children.size()); li.hasPrevious();) {
      Drawable container = li.previous().getTopmostContainer(p);
      if (container != null)
        return container;
    }

    // otherwise if this contains the point, return this
    if (this.contains(p) && isVisible())
      return this;
    return null;
  }

  public boolean contains(Point p) {
    // implementers requiring a more precise definition should override this
    // method
    return getRegion().contains(p);
  }

  // boolean abstract intersects(Region r);

  /*
   * drawing methods
   */

  void drawAll(Graphics g) {
    // System.out.println(this + " " + System.currentTimeMillis() + " " +
    // container);
    if (isVisible())
      draw(g);
    for (Drawable child : children)
      child.drawAll(g);
  }

  public abstract void draw(Graphics g);

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  Mobility getMobility() {
    return mobility;
  }

  /*
   * Implementation of TimeDrivenElement. The interface has no methods or fields
   * and serves only to identify the semantics of being time-driven. Time-driven
   * elements are threaded to allow repaint() to be called every 500ms, and
   * toggle an associated "drawing mode" property on each cycle. The true
   * purpose of the interface is to eliminate the overhead of threading from
   * Drawables that are *not* time driven (see constructor).
   */

  private Thread thread;
  private boolean alternateDrawingMode;

  private boolean isTimeDriven() {
    return this instanceof TimeDriven;
  }

  public void run() {
    while (true) {
      alternateDrawingMode = !alternateDrawingMode;
      surface.repaint();
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
    }
  }

  public boolean inAlternateDrawingMode() {
    return alternateDrawingMode;
  }

  void notifyMousePress() {
    // System.out.println(this + " pressed");
    handleMousePress();
    pressed = true;
  }

  void notifyMouseRelease(boolean mouseOver) {
    // System.out.println(this + " released " + mouseOver);
    handleMouseRelease();
    if (mouseOver && pressed) {
      handleMouseClick();
      // System.out.println(this + " clicked");
    }
    pressed = false;
    for (Drawable child : children)
      child.notifyMouseRelease(false);
  }

  void notifyMouseDrag() {
    pressed = false;
  }

  public void handleMousePress() {
  }

  public void handleMouseRelease() {
  }

  public void handleMouseClick() {
    // System.out.println(this + " clicked");
  }

  /*
   * methods for popup menus
   */
  public void showPopupMenu(int x, int y) {
    if (popupOptions.size() > 0) { // has popup options
      JPopupMenu popup = new JPopupMenu();
      for (String s : popupOptions) {
        JMenuItem menuItem = new JMenuItem(s);
        menuItem.addActionListener(this);
        popup.add(menuItem);
      }
      popupVisible = true;
      popup.addPopupMenuListener(this);
      popup.show(surface, x, y);
    }
  }

  // handle selection of popup menu item
  @Override
  public final void actionPerformed(ActionEvent evt) {
    popupOptionSelected(evt.getActionCommand());
  }

  // implementers should override this method to receive popup events
  public void popupOptionSelected(String selected) {
  }

  boolean popupVisible() {
    return popupVisible;
  }

  public void setPopupOptions(ArrayList<String> popupOptions) {
    this.popupOptions = popupOptions;
  }

  @Override
  public void popupMenuCanceled(PopupMenuEvent e) {
    popupVisible = false;
  }

  @Override
  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
  }

  @Override
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
  }

  void drawTooltip(Graphics g, Point p) {
    String tooltip = getTooltip();
    if (tooltip.length() == 0)
      return;
    g.setFont(surface.getFont(10));
    FontMetrics fm = g.getFontMetrics();

    int margin = fm.getAscent() / 4;
    int width = fm.stringWidth(tooltip) + 4 * margin;
    int height = fm.getAscent() + fm.getDescent() + 2 * margin;
    int x = (int) p.x - width;
    int y = (int) p.y - height;

    if (x < 0)
      x += width;
    if (y < 0)
      y += height;

    g.setColor(new Color(255, 255, 102));
    g.fillRect(x, y, width, height);

    g.setColor(Color.DARK_GRAY);
    g.drawRect(x, y, width, height);
    g.drawString(tooltip, x + 2 * margin, y + margin + fm.getAscent());
  }

  public String getTooltip() {
    return "";
  }
}