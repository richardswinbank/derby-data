package net.firefive.interaction;

public class Region {

  public double x;
  public double y;
  public double width;
  public double height;

  public Region(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public Region scale(Point scale) {
    return new Region(x * scale.x, y * scale.y, width * scale.x, height * scale.y);
  }

  public boolean contains(Point p) {
    if (p.x >= x && p.x <= x + width)
      if (p.y >= y && p.y <= y + height)
        return true;
    return false;
  }

  public String toString() {
    // return getClass().getSimpleName() + " " + x + " " + y + " " + width + " "
    // + height;
    return getClass().getSimpleName() + " " + (int) x + " " + (int) y + " " + (int) width + " " + (int) height;
  }

  public Point getCentre() {
    return new Point(x + width / 2, y + height / 2);
  }

  public Point getPosition() {
    return new Point(x, y);
  }

  public double radius() {
    if (width > height)
      return height / 2;
    return width / 2;
  }

  // public boolean intersects(Region r) {
  // if(this.contains(new Point(r.x, r.y)))
  // return true;
  // if(this.contains(new Point(r.x + r.width, r.y)))
  // return true;
  // if(this.contains(new Point(r.x, r.y + r.height)))
  // return true;
  // if(this.contains(new Point(r.x + r.width, r.y + r.height)))
  // return true;
  // return false;
  // }
}
