package org.fog.test.perfeval.testes.cluster;

public class Point {
  protected double x, y;

  public Point(double x, double y) { this.x = x; this.y = y; }

  public double distancia(Point p) {
      return Math.sqrt(Math.pow(p.x - this.x, 2) + Math.pow(p.y - this.y, 2));
  }

  public String toString() { return String.format("[%.1f, %.1f]", x, y); }
  }
