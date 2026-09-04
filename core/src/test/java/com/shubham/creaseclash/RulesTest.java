package com.shubham.creaseclash;

import java.util.*;
import static com.shubham.creaseclash.CricketGame.*;

/** Behavioral checks, runnable with plain JDK 17. No network or test dependencies. */
public final class RulesTest {
    private static int checks;
    private static void check(boolean condition,String message) {
        checks++; if(!condition) throw new AssertionError(message);
    }
    private static CricketGame fresh(long seed) {
        CricketGame g=new CricketGame(seed,event->{}); g.start(false,Difficulty.CLUB); return g;
    }
    private static void delivery(CricketGame g) {
        g.bowl(); int guard=0;
        while(g.phase!=Phase.DELIVERY && guard++<500) g.update(STEP);
        check(g.phase==Phase.DELIVERY,"Delivery starts after run-up");
    }
    private static void untilResult(CricketGame g) {
        int guard=0; while(g.phase!=Phase.RESULT && guard++<4000) g.update(STEP);
        check(g.phase==Phase.RESULT,"Every delivery resolves");
    }
    private static void next(CricketGame g) {
        int guard=0; while(g.phase==Phase.RESULT && guard++<300) g.update(STEP);
    }
    private static void ideal(CricketGame g) {
        while(g.clock<g.deliveryDuration) g.update(STEP);
        check(g.swing(g.deliveryLine<0?-1:1),"An ideal swing connects");
    }
    private static void placeShot(CricketGame g,double x,double y,double z,boolean bounced) {
        g.phase=Phase.SHOT; g.ballX=x; g.ballY=y; g.ballZ=z;
        g.velocityX=0; g.velocityY=0; g.velocityZ=0;
        g.grounded=bounced; g.shotClock=.4; g.clock=.4;
    }
    public static void main(String[] args) {
        CricketGame g=fresh(8); delivery(g); g.update(.2);
        double pausedClock=g.clock; int pausedBalls=g.balls;
        g.setPaused(true); g.update(5); g.swing(1); g.bowl();
        check(g.clock==pausedClock && g.balls==pausedBalls && !g.swung,"Pause freezes simulation and rejects actions");
        g.setPaused(false); g.update(STEP); check(g.clock<pausedClock+.02,"Resume does not accumulate paused time");

        g=fresh(11); g.start(false,Difficulty.PRO); delivery(g);
        check(!g.swing(1) && !g.swung,"A very early timed tap does not use up the swing");
        while(g.clock<g.deliveryDuration) g.update(STEP);
        check(g.swing(1),"A second tap rescues premature input in timed mode");
        check(!g.swing(-1),"Cannot hit the same ball twice");
        untilResult(g); check(g.balls==1,"A corrected shot counts exactly one ball");
        for(int i=0;i<30;i++) g.update(STEP);
        check(g.balls==1,"Result animation does not score twice");

        // Phone controls must connect even when pressed well before the delivery arrives.
        for(int seed=0;seed<100;seed++) for(int side:new int[]{-1,1}) {
            g=fresh(seed*982451653L);
            check(g.swing(side),"Club shot starts delivery from ready");
            check(g.phase==Phase.RUNUP && g.shotQueued && !g.swung,"Early shot remains queued");
            int guard=0; while(g.phase!=Phase.SHOT && guard++<700) g.update(STEP);
            check(g.phase==Phase.SHOT && g.hits==1,"Queued Club input produces real contact");
            check(Math.signum(g.velocityX)==side && g.velocityY>0,"Shot goes to selected screen side and down the pitch");
            check(g.timing.equals("CLEAN HIT") && g.quality==1,"Club removes timing and line penalties");
            untilResult(g); check(!g.lastWicket,"Assisted ground shot stays safe");
        }
        g=fresh(2); g.swing(-1); g.swing(1);
        check(g.shotSide==1,"Can change a queued shot direction");
        g.setPaused(true); double queuedClock=g.clock; g.update(10);
        check(g.shotQueued && g.clock==queuedClock,"Pause retains the queued shot without advancing");
        g.start(true,Difficulty.CLUB); check(!g.shotQueued,"Restart clears queued input");
        delivery(g); g.lofted=true; ideal(g);
        check(g.timing.equals("CLEAN HIT") && g.quality==1,"Club contact is assisted");
        untilResult(g);

        g=fresh(4); placeShot(g,61.9,0,5,false); g.velocityX=30;
        g.update(STEP); check(g.runs==6,"Airborne rope crossing scores six");
        g=fresh(4); placeShot(g,61.9,0,0,true); g.velocityX=30;
        g.update(STEP); check(g.runs==4,"Bounced rope crossing scores four");

        g=fresh(5); Fielder f=g.fielders.get(0); placeShot(g,f.x,f.y,1,false);
        g.update(STEP); check(g.lastWicket && g.wickets==1 && g.runs==0,"Low aerial ball at fielder is caught");
        g=fresh(5); f=g.fielders.get(0); placeShot(g,f.x,f.y,0,true);
        g.update(STEP); check(g.phase==Phase.RETURN && g.wickets==0,"Grounded ball is collected, not caught");
        untilResult(g); check(!g.lastWicket,"Return completes without wicket");

        for(int expected=1;expected<=3;expected++) {
            g=fresh(5); g.phase=Phase.RETURN; g.catcher=0;
            g.returnDuration=.01; g.clock=0;
            g.shotClock=RUN_REACTION+expected*RUN_SECONDS+.05;
            untilResult(g); check(g.runs==expected,"Automatic running can complete "+expected+" runs");
        }

        g=fresh(7); g.runs=23; placeShot(g,61.9,0,5,false); g.velocityX=30;
        g.update(STEP); check(g.won && g.runs==29 && g.balls==1,"Boundary counts in full when target is passed");
        next(g); check(g.phase==Phase.MATCH_OVER,"Successful chase ends after result");
        int score=g.runs; g.bowl(); g.swing(1); g.update(.1);
        check(g.runs==score && g.balls==1,"No play after match over");

        for(int seed=0;seed<100;seed++) {
            g=fresh(seed); int guard=0;
            while(g.phase!=Phase.MATCH_OVER && guard++<20000) {
                if(g.phase==Phase.READY) g.bowl(); g.update(STEP);
            }
            check(g.phase==Phase.MATCH_OVER,"No-input innings terminates, seed "+seed);
            check(g.balls<=12 && g.wickets<=3,"Innings limits are respected");
            check(g.runs==0 && !g.won,"No input cannot win");
        }
        g=fresh(42); g.start(true,Difficulty.CLUB);
        for(int ball=0;ball<40;ball++) { delivery(g); untilResult(g); next(g); }
        check(g.phase==Phase.READY && g.balls==40,"Practice continues beyond normal wickets and overs");

        // Same elapsed time and inputs must yield the same outcome at 60 and 120 Hz.
        CricketGame a=fresh(99),b=fresh(99); a.bowl(); b.bowl();
        for(int i=0;i<180;i++) { a.update(1.0/60); b.update(STEP); b.update(STEP); }
        check(a.phase==b.phase && a.balls==b.balls && a.wickets==b.wickets && Math.abs(a.ballY-b.ballY)<1e-8,"Frame-rate independent simulation");

        FakePlatform p=new FakePlatform(); GameSession s=new GameSession(44,p);
        s.tap(370,456); check(s.selected==Difficulty.PRO,"Difficulty selector works");
        s.tap(200,550); check(s.game.phase==Phase.READY && s.game.difficulty==Difficulty.PRO,"Play starts selected difficulty");
        s.tap(835,720); check(s.game.lofted,"Loft control works");
        s.tap(1270,55); check(!s.haptics && p.readInt("haptics",1)==0,"Haptic setting persists");
        s.tap(1360,55); check(s.game.paused,"Pause control works");
        s.tap(720,390); check(!s.game.paused,"Resume control works");
        s.background(); check(s.game.paused,"Backgrounding pauses active match");
        s.game.menu(); s.tap(400,625); check(s.help,"Help opens"); s.tap(10,10); check(!s.help,"Help closes");
        s.start(false); s.game.runs=35; s.game.phase=Phase.MATCH_OVER; s.update(STEP);
        check(s.best==35 && p.readInt("best",0)==35,"Completed chase records best score");
        s.start(true); s.game.runs=90; s.update(STEP); check(s.best==35,"Practice does not inflate best chase score");

        s.selected=Difficulty.CLUB; s.start(true); s.tap(240,730);
        check(s.game.shotQueued && s.game.shotSide==-1,"Left touch queues on-side shot");
        s.tap(1180,730); check(s.game.shotSide==1,"Right touch changes to off-side");
        untilResult(s.game); check(s.game.hits==1 && s.game.runs>0,"Touch controls score runs without precision timing");

        Map<String,Integer> outcomes=new TreeMap<>();
        for(Difficulty difficulty:Difficulty.values()) for(int seed=0;seed<120;seed++) {
            // Widely separated seeds avoid Random's correlated first output for small adjacent seeds.
            g=fresh(seed*982451653L); g.start(true,difficulty); delivery(g);
            double offset=((seed%9)-4)*difficulty.window/5;
            while(g.clock<g.deliveryDuration+offset) g.update(STEP);
            g.lofted=seed%2==0; g.swing(seed%3==0?1:(g.deliveryLine<0?-1:1));
            untilResult(g);
            check(Double.isFinite(g.ballX) && Double.isFinite(g.ballY) && Double.isFinite(g.ballZ),"Finite ball position");
            check(g.lastRuns>=0 && g.lastRuns<=6 && g.lastRuns!=5,"Valid scoring outcome");
            outcomes.merge(g.lastWicket?"W":Integer.toString(g.lastRuns),1,Integer::sum);
        }
        check(outcomes.containsKey("W") && outcomes.containsKey("1") && outcomes.containsKey("2") && outcomes.containsKey("4") && outcomes.containsKey("6"),"Shot matrix exercises wickets, running and boundaries");
        System.out.println("PASS: "+checks+" behavioral checks. 360-shot outcome matrix: "+outcomes);
    }
    private static final class FakePlatform implements GameSession.Platform {
        private final Map<String,Integer> values=new HashMap<>();
        public int readInt(String k,int f) { return values.getOrDefault(k,f); }
        public void writeInt(String k,int v) { values.put(k,v); }
        public void sound(String e) { }
        public void vibrate(String e) { }
    }
}
