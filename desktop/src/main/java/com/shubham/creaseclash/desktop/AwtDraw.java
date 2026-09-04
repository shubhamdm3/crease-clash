package com.shubham.creaseclash.desktop;

import com.shubham.creaseclash.Draw;
import java.awt.*;
import java.awt.geom.*;
import java.io.InputStream;

public final class AwtDraw implements Draw {
    private final Graphics2D g;
    private static final Font FONT=loadFont();
    private static Font loadFont() {
        try(InputStream in=AwtDraw.class.getResourceAsStream("/fonts/ui.ttf")) {
            if(in!=null) return Font.createFont(Font.TRUETYPE_FONT,in);
        } catch(Exception ignored) { }
        return new Font(Font.SANS_SERIF,Font.PLAIN,64);
    }
    public AwtDraw(Graphics2D g) {
        this.g=g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    private void color(int c) { g.setColor(new Color(c,true)); }
    public void rect(double x,double y,double w,double h,int c) { color(c); g.fill(new Rectangle2D.Double(x,y,w,h)); }
    public void roundRect(double x,double y,double w,double h,double r,int c) { color(c); g.fill(new RoundRectangle2D.Double(x,y,w,h,r*2,r*2)); }
    public void oval(double x,double y,double rx,double ry,int c) { color(c); g.fill(new Ellipse2D.Double(x-rx,y-ry,rx*2,ry*2)); }
    public void line(double x,double y,double x2,double y2,double width,int c) {
        color(c); g.setStroke(new BasicStroke((float)width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); g.draw(new Line2D.Double(x,y,x2,y2));
    }
    public void polygon(double[] xy,int c) {
        color(c); Path2D p=new Path2D.Double(); p.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2) p.lineTo(xy[i],xy[i+1]); p.closePath(); g.fill(p);
    }
    public void text(String s,double x,double y,double size,int c,boolean center) {
        color(c); g.setFont(FONT.deriveFont((float)size));
        if(center) x-=g.getFontMetrics().stringWidth(s)/2.0;
        g.drawString(s,(float)x,(float)y);
    }
}
