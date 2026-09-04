package com.shubham.creaseclash;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pure Java, deterministic cricket simulation. All positions are arcade field units. */
public final class CricketGame {
    public enum Phase { MENU, READY, RUNUP, DELIVERY, SHOT, RETURN, RESULT, MATCH_OVER }
    public enum Difficulty {
        CLUB("CLUB", 24, .175, .045, 1.58),
        PRO("PRO", 32, .135, .035, 1.33),
        ELITE("ELITE", 42, .105, .027, 1.12);
        public final String label;
        public final int target;
        public final double window, perfect, deliverySeconds;
        Difficulty(String label, int target, double window, double perfect, double deliverySeconds) {
            this.label=label; this.target=target; this.window=window;
            this.perfect=perfect; this.deliverySeconds=deliverySeconds;
        }
    }
    public interface Feedback { void emit(String event); }
    public static final double STEP=1.0/120.0, RADIUS=62, BATTER_Y=-16, GRAVITY=18;
    public static final double RUN_SECONDS=1.65, RUN_REACTION=.35;
    public static final int MAX_BALLS=12, MAX_WICKETS=3;
    public static final class Fielder {
        public final double homeX, homeY;
        public double x,y;
        public Fielder(double x,double y) { this.homeX=x; this.homeY=y; reset(); }
        public void reset() { x=homeX; y=homeY; }
    }
    private final Random random;
    private final Feedback feedback;
    public final List<Fielder> fielders=new ArrayList<>();
    public final List<String> history=new ArrayList<>();
    public Phase phase=Phase.MENU;
    public Difficulty difficulty=Difficulty.CLUB;
    public boolean practice, paused, lofted, swung, grounded, won, lastWicket;
    public int runs,wickets,balls,lastRuns,shotSide=1;
    public double clock, animation, ballX,ballY,ballZ,velocityX,velocityY,velocityZ;
    public double deliveryDuration, deliveryLine, bounceFraction, bounceHeight, swingAt=-99;
    public double timingError, quality, shotClock, returnDuration;
    public String timing="", result="", detail="", deliveryName="";
    public int catcher=-1;
    private double accumulator;
    private double ballPreviousZ;

    public CricketGame(long seed, Feedback feedback) {
        random=new Random(seed); this.feedback=feedback;
        // Gaps are intentional: shot direction and timing should beat the field.
        double[][] positions={{-37,-31},{-13,-48},{27,-39},{46,-6},{-46,9},{26,32},{-22,33},{-51,-21},{51,-24}};
        for(double[] p:positions) fielders.add(new Fielder(p[0],p[1]));
        ballY=26;
    }
    public void start(boolean practice, Difficulty difficulty) {
        this.practice=practice; this.difficulty=difficulty;
        runs=wickets=balls=lastRuns=0; history.clear(); paused=false; lofted=false;
        won=false; timing=""; result=""; accumulator=0; animation=0;
        ready();
    }
    public void menu() { phase=Phase.MENU; paused=false; accumulator=0; }
    public void setPaused(boolean value) {
        if(phase!=Phase.MENU && phase!=Phase.MATCH_OVER) paused=value;
        accumulator=0;
    }
    public void bowl() {
        if(phase!=Phase.READY || paused) return;
        phase=Phase.RUNUP; clock=0; swung=false; swingAt=-99; catcher=-1;
        timing=""; result=""; lastWicket=false; lastRuns=0;
        deliveryLine=(random.nextDouble()*2-1)*2.65;
        bounceFraction=.54+random.nextDouble()*.2;
        bounceHeight=.55+random.nextDouble()*.85;
        deliveryDuration=difficulty.deliverySeconds*(.87+random.nextDouble()*.23);
        deliveryName=deliveryDuration<difficulty.deliverySeconds*.95 ? "QUICK PACE" : "CHANGE OF PACE";
        if(bounceFraction>.69) deliveryName="FULL DELIVERY";
        ballX=0; ballY=26; ballZ=0;
    }
    public void toggleLoft() {
        if(!paused && phase!=Phase.SHOT && phase!=Phase.RETURN && phase!=Phase.RESULT) lofted=!lofted;
    }
    /** One swing per delivery. A held button never repeats. */
    public boolean swing(int side) {
        if(phase!=Phase.DELIVERY || paused || swung) return false;
        swung=true; shotSide=side<0?-1:1; swingAt=animation;
        timingError=clock-deliveryDuration;
        if(Math.abs(timingError)>difficulty.window) {
            timing=timingError<0?"TOO EARLY":"TOO LATE";
            return false;
        }
        double normalized=Math.abs(timingError)/difficulty.window;
        quality=1-.70*normalized;
        boolean wrongSide=Math.abs(deliveryLine)>1.1 && Math.signum(deliveryLine)!=shotSide;
        if(wrongSide) quality*=.72;
        timing=Math.abs(timingError)<=difficulty.perfect?"PERFECT":timingError<0?"EARLY":"LATE";
        detail=wrongSide?"Across the line - less control":lofted?"Aerial shot - watch the field":"Along the ground - find the gap";
        double angle=Math.toRadians(53+timingError/difficulty.window*27+deliveryLine*2);
        double speed=lofted?23+quality*21:12+quality*30;
        velocityX=shotSide*Math.sin(angle)*speed;
        velocityY=-Math.cos(angle)*speed;
        velocityZ=lofted?5+quality*11:1.4;
        ballX=deliveryLine; ballY=BATTER_Y; ballZ=lofted?.9:.25;
        ballPreviousZ=ballZ;
        grounded=false; shotClock=0; clock=0; phase=Phase.SHOT;
        feedback.emit("hit");
        return true;
    }
    public void update(double seconds) {
        if(paused) return;
        // Drop long background gaps; never accelerate the ball after a resume.
        accumulator+=Math.min(Math.max(seconds,0),.10);
        while(accumulator+1e-10>=STEP) { tick(STEP); accumulator-=STEP; }
    }
    private void tick(double dt) {
        animation+=dt; clock+=dt;
        switch(phase) {
            case RUNUP:
                if(clock>=1.0) { phase=Phase.DELIVERY; clock=0; ballZ=2.6; feedback.emit("release"); }
                break;
            case DELIVERY:
                double t=clock/deliveryDuration;
                ballX=deliveryLine*Math.min(1,t);
                ballY=26+(BATTER_Y-26)*t;
                if(t<bounceFraction) ballZ=2.6*(1-t/bounceFraction);
                else {
                    double p=(t-bounceFraction)/(1-bounceFraction);
                    ballZ=Math.max(0,bounceHeight*(2*p-p*p));
                }
                if(ballPreviousZ>.04 && ballZ<=.04 && t<.85) feedback.emit("bounce");
                ballPreviousZ=ballZ;
                if(clock>deliveryDuration+difficulty.window) {
                    if(!swung) timing="MISSED";
                    boolean bowled=Math.abs(deliveryLine)<1.12;
                    finish(0,bowled,bowled?"BOWLED":"DOT BALL",bowled?"Protect your stumps. Watch the bounce.":"Through to the keeper. No run.");
                }
                break;
            case SHOT: updateShot(dt); break;
            case RETURN:
                shotClock+=dt;
                double p=Math.min(1,clock/returnDuration);
                Fielder f=fielders.get(catcher);
                ballX=f.x*(1-p); ballY=f.y+(BATTER_Y-f.y)*p; ballZ=Math.sin(p*Math.PI)*3;
                if(p>=1) {
                    int completed=Math.min(3,Math.max(0,(int)Math.floor((shotClock-RUN_REACTION)/RUN_SECONDS)));
                    finish(completed,false,completed==0?"DOT BALL":completed==1?"1 RUN":completed+" RUNS","Fielded and returned. Running is automatic.");
                }
                break;
            case RESULT:
                if(clock>=1.8) {
                    if(isFinished()) { phase=Phase.MATCH_OVER; clock=0; feedback.emit(won?"win":"end"); }
                    else ready();
                }
                break;
            default: break;
        }
    }
    private void updateShot(double dt) {
        shotClock+=dt;
        double oldX=ballX,oldY=ballY;
        ballX+=velocityX*dt; ballY+=velocityY*dt;
        ballZ+=velocityZ*dt; velocityZ-=GRAVITY*dt;
        boolean hitGround=ballZ<=0;
        if(hitGround) {
            ballZ=0;
            if(!grounded) feedback.emit("bounce");
            velocityZ=Math.abs(velocityZ)>.9?-velocityZ*.22:0;
        }
        // Boundary is evaluated before fielding, with crossing height to decide 4 versus 6.
        if(Math.hypot(ballX,ballY)>=RADIUS) {
            double oldR=Math.hypot(oldX,oldY), newR=Math.hypot(ballX,ballY);
            double fraction=clamp((RADIUS-oldR)/Math.max(.0001,newR-oldR),0,1);
            double crossingZ=ballPreviousZ+(ballZ-ballPreviousZ)*fraction;
            boolean six=!grounded && (!hitGround || crossingZ>.02);
            finish(six?6:4,false,six?"SIX!":"FOUR!",six?"All the way. Cleared the rope.":"Found the gap. Away to the boundary.");
            return;
        }
        if(hitGround) grounded=true;
        ballPreviousZ=ballZ;
        double speed=Math.hypot(velocityX,velocityY);
        double deceleration=grounded && ballZ<.25?15:.6;
        double factor=speed>.001?Math.max(0,1-deceleration*dt/speed):0;
        velocityX*=factor; velocityY*=factor;
        // Chase projected landing point while airborne, current ball position once it bounces.
        double airSeconds=grounded?0:Math.max(0,(velocityZ+Math.sqrt(velocityZ*velocityZ+2*GRAVITY*ballZ))/GRAVITY);
        double chaseX=ballX+velocityX*airSeconds,chaseY=ballY+velocityY*airSeconds;
        double chaseRadius=Math.hypot(chaseX,chaseY);
        if(chaseRadius>RADIUS-1) { chaseX*= (RADIUS-1)/chaseRadius; chaseY*= (RADIUS-1)/chaseRadius; }
        int nearest=-1; double best=Double.MAX_VALUE;
        for(int i=0;i<fielders.size();i++) {
            Fielder f=fielders.get(i); double d=Math.hypot(chaseX-f.x,chaseY-f.y);
            if(d<best) { best=d; nearest=i; }
        }
        for(int i=0;i<fielders.size();i++) {
            Fielder f=fielders.get(i);
            if(i==nearest && shotClock>.22) {
                double dx=chaseX-f.x,dy=chaseY-f.y,d=Math.hypot(dx,dy);
                double travel=Math.min(d,(difficulty==Difficulty.ELITE?10.5:difficulty==Difficulty.PRO?9.2:8.2)*dt);
                if(d>.0001) { f.x+=dx/d*travel; f.y+=dy/d*travel; }
            }
            double distance=Math.hypot(ballX-f.x,ballY-f.y);
            if(shotClock>.22 && distance<1.8 && ballZ<2.1 && (!grounded || ballZ<.9)) {
                catcher=i;
                if(!grounded) finish(0,true,"CAUGHT","The fielder was under it. Try a ground shot.");
                else {
                    phase=Phase.RETURN; clock=0;
                    returnDuration=.45+Math.hypot(f.x,f.y-BATTER_Y)/34;
                }
                return;
            }
        }
        // Last-resort stop protects the match if tuning ever creates an unreachable ball.
        if(shotClock>18) finish(3,false,"3 RUNS","Retrieved from the deep.");
    }
    private void finish(int awarded,boolean wicket,String label,String explanation) {
        if(phase==Phase.RESULT || phase==Phase.MATCH_OVER) return;
        balls++; lastRuns=awarded; lastWicket=wicket; runs+=awarded;
        if(wicket) wickets++;
        history.add(wicket?"W":Integer.toString(awarded));
        result=label; detail=explanation; phase=Phase.RESULT; clock=0;
        won=!practice && runs>=difficulty.target;
        feedback.emit(wicket?"wicket":awarded>=4?"boundary":"run");
    }
    private void ready() {
        phase=Phase.READY; clock=0; swung=false; swingAt=-99;
        ballX=0; ballY=26; ballZ=0; ballPreviousZ=0; catcher=-1;
        for(Fielder f:fielders) f.reset();
    }
    public boolean isFinished() { return !practice && (won || balls>=MAX_BALLS || wickets>=MAX_WICKETS); }
    public int needed() { return Math.max(0,difficulty.target-runs); }
    public int ballsLeft() { return Math.max(0,MAX_BALLS-balls); }
    public String overs() { return balls/6+"."+balls%6; }
    public double deliveryProgress() { return phase==Phase.DELIVERY?clock/deliveryDuration:0; }
    public double swingProgress() { return clamp((animation-swingAt)/.34,0,1); }
    public static double clamp(double n,double min,double max) { return Math.max(min,Math.min(max,n)); }
}
