package com.shubham.creaseclash;

/** Top-left origin, virtual 1440x810 canvas shared by Android and desktop QA. */
public interface Draw {
    void rect(double x,double y,double w,double h,int color);
    void roundRect(double x,double y,double w,double h,double radius,int color);
    void oval(double cx,double cy,double rx,double ry,int color);
    void line(double x1,double y1,double x2,double y2,double width,int color);
    void polygon(double[] xy,int color);
    void text(String text,double x,double baseline,double size,int color,boolean centered);
}
