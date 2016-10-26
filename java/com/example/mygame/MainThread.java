package com.example.mygame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.sql.Time;


/**
 * Created by ПОДАРУНКОВИЙ on 15.10.2016.
 */
public class MainThread extends Thread {
    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }
    @Override
    public void run(){
        long starTime;
        long timeMilis;
        long waitTime;
        long targetTime = 1000/FPS;
        long totalTime=0;
        int frameCount=0;

        while (running){
            starTime = System.nanoTime();
            canvas = null;
            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            }catch (Exception e){}
            finally {
                if (canvas!=null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch (Exception e){e.printStackTrace();}
                }
            }
            timeMilis = (System.nanoTime()-starTime)/1000000;
            waitTime = targetTime - timeMilis;
            try{
                this.sleep(waitTime);
            }catch (Exception e){}
             totalTime += System.nanoTime()-starTime;
            frameCount++;
            if (frameCount==FPS){
                averageFPS = (1000/((totalTime/frameCount)/1000000));
                frameCount=0;
                totalTime=0;
                System.out.println(averageFPS);
            }
        }
    }
    public void setRunning(boolean b){
        running=b;
    }
}
