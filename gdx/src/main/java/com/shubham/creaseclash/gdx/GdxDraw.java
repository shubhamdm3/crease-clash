package com.shubham.creaseclash.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shubham.creaseclash.Draw;

/** OpenGL drawing adapter. The game scene itself lives in GameRenderer. */
public final class GdxDraw implements Draw {
    private final ShapeRenderer shapes=new ShapeRenderer();
    private final SpriteBatch batch=new SpriteBatch();
    private final BitmapFont font=new BitmapFont(Gdx.files.internal("fonts/ui.fnt"));
    private final GlyphLayout layout=new GlyphLayout();
    private boolean shapeMode,textMode;
    public GdxDraw() { font.getRegion().getTexture().setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,com.badlogic.gdx.graphics.Texture.TextureFilter.Linear); }
    public void begin(OrthographicCamera camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND); Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined); batch.setProjectionMatrix(camera.combined);
    }
    private void shape(int c) {
        if(textMode) {
            batch.end(); textMode=false;
            // SpriteBatch.end disables blending; restore it for translucent scene shapes.
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        if(!shapeMode) { shapes.begin(ShapeRenderer.ShapeType.Filled); shapeMode=true; }
        shapes.setColor(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f,((c>>>24)&255)/255f);
    }
    public void rect(double x,double y,double w,double h,int c) { shape(c); shapes.rect((float)x,(float)(810-y-h),(float)w,(float)h); }
    public void roundRect(double x,double y,double w,double h,double r,int c) {
        // Non-overlapping triangle fan prevents dark seams in translucent rounded panels.
        shape(c);
        double firstX=x+w-r,firstY=y,previousX=firstX,previousY=firstY;
        for(int corner=0;corner<4;corner++) {
            double centerX=corner<2?x+w-r:x+r;
            double centerY=corner==0 || corner==3?y+r:y+h-r;
            for(int step=0;step<=8;step++) {
                double angle=Math.toRadians(-90+corner*90+step*90.0/8);
                double nextX=centerX+Math.cos(angle)*r,nextY=centerY+Math.sin(angle)*r;
                shapes.triangle((float)(x+w/2),(float)(810-y-h/2),(float)previousX,(float)(810-previousY),(float)nextX,(float)(810-nextY));
                previousX=nextX; previousY=nextY;
            }
        }
        shapes.triangle((float)(x+w/2),(float)(810-y-h/2),(float)previousX,(float)(810-previousY),(float)firstX,(float)(810-firstY));
    }
    public void oval(double x,double y,double rx,double ry,int c) {
        shape(c); shapes.ellipse((float)(x-rx),(float)(810-y-ry),(float)(rx*2),(float)(ry*2),rx>100?100:24);
    }
    public void line(double x,double y,double x2,double y2,double width,int c) {
        shape(c); shapes.rectLine((float)x,(float)(810-y),(float)x2,(float)(810-y2),(float)width);
        oval(x,y,width/2,width/2,c); oval(x2,y2,width/2,width/2,c);
    }
    public void polygon(double[] points,int c) {
        shape(c);
        for(int i=2;i<points.length-2;i+=2) shapes.triangle((float)points[0],(float)(810-points[1]),(float)points[i],(float)(810-points[i+1]),(float)points[i+2],(float)(810-points[i+3]));
    }
    public void text(String s,double x,double y,double size,int c,boolean center) {
        if(shapeMode) { shapes.end(); shapeMode=false; }
        if(!textMode) { batch.begin(); textMode=true; }
        font.getData().setScale((float)(size/64));
        font.setColor(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f,((c>>>24)&255)/255f);
        layout.setText(font,s);
        font.draw(batch,layout,(float)(center?x-layout.width/2:x),(float)(810-y+font.getCapHeight()));
    }
    public void end() {
        if(shapeMode) shapes.end(); if(textMode) batch.end(); shapeMode=textMode=false;
    }
    public void dispose() { end(); shapes.dispose(); batch.dispose(); font.dispose(); }
}
