package com.shubham.creaseclash.android;

import android.os.Bundle;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.shubham.creaseclash.gdx.CreaseApp;

public final class AndroidLauncher extends AndroidApplication {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=28) {
            WindowManager.LayoutParams attributes=getWindow().getAttributes();
            attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            getWindow().setAttributes(attributes);
        }
        AndroidApplicationConfiguration config=new AndroidApplicationConfiguration();
        config.useImmersiveMode=true; config.useAccelerometer=false; config.useCompass=false;
        config.useGyroscope=false; config.numSamples=2;
        Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        initialize(new CreaseApp(event->{
            if(vibrator==null || !vibrator.hasVibrator()) return;
            if(event.equals("wicket")) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0,45,45,80},new int[]{0,150,0,210},-1));
            } else {
                boolean boundary=event.equals("boundary");
                vibrator.vibrate(VibrationEffect.createOneShot(boundary?38:18,boundary?135:80));
            }
        }),config);
    }
}
