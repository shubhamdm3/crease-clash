package com.shubham.creaseclash.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.shubham.creaseclash.*;
import java.util.HashMap;
import java.util.Map;

public final class CreaseApp extends ApplicationAdapter {
    public interface Haptics { void pulse(String event); }
    private final Haptics haptics;
    private GameSession session;
    private GdxDraw draw;
    private final GameRenderer renderer=new GameRenderer();
    private final OrthographicCamera camera=new OrthographicCamera();
    private final FitViewport viewport=new FitViewport(1440,810,camera);
    private final Map<String,Sound> sounds=new HashMap<>();
    private final Vector2 touch=new Vector2();
    private boolean timingCueSent;
    public CreaseApp(Haptics haptics) { this.haptics=haptics; }
    @Override public void create() {
        draw=new GdxDraw();
        final Preferences preferences=Gdx.app.getPreferences("crease-clash");
        for(String event:new String[]{"hit","bounce","wicket","boundary","release","run","win","end"}) {
            sounds.put(event,Gdx.audio.newSound(Gdx.files.internal("audio/"+event+".wav")));
        }
        session=new GameSession(System.nanoTime(),new GameSession.Platform() {
            public int readInt(String key,int fallback) { return preferences.getInteger(key,fallback); }
            public void writeInt(String key,int value) { preferences.putInteger(key,value); preferences.flush(); }
            public void sound(String event) { Sound sound=sounds.get(event); if(sound!=null) sound.play(event.equals("release")?.2f:.6f); }
            public void event(String event) { Gdx.app.log("CreaseClash", "event="+event+" side="+session.game.shotSide+" runs="+session.game.runs+" balls="+session.game.balls+" grade="+session.game.timingGrade+" sixEligible="+session.game.sixEligible+" lastRuns="+session.game.lastRuns); }
            public void vibrate(String event) { haptics.pulse(event); }
        });
        Gdx.input.setCatchKey(Input.Keys.BACK,true);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int x,int y,int pointer,int button) {
                viewport.unproject(touch.set(x,y));
                double virtualY=810-touch.y;
                if(touch.x<0 || touch.x>1440 || virtualY<0 || virtualY>810) return false;
                session.tap(touch.x,virtualY); return true;
            }
            @Override public boolean keyDown(int key) {
                if(key==Input.Keys.BACK || key==Input.Keys.ESCAPE) { session.back(); return true; }
                return false;
            }
        });
    }
    @Override public void resize(int width,int height) { viewport.update(width,height,true); }
    @Override public void render() {
        // Clear the full surface, including letterbox margins on tall or wide phones.
        Gdx.gl.glViewport(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        Gdx.gl.glClearColor(.035f,.08f,.09f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(); camera.update(); session.update(Gdx.graphics.getDeltaTime());
        if(session.game.phase!=CricketGame.Phase.DELIVERY) timingCueSent=false;
        else if(!timingCueSent && session.game.clock>=session.game.deliveryDuration-.11) {
            timingCueSent=true;
            Gdx.app.log("CreaseClash","event=timing-cue");
        }
        draw.begin(camera); renderer.render(draw,session); draw.end();
    }
    @Override public void pause() { if(session!=null) session.background(); }
    @Override public void resume() { /* Explicit resume button prevents an unseen delivery. */ }
    @Override public void dispose() { if(draw!=null) draw.dispose(); for(Sound s:sounds.values()) s.dispose(); }
}
