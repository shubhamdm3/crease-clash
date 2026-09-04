package com.shubham.creaseclash;

/** Owns input and preferences; both platform launchers use this exact controller. */
public final class GameSession {
    public interface Platform {
        int readInt(String key,int fallback);
        void writeInt(String key,int value);
        void sound(String event);
        void vibrate(String event);
    }
    public final CricketGame game;
    public final Platform platform;
    public CricketGame.Difficulty selected=CricketGame.Difficulty.CLUB;
    public boolean sound=true,haptics=true,help=false;
    public int best;
    private boolean saved;
    public GameSession(long seed,Platform platform) {
        this.platform=platform;
        sound=platform.readInt("sound",1)==1; haptics=platform.readInt("haptics",1)==1;
        best=platform.readInt("best",0);
        selected=CricketGame.Difficulty.values()[Math.max(0,Math.min(2,platform.readInt("difficulty",0)))];
        game=new CricketGame(seed,this::feedback);
    }
    private void feedback(String event) {
        if(sound) platform.sound(event);
        if(haptics && (event.equals("hit") || event.equals("wicket") || event.equals("boundary"))) platform.vibrate(event);
    }
    public void update(double dt) {
        game.update(dt);
        if(!game.practice && game.phase==CricketGame.Phase.MATCH_OVER && !saved) {
            best=Math.max(best,game.runs); platform.writeInt("best",best); saved=true;
        }
    }
    public void start(boolean practice) { saved=false; help=false; game.start(practice,selected); }
    public void background() { game.setPaused(true); }
    public void back() {
        if(help) { help=false; return; }
        if(game.phase==CricketGame.Phase.MENU) return;
        if(game.phase==CricketGame.Phase.MATCH_OVER) game.menu();
        else game.setPaused(!game.paused);
    }
    public void tap(double x,double y) {
        if(help) { help=false; return; }
        if(hit(x,y,1140,28,78,54)) { sound=!sound; platform.writeInt("sound",sound?1:0); return; }
        if(hit(x,y,1230,28,78,54)) { haptics=!haptics; platform.writeInt("haptics",haptics?1:0); return; }
        if(game.phase==CricketGame.Phase.MENU) {
            if(hit(x,y,76,516,445,70)) start(false);
            else if(hit(x,y,76,600,214,58)) start(true);
            else if(hit(x,y,307,600,214,58)) help=true;
            else if(hit(x,y,76,431,445,52)) {
                selected=CricketGame.Difficulty.values()[(selected.ordinal()+1)%3];
                platform.writeInt("difficulty",selected.ordinal());
            }
            return;
        }
        if(game.paused) {
            if(hit(x,y,530,358,380,64)) game.setPaused(false);
            else if(hit(x,y,530,438,380,58)) start(game.practice);
            else if(hit(x,y,530,512,380,58)) game.menu();
            return;
        }
        if(game.phase==CricketGame.Phase.MATCH_OVER) {
            if(hit(x,y,530,510,380,64)) start(game.practice);
            else if(hit(x,y,530,590,380,58)) game.menu();
            return;
        }
        if(hit(x,y,1320,28,78,54)) { game.setPaused(true); return; }
        if(hit(x,y,344,682,276,92)) game.swing(-1);
        else if(hit(x,y,1116,682,276,92)) game.swing(1);
        else if(hit(x,y,658,682,242,92)) {
            if(game.phase==CricketGame.Phase.READY) game.bowl();
        }
        else if(hit(x,y,928,682,160,92)) game.toggleLoft();
    }
    public static boolean hit(double x,double y,double bx,double by,double w,double h) {
        return x>=bx && x<=bx+w && y>=by && y<=by+h;
    }
}
