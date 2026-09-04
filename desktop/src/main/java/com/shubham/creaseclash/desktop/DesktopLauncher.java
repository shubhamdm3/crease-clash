package com.shubham.creaseclash.desktop;

import com.shubham.creaseclash.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/** Playable Java desktop harness; renders the same scene and runs the same rules as Android. */
public final class DesktopLauncher extends JPanel {
    private final GameSession session;
    private final GameRenderer renderer=new GameRenderer();
    private long previous=System.nanoTime();
    private double scale=1,offsetX,offsetY;
    public DesktopLauncher() {
        DesktopPlatform platform=new DesktopPlatform();
        session=new GameSession(System.nanoTime(),platform);
        setPreferredSize(new Dimension(1280,720)); setFocusable(true); setBackground(Color.BLACK);
        addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) {
            session.tap((e.getX()-offsetX)/scale,(e.getY()-offsetY)/scale); requestFocusInWindow();
        }});
        addKeyListener(new KeyAdapter() {
            private final java.util.Set<Integer> held=new java.util.HashSet<>();
            public void keyPressed(KeyEvent e) {
                if(!held.add(e.getKeyCode())) return;
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: case KeyEvent.VK_A: session.game.swing(-1); break;
                    case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: session.game.swing(1); break;
                    case KeyEvent.VK_SPACE:
                        if(session.game.phase==CricketGame.Phase.MENU) session.start(false); else session.game.bowl(); break;
                    case KeyEvent.VK_L: session.game.toggleLoft(); break;
                    case KeyEvent.VK_ESCAPE: case KeyEvent.VK_P: session.back(); break;
                    default: break;
                }
            }
            public void keyReleased(KeyEvent e) { held.remove(e.getKeyCode()); }
        });
        addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { session.background(); } });
        new Timer(8,e->{
            long now=System.nanoTime(); session.update((now-previous)/1e9); previous=now; repaint();
        }).start();
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        scale=Math.min(getWidth()/1440.0,getHeight()/810.0);
        offsetX=(getWidth()-1440*scale)/2; offsetY=(getHeight()-810*scale)/2;
        Graphics2D g=(Graphics2D)graphics.create(); g.translate(offsetX,offsetY); g.scale(scale,scale);
        renderer.render(new AwtDraw(g),session); g.dispose();
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].equals("--smoke")) { smoke(args.length>1?args[1]:"build/screenshots"); return; }
        if(GraphicsEnvironment.isHeadless()) { System.err.println("No desktop display. Use --smoke for rendered screenshots."); return; }
        SwingUtilities.invokeLater(()->{
            JFrame frame=new JFrame("Crease Clash | A/D: shot, Space: bowl, L: loft, Esc: pause");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); frame.add(new DesktopLauncher());
            frame.pack(); frame.setMinimumSize(new Dimension(960,540)); frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
    public static GameSession quietSession(long seed) {
        return new GameSession(seed,new GameSession.Platform() {
            private final Map<String,Integer> values=new HashMap<>();
            public int readInt(String k,int f) { return values.getOrDefault(k,f); }
            public void writeInt(String k,int v) { values.put(k,v); }
            public void sound(String event) { }
            public void vibrate(String event) { }
        });
    }
    private static void image(GameSession s,Path path,int w,int h) throws Exception {
        BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics(); g.setColor(Color.BLACK); g.fillRect(0,0,w,h);
        double scale=Math.min(w/1440.0,h/810.0); g.translate((w-1440*scale)/2,(h-810*scale)/2); g.scale(scale,scale);
        new GameRenderer().render(new AwtDraw(g),s); g.dispose(); ImageIO.write(image,"png",path.toFile());
    }
    private static void smoke(String directory) throws Exception {
        Path dir=Path.of(directory); Files.createDirectories(dir);
        GameSession s=quietSession(103);
        image(s,dir.resolve("01-menu.png"),1440,810);
        s.start(false); image(s,dir.resolve("02-ready.png"),1440,810);
        s.game.bowl(); while(s.game.phase!=CricketGame.Phase.DELIVERY) s.update(CricketGame.STEP);
        while(s.game.clock<s.game.deliveryDuration-.20) s.update(CricketGame.STEP);
        image(s,dir.resolve("03-delivery.png"),1440,810);
        s.game.lofted=true;
        while(s.game.clock<s.game.deliveryDuration) s.update(CricketGame.STEP);
        s.game.swing(s.game.deliveryLine<0?-1:1);
        for(int i=0;i<54;i++) s.update(CricketGame.STEP);
        image(s,dir.resolve("04-shot.png"),1440,810);
        while(s.game.phase!=CricketGame.Phase.RESULT) s.update(CricketGame.STEP);
        image(s,dir.resolve("05-result.png"),1440,810);
        image(s,dir.resolve("06-wide-20x9.png"),1800,810);
        while(s.game.phase==CricketGame.Phase.RESULT) s.update(CricketGame.STEP);
        s.game.setPaused(true); image(s,dir.resolve("07-pause.png"),1440,810);
        s.game.menu(); s.help=true; image(s,dir.resolve("08-help.png"),1440,810);
        s.help=false; s.start(false);
        int guard=0;
        while(s.game.phase!=CricketGame.Phase.MATCH_OVER && guard++<40000) {
            if(s.game.phase==CricketGame.Phase.READY) s.game.bowl();
            s.update(CricketGame.STEP);
        }
        if(guard>=40000) throw new AssertionError("Match did not end");
        image(s,dir.resolve("09-match-over.png"),1440,810);
        System.out.println("Rendered 9 shared-renderer screenshots to "+dir.toAbsolutePath());
    }
    private static final class DesktopPlatform implements GameSession.Platform {
        private final Preferences prefs=Preferences.userRoot().node("com/shubham/creaseclash");
        private final Map<String,Clip> clips=new HashMap<>();
        DesktopPlatform() {
            for(String name:new String[]{"hit","bounce","boundary","wicket","win","release","run","end"}) {
                try {
                    var stream=getClass().getResourceAsStream("/audio/"+name+".wav");
                    if(stream==null) continue;
                    try(var audio=AudioSystem.getAudioInputStream(new BufferedInputStream(stream))) {
                        Clip clip=AudioSystem.getClip(); clip.open(audio); clips.put(name,clip);
                    }
                } catch(Exception ignored) { /* A missing audio device does not stop play. */ }
            }
        }
        public int readInt(String k,int f) { return prefs.getInt(k,f); }
        public void writeInt(String k,int v) { prefs.putInt(k,v); }
        public void sound(String event) { Clip c=clips.get(event); if(c!=null) { c.stop(); c.setFramePosition(0); c.start(); } }
        public void vibrate(String event) { /* Desktop devices do not expose Android haptics. */ }
    }
}
