package com.example.hp.thetacab;

import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.hujiaweibujidao.wava.Techniques;
import com.github.hujiaweibujidao.wava.YoYo;

import java.util.ArrayList;

/**
 * Created by hp on 7/14/2016.
 */
public class Animations {
    public static void makeVisible(View view){
        view.setVisibility(View.VISIBLE);
    }

    public static void makeVisible(View... views){
        for(View view:views){
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void makeInvisible(View view){
        view.setVisibility(View.GONE);
    }

    public static void makeInvisible(View... views){
        for(View view:views){
            view.setVisibility(View.INVISIBLE);
        }
    }

    public static void remove(View view){
        view.setVisibility(View.GONE);
    }

    public static void remove(View... views){
        for(View view:views){
            view.setVisibility(View.GONE);
        }
    }

    public static void playYoYoAnimOnMultipleViews(Techniques technique,int duration, View... views){
        for(View view:views){
            YoYo.Builder builder = YoYo.with(technique).duration(duration).interpolate(new AccelerateDecelerateInterpolator());
            builder.playOn(view);
        }
    }

    public static void makeVisibleAfterDelay(int delay, final View... views){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(View view:views){
                    view.setVisibility(View.VISIBLE);
                }
            }
        },delay);
    }


    public static void makeInvisibleAfterDelay(int delay, final View... views){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(View view:views){
                    view.setVisibility(View.INVISIBLE);
                }
            }
        },delay);
    }

    public static void removeAfterDelay(int delay, final View... views){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(View view:views){
                    view.setVisibility(View.GONE);
                }
            }
        },delay);
    }
}
