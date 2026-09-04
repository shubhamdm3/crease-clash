package com.shubham.creaseclash;

import static com.shubham.creaseclash.CricketGame.Phase;

/** Original vector artwork. No downloaded cricket artwork, logos, or player likenesses. */
public final class GameRenderer {
    public static final int W=1440,H=810;
    private static final int BG=0xff10292d,INK=0xff102b30,PANEL=0xff19363a,WHITE=0xfff5f2df;
    private static final int MUTED=0xffa6b9b4,LIME=0xffdbef7d,CORAL=0xfff58a71,GRASS=0xff48785d;
    private Draw d;
    private GameSession session;
    private CricketGame g;
    private double cx=850,cy=394,sx=489.0/CricketGame.RADIUS,sy=233.0/CricketGame.RADIUS;

    public void render(Draw draw,GameSession session) {
        d=draw; this.session=session; g=session.game;
        d.rect(0,0,W,H,BG);
        stadium();
        header();
        if(g.phase==Phase.MENU) menu();
        else {
            scoreboard(); controls(); liveCaption();
            if(g.phase==Phase.MATCH_OVER) summary();
            else if(g.paused) pause();
        }
        if(session.help) help();
    }
    private void header() {
        d.text("CREASE",42,49,27,WHITE,false);
        d.text("CLASH",42,78,27,LIME,false);
        d.line(204,30,204,80,1,0xff46605c);
        d.text("TWO OVERS. ONE CHASE.",232,51,14,MUTED,false);
        d.text(g.phase==Phase.MENU?"OFFLINE CRICKET CLUB":g.practice?"PRACTICE NETS":g.difficulty.label+" / TARGET CHASE",232,77,16,WHITE,false);
        d.roundRect(927,36,184,35,17,0xff24453f);
        d.oval(945,53,4,4,LIME);
        d.text("OFFLINE / SINGLE PLAYER",1032,58,10,LIME,true);
        smallButton(1140,"SOUND",session.sound);
        smallButton(1230,"HAPTIC",session.haptics);
        d.roundRect(1320,28,78,54,13,PANEL);
        if(g.phase==Phase.MENU) d.text("v0.1",1359,61,15,MUTED,true);
        else { d.rect(1349,44,6,22,WHITE); d.rect(1363,44,6,22,WHITE); }
        d.line(40,106,1400,106,1,0xff36504d);
    }
    private void smallButton(double x,String title,boolean on) {
        d.roundRect(x,28,78,54,13,PANEL);
        d.text(title,x+39,49,10,MUTED,true);
        d.text(on?"ON":"OFF",x+39,69,13,on?LIME:CORAL,true);
    }
    private void stadium() {
        // Stadium silhouette and seating tiers remain behind the rope.
        d.oval(cx,cy+4,534,266,0xff213d3d);
        d.oval(cx,cy,522,252,0xff385a51);
        for(int row=0;row<3;row++) {
            for(int i=0;i<88;i++) {
                double angle=Math.PI+(i+.5)*Math.PI/88;
                double x=cx+Math.cos(angle)*(511+row*8),y=cy+Math.sin(angle)*(239+row*7);
                int color=(i+row*3)%7==0?LIME:(i+row)%4==0?CORAL:0xff829a82;
                d.oval(x,y,2.7,2.7,color);
            }
        }
        floodlight(385,162); floodlight(1320,162);
        d.oval(cx,cy,494,238,0xffd5d8ac);
        d.oval(cx,cy,489,233,GRASS);
        for(int ring=5;ring>=1;ring--) {
            d.oval(cx,cy,489*ring/6.0,233*ring/6.0,ring%2==0?0xff48785d:0xff4b7c60);
        }
        // Dotted inner ring.
        for(int i=0;i<70;i++) {
            double a=i*Math.PI*2/70;
            d.oval(cx+Math.cos(a)*252,cy+Math.sin(a)*120,1.4,1.4,0xff8aab80);
        }
        d.text("OFF SIDE",472,631,11,0xff8fb09a,false);
        d.text("LEG SIDE",1272,631,11,0xff8fb09a,true);
        // Square and pitch.
        d.polygon(new double[]{cx-45,py(-23),cx+45,py(-23),cx+59,py(30),cx-59,py(30)},0xff7e946b);
        d.polygon(new double[]{cx-25,py(-21),cx+25,py(-21),cx+33,py(27),cx-33,py(27)},0xffc9ba88);
        d.polygon(new double[]{cx-19,py(-20),cx+19,py(-20),cx+27,py(26),cx-27,py(26)},0xffd3c291);
        for(int i=0;i<35;i++) {
            double y=py(-19)+i*4.5;
            d.line(cx-15+(i%4)*5,y,cx-10+(i%4)*5,y+.5,.7,0xffb8aa7a);
        }
        crease(-16,31); crease(24,39);
        stumps(cx+5,py(-19),.85,g.lastWicket && g.result.equals("BOWLED") && g.phase==Phase.RESULT);
        stumps(cx-7,py(26),.9,false);
        // Keeper and distant fielders drawn first.
        for(int i=0;i<g.fielders.size();i++) {
            CricketGame.Fielder f=g.fielders.get(i);
            double stride=g.phase==Phase.SHOT?Math.sin(g.animation*12+i):0;
            player(px(f.x),py(f.y),.63,0xffed977f,stride,false,false,0);
        }
        player(cx+3,py(-25),.55,0xffed977f,0,false,false,0);
        // Automatic running shares the simulation's run interval.
        boolean running=g.phase==Phase.SHOT || g.phase==Phase.RETURN;
        double runProgress=CricketGame.clamp((g.shotClock-CricketGame.RUN_REACTION)/CricketGame.RUN_SECONDS,0,3);
        double leg=runProgress%2;
        double runFraction=leg>1?2-leg:leg;
        double batterY=running?CricketGame.BATTER_Y+(24-CricketGame.BATTER_Y)*runFraction:CricketGame.BATTER_Y;
        double nonstrikerY=running?24-(24-CricketGame.BATTER_Y)*runFraction:22;
        double runningStride=running?Math.sin(g.animation*18):0;
        player(cx-47,py(nonstrikerY),.72,LIME,runningStride,true,false,0);
        player(cx+61,py(32),.68,WHITE,0,false,false,0);
        double bowlerY=26;
        double stride=0,arm=0;
        if(g.phase==Phase.RUNUP) { bowlerY=43-g.clock*17; stride=Math.sin(g.clock*19); arm=g.clock>.72?(g.clock-.72)/.28:0; }
        else if(g.phase==Phase.DELIVERY) { bowlerY=26-Math.min(4,g.clock*7); stride=Math.sin(g.clock*13)*.5; arm=Math.max(0,1-g.clock*3); }
        player(cx-3,py(bowlerY),.89,CORAL,stride,false,false,arm);
        double swing=g.swingProgress();
        player(cx-14,py(batterY),.9,LIME,runningStride,true,g.swung && swing<1,swing);
        if(g.phase==Phase.DELIVERY) {
            // Landing marker, not a timing meter: players must watch the actual ball.
            double bx=px(g.deliveryLine*g.bounceFraction),by=py(26+(-16-26)*g.bounceFraction);
            d.oval(bx,by,10,4,0xffbdc78a);
            d.oval(bx,by,7,2.5,GRASS);
        }
        if(g.phase==Phase.DELIVERY || g.phase==Phase.SHOT || g.phase==Phase.RETURN || g.phase==Phase.RESULT) ball();
        if(g.phase==Phase.RESULT && g.lastRuns>=4) {
            for(int i=0;i<22;i++) {
                double x=380+(i*71)%980,y=145+(i*53)%380+g.clock*29;
                d.rect(x,y,3+(i%3),6,i%2==0?LIME:CORAL);
            }
        }
    }
    private void floodlight(double x,double y) {
        d.line(x,y,x,y+52,4,0xff70877b);
        d.roundRect(x-25,y-10,50,19,4,0xff5b7668);
        for(int i=0;i<5;i++) d.rect(x-20+i*9,y-6,5,9,0xffc9d6a6);
    }
    private void crease(double worldY,double width) {
        double y=py(worldY);
        d.line(cx-width,y,cx+width,y,2,WHITE);
        d.line(cx-25,y-9,cx-25,y+13,1.5,WHITE);
        d.line(cx+25,y-9,cx+25,y+13,1.5,WHITE);
    }
    private void stumps(double x,double y,double s,boolean broken) {
        for(int i=0;i<3;i++) d.line(x+(i-1)*5*s,y,x+(i-1)*5*s+(broken?(i-1)*14:0),y-22*s,2.8*s,WHITE);
        if(!broken) d.line(x-6*s,y-23*s,x+6*s,y-23*s,2.2*s,CORAL);
        else { d.line(x-13,y-26,x-7,y-30,2,WHITE); d.line(x+14,y-21,x+20,y-18,2,WHITE); }
    }
    private void player(double x,double y,double s,int shirt,double stride,boolean batter,boolean swinging,double arm) {
        d.oval(x,y+2,14*s,4*s,0xff355c4a);
        double hipY=y-22*s,shoulderY=y-43*s;
        d.line(x,hipY,x-9*s-stride*4*s,y-2*s,5*s,INK);
        d.line(x,hipY,x+9*s+stride*4*s,y-2*s,5*s,INK);
        if(batter) {
            d.line(x-6*s,y-15*s,x-9*s,y-3*s,5*s,WHITE);
            d.line(x+6*s,y-15*s,x+9*s,y-3*s,5*s,WHITE);
        }
        d.line(x-11*s-stride*4*s,y,x-3*s-stride*4*s,y,4*s,WHITE);
        d.line(x+5*s+stride*4*s,y,x+13*s+stride*4*s,y,4*s,WHITE);
        d.line(x,shoulderY,x,hipY,13*s,shirt);
        d.oval(x,y-55*s,8*s,8*s,0xffedc294);
        if(batter) {
            d.oval(x,y-59*s,9*s,6*s,INK);
            d.line(x+4*s,y-55*s,x+11*s,y-55*s,3*s,INK);
            double handX=x+13*s,handY=y-33*s;
            if(swinging) {
                double a=-1.5+arm*3.5;
                handX=x+Math.cos(a)*20*s*g.shotSide;
                handY=y-36*s+Math.sin(a)*11*s;
                double tipX=handX+Math.cos(a)*29*s*g.shotSide,tipY=handY+Math.sin(a)*29*s;
                d.line(handX,handY,tipX,tipY,7*s,0xffedd09b);
            } else d.line(handX+3*s,handY,handX+10*s,y-4*s,7*s,0xffedd09b);
            d.line(x-4*s,shoulderY+4*s,handX,handY,4.5*s,shirt);
            d.line(x+4*s,shoulderY+4*s,handX,handY,4.5*s,shirt);
            d.oval(handX,handY,4*s,4*s,WHITE);
        } else {
            d.line(x-5*s,shoulderY+4*s,x-15*s,shoulderY+19*s+stride*4*s,4.5*s,shirt);
            d.line(x+5*s,shoulderY+4*s,x+14*s,arm>0?shoulderY-23*s*arm:shoulderY+18*s-stride*4*s,4.5*s,shirt);
        }
    }
    private void ball() {
        double x=px(g.ballX),ground=py(g.ballY),y=ground-g.ballZ*10.8;
        d.oval(x,ground+2,4+g.ballZ*.35,2.2,0xff294e42);
        if(g.phase==Phase.SHOT) {
            for(int i=4;i>=1;i--) {
                double tx=x-g.velocityX*sx*.012*i,ty=y-(g.velocityY*sy-g.velocityZ*10.8)*.012*i;
                d.oval(tx,ty,Math.max(1,4-i*.6),Math.max(1,4-i*.6),0xffbed28b);
            }
        } else if(g.phase==Phase.DELIVERY) d.line(x,y+5,x,y+17,3,0xffe4c898);
        d.oval(x,y,7.3,7.3,INK); d.oval(x,y,5.7,5.7,0xfffff8e5);
        d.line(x-2,y+2,x+2,y-2,1.6,CORAL);
    }
    private void scoreboard() {
        d.roundRect(40,137,272,511,23,PANEL);
        d.text(g.practice?"PRACTICE NETS":"THE CHASE",64,174,13,LIME,false);
        d.text(Integer.toString(g.runs),63,255,72,WHITE,false);
        d.text("/ "+g.wickets,190,250,28,MUTED,false);
        d.text("RUNS",65,281,11,MUTED,false);
        d.text("WICKETS",190,281,11,MUTED,false);
        d.line(64,302,288,302,1,0xff3b5450);
        d.text(g.practice?"OVERS BOWLED":"OVERS",64,331,11,MUTED,false);
        d.text(g.overs()+(g.practice?"":" / 2"),64,363,25,WHITE,false);
        if(!g.practice) {
            d.text("TARGET",217,331,11,MUTED,false);
            d.text(Integer.toString(g.difficulty.target),217,363,25,LIME,false);
        }
        d.roundRect(62,387,228,88,14,0xff25433d);
        if(g.practice) {
            d.text("NO BALL LIMIT",176,420,17,LIME,true);
            d.text("Learn the bounce. Find your timing.",176,449,10,WHITE,true);
        } else {
            d.text(g.needed()+" FROM "+g.ballsLeft(),176,423,26,LIME,true);
            d.text("RUNS NEEDED / BALLS LEFT",176,451,10,MUTED,true);
        }
        d.text("LAST SIX BALLS",64,511,11,MUTED,false);
        int start=Math.max(0,g.history.size()-6);
        for(int i=0;i<6;i++) {
            String value=start+i<g.history.size()?g.history.get(start+i):"-";
            int color=value.equals("W")?CORAL:value.equals("4")||value.equals("6")?LIME:WHITE;
            d.oval(78+i*36,541,14,14,value.equals("-")?0xff234145:color);
            d.text(value,78+i*36,546,12,value.equals("-")?MUTED:INK,true);
        }
        d.text("YOUR BEST CHASE SCORE",64,595,10,MUTED,false);
        d.text(session.best+" RUNS",64,622,20,WHITE,false);
        d.text("READ THE BALL. TRUST YOUR TIMING.",40,792,11,MUTED,false);
    }
    private void controls() {
        boolean active=g.phase==Phase.DELIVERY && !g.swung;
        button(344,682,276,92,"LEFT SHOT","OFF SIDE",active?LIME:0xff35504b,active?INK:WHITE);
        button(1116,682,276,92,"RIGHT SHOT","LEG SIDE",active?LIME:0xff35504b,active?INK:WHITE);
        String center=g.phase==Phase.READY?"BOWL":g.phase==Phase.RUNUP?"GET READY":g.phase==Phase.DELIVERY?"WATCH IT":g.phase==Phase.RESULT?"NEXT BALL...":"IN PLAY";
        button(658,682,242,92,center,g.phase==Phase.READY?"TAP TO FACE A DELIVERY":"",g.phase==Phase.READY?LIME:PANEL,g.phase==Phase.READY?INK:MUTED);
        button(928,682,160,92,g.lofted?"LOFT":"GROUND","TAP TO SWITCH",g.lofted?CORAL:PANEL,g.lofted?INK:WHITE);
        d.text("Choose a side. Tap as the ball reaches the bat.",869,803,11,MUTED,true);
    }
    private void liveCaption() {
        String title,sub;
        int color=WHITE;
        switch(g.phase) {
            case READY: title="YOUR CREASE. YOUR CALL."; sub="Choose ground or loft. Tap BOWL when ready."; break;
            case RUNUP: title=g.deliveryName; sub="Watch the release, then the bounce."; break;
            case DELIVERY: title=g.swung?g.timing:"WATCH THE BALL"; sub=g.swung?"One swing per delivery.":"Tap LEFT or RIGHT as it reaches your bat."; break;
            case SHOT: title=g.timing; sub=g.detail; color=g.timing.equals("PERFECT")?LIME:WHITE; break;
            case RETURN: title="COMING BACK IN"; sub="The fielders are returning the ball."; break;
            case RESULT: title=g.result; sub=g.detail; color=g.lastWicket?CORAL:g.lastRuns>=4?LIME:WHITE; break;
            default: return;
        }
        d.roundRect(605,128,490,63,15,0xee19363a);
        d.text(title,850,156,20,color,true);
        d.text(sub,850,178,11,MUTED,true);
        if(g.phase==Phase.RESULT && (g.lastRuns>=4 || g.lastWicket)) {
            d.roundRect(719,542,262,79,18,0xf010292d);
            d.text(g.lastWicket?"W":Integer.toString(g.lastRuns),850,601,57,color,true);
        }
    }
    private void menu() {
        d.roundRect(40,136,530,583,26,0xf510292d);
        d.roundRect(76,168,160,28,14,0xff29453d);
        d.text("THE POCKET CRICKET CLUB",156,187,9,LIME,true);
        d.text("Two overs.",72,275,58,WHITE,false);
        d.text("One chase.",72,341,58,LIME,false);
        d.text("Pick your shot. Find the gap.",76,384,18,WHITE,false);
        d.text("Make the last ball count.",76,411,18,MUTED,false);
        d.roundRect(76,431,445,52,13,PANEL);
        d.text("DIFFICULTY",95,462,12,MUTED,false);
        d.text(session.selected.label+"  /  "+session.selected.target+" TO WIN",362,463,15,LIME,true);
        d.text(">",494,464,20,LIME,true);
        button(76,516,445,70,"PLAY THE CHASE","",LIME,INK);
        button(76,600,214,58,"PRACTICE","",PANEL,WHITE);
        button(307,600,214,58,"HOW TO PLAY","",PANEL,WHITE);
        d.text("12 BALLS  /  3 WICKETS  /  NO INTERNET NEEDED",76,692,11,MUTED,false);
        // Hero ball with original graphic motion lines.
        d.oval(1110,355,28,28,0xff244c40);
        d.line(1015,308,1072,267,5,0xffbdd58b);
        d.line(1044,326,1092,290,3,0xffbdd58b);
        d.oval(1122,253,27,27,WHITE);
        d.line(1108,270,1135,236,3,CORAL);
        for(int i=0;i<5;i++) d.line(1110+i*5,262-i*6,1116+i*5,266-i*6,1.5,CORAL);
        d.roundRect(970,576,313,59,16,0xee19363a);
        d.text("SIMPLE CONTROLS. REAL CONSEQUENCES.",1127,612,11,LIME,true);
        d.text("ORIGINAL ARCADE CRICKET / FIRST PLAYABLE",40,780,11,MUTED,false);
        d.text("JAVA + LIBGDX",1260,780,11,MUTED,false);
    }
    private void button(double x,double y,double w,double h,String title,String subtitle,int background,int foreground) {
        d.roundRect(x,y+4,w,h,16,0xff0c2025);
        d.roundRect(x,y,w,h,16,background);
        d.text(title,x+w/2,y+h/2+(subtitle.isEmpty()?6:-2),w<180?19:23,foreground,true);
        if(!subtitle.isEmpty()) d.text(subtitle,x+w/2,y+h/2+23,11,foreground,true);
    }
    private void dim() { d.rect(0,107,W,H-107,0xcb081b20); }
    private void pause() {
        dim(); d.roundRect(482,209,476,400,26,BG);
        d.text("TAKE A BREATHER",720,267,16,LIME,true);
        d.text("Match paused",720,315,35,WHITE,true);
        button(530,358,380,64,"RESUME","",LIME,INK);
        button(530,438,380,58,"RESTART MATCH","",PANEL,WHITE);
        button(530,512,380,58,"BACK TO CLUB","",PANEL,WHITE);
    }
    private void summary() {
        dim(); d.roundRect(482,185,476,502,26,BG);
        d.text(g.won?"CHASE COMPLETE":"INNINGS COMPLETE",720,233,14,g.won?LIME:CORAL,true);
        d.text(g.won?"You brought it home.":"One more innings?",720,286,30,WHITE,true);
        d.text(g.runs+" / "+g.wickets,720,377,76,LIME,true);
        d.text(g.overs()+" OVERS  /  TARGET "+g.difficulty.target,720,413,14,MUTED,true);
        String explanation=g.won?"Won with "+g.ballsLeft()+" balls to spare.":g.runs==g.difficulty.target-1?"Scores level. The chase ends in a tie.":"Lost by "+(g.difficulty.target-1-g.runs)+" runs.";
        d.text(explanation,720,457,15,WHITE,true);
        button(530,510,380,64,"PLAY AGAIN","",LIME,INK);
        button(530,590,380,58,"BACK TO CLUB","",PANEL,WHITE);
    }
    private void help() {
        dim(); d.roundRect(352,159,736,534,26,BG);
        d.text("MAKE EVERY BALL COUNT",720,214,29,LIME,true);
        String[][] rows={{"01","WATCH THE BOUNCE","Tap as the ball reaches the batter at the far end of the pitch."},
            {"02","PICK YOUR SIDE","LEFT hits to the off side. RIGHT hits to the leg side."},
            {"03","CHOOSE YOUR RISK","GROUND finds gaps. LOFT can clear the rope or get caught."},
            {"04","ONE TAP. ONE SHOT.","Early and late swings lose power. Holding a button does not repeat."},
            {"05","CHASE THE TARGET","Twelve balls, three wickets. Fielding and running are automatic."}};
        for(int i=0;i<rows.length;i++) {
            double y=268+i*71;
            d.text(rows[i][0],390,y+5,22,CORAL,false);
            d.text(rows[i][1],445,y,14,WHITE,false);
            d.text(rows[i][2],445,y+25,12,MUTED,false);
        }
        d.text("TAP ANYWHERE TO CLOSE",720,661,12,LIME,true);
    }
    private double px(double x) { return cx+x*sx; }
    private double py(double y) { return cy+y*sy; }
}
