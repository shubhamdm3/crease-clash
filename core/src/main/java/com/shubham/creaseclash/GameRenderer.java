package com.shubham.creaseclash;

import static com.shubham.creaseclash.CricketGame.Phase;

/** Original vector artwork. No downloaded cricket artwork, logos, or player likenesses. */
public final class GameRenderer {
    public static final int W=1440,H=810;
    private static final int BG=0xff101e32,INK=0xff152337,PANEL=0xff1e3348,WHITE=0xfff5f2df;
    private static final int MUTED=0xffa6b9b4,LIME=0xffffd76b,CORAL=0xfff58a71,GRASS=0xff48785d;
    private Draw d;
    private GameSession session;
    private CricketGame g;
    private double cx=720,cy=480,sx=10,sy=-2.8,wide;

    public void render(Draw draw,GameSession session) {
        d=draw; this.session=session; g=session.game;
        d.rect(0,0,W,H,BG);
        wide=g.phase==Phase.MENU?0:g.phase==Phase.SHOT?CricketGame.clamp((g.shotClock-.18)/.65,0,1):
            g.phase==Phase.RETURN || g.phase==Phase.RESULT || g.phase==Phase.MATCH_OVER?1:0;
        wide=wide*wide*(3-2*wide);
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
        if(g.phase==Phase.MENU) d.text("v0.4",1359,61,15,MUTED,true);
        else { d.rect(1349,44,6,22,WHITE); d.rect(1363,44,6,22,WHITE); }
        d.line(40,106,1400,106,1,0xff36504d);
    }
    private void smallButton(double x,String title,boolean on) {
        d.roundRect(x,28,78,54,13,PANEL);
        d.text(title,x+39,49,10,MUTED,true);
        d.text(on?"ON":"OFF",x+39,69,13,on?LIME:CORAL,true);
    }
    private void stadium() {
        // Evening sky and layered stands: generated from vector shapes on every platform.
        for(int i=0;i<24;i++) d.rect(0,107+i*7,1440,8,mix(0xff234565,0xffe3b698,i/23.0));
        d.oval(1200,182,32,32,0xffffe0a1);
        for(int i=0;i<24;i++) {
            double x=i*65,top=212-(i*37%39);
            d.rect(x,top,48,260-top,0xff445766);
        }
        d.polygon(new double[]{0,229,200,205,720,224,1210,205,1440,227,1440,322,0,322},0xff233447);
        for(int row=0;row<5;row++) {
            double y=237+row*13;
            d.line(0,y+7,1440,y+7,3,0xff10283d);
            for(int i=0;i<140;i++) {
                int color=(i*7+row*11)%13<3?0xfff9cb73:(i+row)%3==0?0xff7aa8cc:0xffd0c8b3;
                d.oval(i*10.5+(row%2)*5,y,2.2,3,color);
            }
        }
        floodlight(107,174); floodlight(1333,174);
        d.rect(0,302,1440,24,0xffe5e6d8);
        for(int i=0;i<8;i++) {
            d.rect(i*180+3,305,174,18,i%2==0?0xff244f70:0xffbf6156);
            d.text(i%2==0?"CREASE CLASH":"THE POCKET CRICKET CLUB",i*180+90,318,9,WHITE,true);
        }
        d.rect(0,326,1440,340,0xff428754);
        d.oval(720,480,795,183,0xff326e49);
        d.oval(720,480,783,177,0xffe4dfb7);
        d.oval(720,480,777,171,0xff4b955b);
        for(int ring=6;ring>=1;ring--) d.oval(720,480,777*ring/7.0,171*ring/7.0,ring%2==0?0xff4b955b:0xff438952);
        for(int i=0;i<84;i++) {
            double a=i*Math.PI*2/84;
            d.oval(720+Math.cos(a)*420,480+Math.sin(a)*94,1.6,1.2,0xffb0c995);
        }
        // Perspective from behind the right-handed batter. Positive X is the off side.
        pitchQuad(-4.8,4.8,-20,29,0xff869263);
        pitchQuad(-3.2,3.2,-20,27,0xffd7be89);
        pitchQuad(-2.9,2.9,-19,27,0xffe2ca97);
        for(int i=0;i<50;i++) {
            double y=-18+i*.88,x=(i%5-2)*.7;
            d.line(wx(x,y),py(y),wx(x+.3,y),py(y+.09),.8,0xffb5a071);
        }
        crease(-16,4.3); crease(24,4.3);
        d.line(wx(-3.2,-20),py(-20),wx(-3.2,27),py(27),1,0xffb49a6e);
        // Paint fielders before the closer players, clipping those outside the batting camera.
        for(int i=0;i<g.fielders.size();i++) {
            CricketGame.Fielder f=g.fielders.get(i);
            double x=wx(f.x,f.y),y=py(f.y);
            if(y<640 && y>265 && x>25 && x<1415) player(x,y,playerScale(f.y)*.77,CORAL,
                g.phase==Phase.SHOT?Math.sin(g.animation*12+i):0,false,false,0);
        }
        double bowlerY=g.phase==Phase.RUNUP?43-g.clock*17:26;
        double stride=g.phase==Phase.RUNUP?Math.sin(g.clock*19):g.phase==Phase.DELIVERY?Math.sin(g.clock*13)*.4:0;
        double arm=g.phase==Phase.RUNUP && g.clock>.72?(g.clock-.72)/.28:g.phase==Phase.DELIVERY?Math.max(0,1-g.clock*3):0;
        boolean running=g.phase==Phase.SHOT || g.phase==Phase.RETURN;
        double runStride=running?Math.sin(g.animation*18):0;
        player(wx(5.2,29),py(29),playerScale(29),WHITE,0,false,false,0);
        double nonStrikerY=running?g.nonStrikerRunY():23;
        player(wx(-5.2,nonStrikerY),py(nonStrikerY),playerScale(nonStrikerY),0xff559bdf,-runStride,true,false,0);
        stumps(wx(0,26),py(26),playerScale(26),false);
        player(wx(-.5,bowlerY),py(bowlerY),playerScale(bowlerY),CORAL,stride,false,false,arm);
        double by=running?g.strikerRunY():CricketGame.BATTER_Y;
        if(g.phase==Phase.RUNUP || g.phase==Phase.DELIVERY || g.phase==Phase.READY) {
            double y=py(-16),x=wx(0,-16);
            d.oval(x,y,61,13,0xff82b892);
            d.oval(x,y,53,8,0xffd4bd89);
            if(g.phase==Phase.DELIVERY) {
                double bounceY=26+(-16-26)*g.bounceFraction;
                d.oval(wx(g.deliveryXAt(g.bounceFraction),bounceY),py(bounceY),17,5,0xfffde6a2);
            }
        }
        double swing=g.swingProgress();
        player(wx(-2.7,by),py(by),playerScale(by),0xff559bdf,
            runStride,true,g.swung && swing<1,swing);
        stumps(wx(0,-17.2),py(-17.2),playerScale(-17.2)*.85,g.lastWicket && g.result.equals("BOWLED") && g.phase==Phase.RESULT);
        if(g.phase==Phase.DELIVERY || g.phase==Phase.SHOT || g.phase==Phase.RETURN || g.phase==Phase.RESULT) ball();
        if(g.phase==Phase.RESULT && g.lastRuns>=4) for(int i=0;i<44;i++) {
            double x=55+(i*71)%1330,y=234+(i*53)%330+g.clock*24;
            d.rect(x,y,3+(i%4),7,i%2==0?LIME:CORAL);
        }
        d.rect(0,654,1440,156,BG);
        d.text("ON SIDE / LEG",61,671,13,LIME,false);
        d.text("RIGHT-HANDED BATTER",720,671,10,MUTED,true);
        d.text("OFF SIDE",1325,671,13,LIME,true);
    }
    private static int mix(int a,int b,double t) {
        int r=(int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t);
        int g=(int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t);
        int bl=(int)((a&255)*(1-t)+(b&255)*t);
        return 0xff000000|(r<<16)|(g<<8)|bl;
    }
    private void pitchQuad(double left,double right,double near,double far,int color) {
        d.polygon(new double[]{wx(left,near),py(near),wx(right,near),py(near),wx(right,far),py(far),wx(left,far),py(far)},color);
    }
    private void floodlight(double x,double y) {
        d.polygon(new double[]{x-34,y+11,x+34,y+11,x+115,330,x-115,330},0x0dfef4ce);
        d.line(x,y,x,302,5,0xff7c929b);
        d.line(x+7,y,x+7,302,2,0xff30495c);
        d.roundRect(x-36,y-12,72,29,5,0xff496276);
        for(int row=0;row<2;row++) for(int i=0;i<6;i++) d.roundRect(x-30+i*10,y-7+row*11,7,8,2,0xfffff3cf);
    }
    private void crease(double y,double width) {
        d.line(wx(-width,y),py(y),wx(width,y),py(y),2.2,WHITE);
        for(int side:new int[]{-1,1}) d.line(wx(side*2.7,y-1.2),py(y-1.2),wx(side*2.7,y+2),py(y+2),1.8,WHITE);
    }
    private void stumps(double x,double y,double s,boolean broken) {
        for(int i=0;i<3;i++) d.line(x+(i-1)*5*s,y,x+(i-1)*5*s+(broken?(i-1)*14:0),y-22*s,2.8*s,WHITE);
        if(!broken) d.line(x-6*s,y-23*s,x+6*s,y-23*s,2.2*s,CORAL);
        else { d.line(x-13,y-26,x-7,y-30,2,WHITE); d.line(x+14,y-21,x+20,y-18,2,WHITE); }
    }
    private void player(double x,double y,double s,int shirt,double stride,boolean batter,boolean swinging,double arm) {
        d.oval(x+8*s,y+3,23*s,5*s,0x50304028);
        double hipY=y-22*s,shoulderY=y-43*s;
        d.line(x,hipY,x-9*s-stride*4*s,y-2*s,5*s,INK);
        d.line(x,hipY,x+9*s+stride*4*s,y-2*s,5*s,INK);
        if(batter) {
            d.line(x-6*s,y-15*s,x-9*s,y-3*s,5*s,WHITE);
            d.line(x+6*s,y-15*s,x+9*s,y-3*s,5*s,WHITE);
        }
        d.line(x-11*s-stride*4*s,y,x-3*s-stride*4*s,y,4*s,WHITE);
        d.line(x+5*s+stride*4*s,y,x+13*s+stride*4*s,y,4*s,WHITE);
        d.line(x,shoulderY,x,hipY,15*s,INK);
        d.line(x,shoulderY,x,hipY,12*s,shirt);
        d.line(x+3*s,shoulderY+3*s,x+3*s,hipY-2*s,2*s,batter?0xff7ab6e4:0xfff5b391);
        d.line(x-6*s,hipY,x+6*s,hipY,2*s,batter?LIME:WHITE);
        if(batter) { d.text("07",x,y-28*s,7*s,WHITE,true); }
        d.oval(x,y-55*s,8*s,8*s,0xffedc294);
        if(batter) {
            d.oval(x,y-59*s,9*s,7*s,0xff234772);
            d.oval(x-3*s,y-61*s,4*s,2*s,0xff5385b1);
            d.line(x+3*s,y-53*s,x+10*s,y-50*s,1*s,0xffd1e0e2);
            d.line(x+3*s,y-50*s,x+9*s,y-47*s,1*s,0xffd1e0e2);
            d.line(x+4*s,y-55*s,x+11*s,y-55*s,3*s,INK);
            double handX=x+13*s,handY=y-33*s;
            if(swinging) {
                double a=-1.5+arm*3.5;
                handX=x+Math.cos(a)*20*s*g.shotSide;
                handY=y-36*s+Math.sin(a)*11*s;
                double tipX=handX+Math.cos(a)*29*s*g.shotSide,tipY=handY+Math.sin(a)*29*s;
                d.line(handX,handY,tipX,tipY,7*s,0xffedd09b);
            } else {
                d.line(handX+3*s,handY,handX+10*s,y-4*s,8*s,0xffb88e56);
                d.line(handX+4*s,handY+5*s,handX+10*s,y-6*s,5*s,0xfff5ddaa);
                d.line(handX+3*s,handY-4*s,handX+5*s,handY+6*s,3*s,INK);
            }
            d.line(x-4*s,shoulderY+4*s,handX,handY,4.5*s,shirt);
            d.line(x+4*s,shoulderY+4*s,handX,handY,4.5*s,shirt);
            d.oval(handX,handY,4*s,4*s,WHITE);
        } else {
            d.line(x-5*s,shoulderY+4*s,x-15*s,shoulderY+19*s+stride*4*s,4.5*s,shirt);
            d.line(x+5*s,shoulderY+4*s,x+14*s,arm>0?shoulderY-23*s*arm:shoulderY+18*s-stride*4*s,4.5*s,shirt);
        }
    }
    private void ball() {
        double x=wx(g.ballX,g.ballY),ground=py(g.ballY),height=10.8*wide+15*(1-wide);
        double y=ground-g.ballZ*height;
        double r=g.phase==Phase.DELIVERY?6+6*CricketGame.clamp(g.deliveryProgress(),0,1):7;
        d.oval(x+3,ground+3,r*.85,3.4,0x80304e32);
        if(g.phase==Phase.DELIVERY) {
            d.line(x,y-32,x,y-9,4,0x90fff1bd);
            d.oval(x,y,r+6,r+6,0x38fff1bd);
        } else if(g.phase==Phase.SHOT) for(int i=5;i>=1;i--) {
            double wx=g.ballX-g.velocityX*.016*i,wy=g.ballY-g.velocityY*.016*i;
            d.oval(wx(wx,wy),py(wy)-(g.ballZ-g.velocityZ*.016*i)*height,4-i*.5,4-i*.5,0xffe4e3a2);
        }
        d.oval(x,y,r+2,r+2,INK); d.oval(x,y,r,r,0xfffffaf0);
        d.line(x-r*.35,y+r*.65,x+r*.35,y-r*.65,2,CORAL);
        d.oval(x-r*.3,y-r*.4,2,2,0xffffffff);
    }
    private void scoreboard() {
        d.roundRect(40,123,375,104,18,0xff1e3348);
        d.text(g.practice?"PRACTICE":"THE CHASE",61,148,11,LIME,false);
        d.text(g.runs+" / "+g.wickets,60,199,43,WHITE,false);
        d.text("OVERS",250,163,10,MUTED,false);
        d.text(g.overs()+(g.practice?"":" / 2"),250,196,24,WHITE,false);
        d.roundRect(1025,123,375,104,18,0xff1e3348);
        d.text(g.practice?"UNLIMITED BALLS":g.needed()+" RUNS FROM "+g.ballsLeft()+" BALLS",1047,151,15,LIME,false);
        int start=Math.max(0,g.history.size()-6);
        for(int i=0;i<6;i++) {
            String value=start+i<g.history.size()?g.history.get(start+i):"-";
            int color=value.equals("W")?CORAL:value.equals("4")||value.equals("6")?LIME:WHITE;
            d.oval(1067+i*55,191,17,17,value.equals("-")?0xff354a5b:color);
            d.text(value,1067+i*55,197,14,value.equals("-")?MUTED:INK,true);
        }
    }
    private void controls() {
        boolean active=g.phase==Phase.DELIVERY && !g.swung;
        boolean perfectNow=active && Math.abs(g.clock-g.deliveryDuration)<=g.difficulty.perfect;
        button(40,682,420,100,"HIT LEFT","ON SIDE / MANUAL TIMING",active?0xff7ccce0:PANEL,active?INK:WHITE);
        button(980,682,420,100,"HIT RIGHT","OFF SIDE / MANUAL TIMING",active?0xff7ccce0:PANEL,active?INK:WHITE);
        String center=g.phase==Phase.READY?"BOWL":g.phase==Phase.RUNUP?"GET READY":
            g.phase==Phase.DELIVERY?(perfectNow?"HIT NOW!":"WATCH BALL"):
            g.phase==Phase.RESULT?"NEXT BALL":"IN PLAY";
        button(490,682,200,100,center,g.phase==Phase.READY?"TAP TO BOWL":"",g.phase==Phase.READY?LIME:PANEL,g.phase==Phase.READY?INK:MUTED);
        button(720,682,230,100,g.lofted?"LOFTED SHOT":"GROUND SHOT","TAP TO CHANGE",g.lofted?CORAL:PANEL,g.lofted?INK:WHITE);
        d.text("MANUAL TIMING: tap when the marker enters gold. Only PERFECT lofts can score six.",720,803,12,MUTED,true);
    }
    private void liveCaption() {
        String title,sub; int color=WHITE;
        boolean club=g.difficulty==CricketGame.Difficulty.CLUB;
        switch(g.phase) {
            case READY: title="TAP BOWL TO START"; sub="Select GROUND or LOFT before the delivery."; break;
            case RUNUP: title=g.deliveryName; sub=g.deliveryHint; break;
            case DELIVERY:
                boolean perfectNow=Math.abs(g.clock-g.deliveryDuration)<=g.difficulty.perfect;
                title=perfectNow?"PERFECT ZONE - HIT NOW!":g.deliveryName;
                sub=perfectNow?"Tap now—this is the only six-power timing.":g.deliveryHint;
                color=perfectNow?LIME:WHITE; break;
            case SHOT: title=g.timing; sub=g.detail; color=g.timingGrade==CricketGame.Timing.PERFECT?LIME:WHITE; break;
            case RETURN: title="RUNNING BETWEEN WICKETS"; sub="Running and fielding happen automatically."; break;
            case RESULT: title=g.result; sub=g.detail; color=g.lastWicket?CORAL:LIME; break;
            default: return;
        }
        d.roundRect(438,123,564,104,18,0xff1e3348);
        d.text(title,720,164,22,color,true);
        d.text(sub,720,195,12,MUTED,true);
        if(g.phase==Phase.DELIVERY || g.phase==Phase.SHOT || g.phase==Phase.RETURN || g.phase==Phase.RESULT) {
            double range=g.difficulty.window*3,barX=60,barW=335;
            d.roundRect(40,235,375,69,12,0xff152c3c);
            d.text("EARLY",60,252,9,MUTED,false);
            d.text("PERFECT",227.5,252,9,LIME,true);
            d.text("LATE",372,252,9,MUTED,false);
            d.roundRect(barX,262,barW,14,7,0xffa96559);
            d.rect(barX+barW/2-barW*g.difficulty.window*.55/range,262,barW*g.difficulty.window*1.1/range,14,0xff7ccce0);
            double gold=barW*g.difficulty.perfect/range;
            d.rect(barX+barW/2-gold,260,gold*2,18,LIME);
            double error=g.phase==Phase.DELIVERY?g.clock-g.deliveryDuration:g.timingError;
            double marker=barX+barW*CricketGame.clamp(.5+error/range,0,1);
            d.line(marker,257,marker,281,3,WHITE);
            d.text(g.phase==Phase.DELIVERY?"TAP AS THE MARKER ENTERS GOLD":g.timing+" / "+Math.abs(Math.round(error*1000))+" ms "+(error<0?"EARLY":"LATE"),227.5,296,10,WHITE,true);
        }
        if(g.phase==Phase.RESULT && (g.lastRuns>=4 || g.lastWicket)) {
            d.roundRect(618,485,204,128,20,0xee101e32);
            d.text(g.lastWicket?"W":Integer.toString(g.lastRuns),720,574,85,color,true);
            d.text(g.lastWicket?"WICKET":"BOUNDARY",720,599,12,LIME,true);
        }
    }
    private void menu() {
        d.roundRect(40,136,530,583,26,0xf510292d);
        d.roundRect(76,168,160,28,14,0xff29453d);
        d.text("THE POCKET CRICKET CLUB",156,187,9,LIME,true);
        d.text("Two overs.",72,275,58,WHITE,false);
        d.text("One chase.",72,341,58,LIME,false);
        d.text("Pick your shot. Find the gap.",76,384,18,WHITE,false);
        d.text("Every shot uses manual timing.",76,411,18,MUTED,false);
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
        d.text("MANUAL TIMING. EARN YOUR SIX.",1127,612,11,LIME,true);
        d.text("ORIGINAL ARCADE CRICKET / BATTING UPDATE",40,780,11,MUTED,false);
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
        String[][] rows={{"01","BOWL FIRST","Select a shot type, tap BOWL, then watch the delivery."},
            {"02","PICK YOUR SIDE","Behind the batter: LEFT is on / leg side. RIGHT is off side."},
            {"03","CHOOSE YOUR RISK","GROUND finds gaps. LOFT can clear the rope or get caught."},
            {"04","MANUAL INPUT","Every shot is timed. Tap in gold; only PERFECT LOFT can score six."},
            {"05","CHASE THE TARGET","Twelve balls, three wickets. Fielding and running are automatic."}};
        for(int i=0;i<rows.length;i++) {
            double y=268+i*71;
            d.text(rows[i][0],390,y+5,22,CORAL,false);
            d.text(rows[i][1],445,y,14,WHITE,false);
            d.text(rows[i][2],445,y+25,12,MUTED,false);
        }
        d.text("TAP ANYWHERE TO CLOSE",720,661,12,LIME,true);
    }
    private double depth(double y) { return 1/(1+Math.max(-6,y+16)*.027); }
    private double wx(double x,double y) { return cx+x*(17*depth(y)*(1-wide)+sx*wide); }
    private double py(double y) { return (625-(y+16)*14*depth(y))*(1-wide)+(cy+y*sy)*wide; }
    private double playerScale(double y) { return 2.48*depth(y)*(1-wide)+.78*wide; }
}
