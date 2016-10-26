package com.example.mygame;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 642;
    public static final int HEIGHT = 360;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private long resetTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private Explosion explosion;
    private boolean explose=false;
    private int best;
    private boolean drawInstruction=true;

    private ArrayList<Missile> missiles;
    private Random rand = new Random();


    public GamePanel(Context context)
    {
        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);



        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread=null;
            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(),-130, 100, 100, 25);
        missiles = new ArrayList<Missile>();
        smokeStartTime=  System.nanoTime();
        missileStartTime = System.nanoTime();
        best=Integer.parseInt(openFile("savefile.txt", getContext()));

        thread = new MainThread(getHolder(), this);

        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    { if(!explose) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying()) {
                player.setPlaying(true);
            } else {
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
    }
        return super.onTouchEvent(event);

    }

    public void update()

    {




        if(player.getPlaying()) {
               drawInstruction=false;
            bg.update();
            player.update();
            if (player.getScore()>best)best=player.getScore();

            //add missiles on timer
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            if(missileElapsed >(2000 - player.getScore()/4)){

                System.out.println("making missile");
                //first missile always goes down the middle
                if(missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }
                else
                {

                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT)),45,15, player.getScore(),13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for(int i = 0; i<missiles.size();i++)
            {
                //update missile
                missiles.get(i).update();

                if(collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    resetTime = System.nanoTime();
                    player.setPlaying(false);

                    explosion.setY(player.getY()-30);
                    explose=true;
                    save("savefile.txt", getContext(), best);
                    player.setPlaying(false);



                    break;
                }
                //remove missile if it is way off the screen
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }



            //add smoke puffs on timer

        }
        else{
            if (explose) {
                explosion.update();
            }
            long resetElapsed = (System.nanoTime()-resetTime)/1000000;

            if(resetElapsed > 2500)
            {
                newGame();

            }



        }
    }

    private void newGame() {
        explose=false;
        explosion.setNewAnimation();
        explosion.setY(-130);
        missiles.clear();
        player.resetScore();
        resetTime=System.nanoTime();
       //
        player.setY(HEIGHT/2);

        drawInstruction=true;
    }

    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRect(),b.getRect()))
        {
            return true;
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();



            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);
                explosion.draw(canvas);

            //draw smokepuffs

            //draw missiles
            for(Missile m: missiles)
            {
                m.draw(canvas);
            }
             drawText(canvas);





            canvas.restoreToCount(savedState);
        }
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()*3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + Integer.parseInt(openFile("savefile.txt", getContext()))*3, WIDTH - 215, HEIGHT - 10, paint);

        if (drawInstruction){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
    }

    public static void save(String filename,
                            Context ctx, int best) {
  String bbest = String.valueOf(best);
        try {
            OutputStream outputStream = ctx.openFileOutput(filename, 0);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            osw.write(bbest);
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private String openFile(String fileName, Context ctx) {
        String bbest="1";
        try {
            InputStream inputStream = ctx.openFileInput(fileName);

            if (inputStream != null) {

                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;


                line = reader.readLine();


                bbest=line;
                inputStream.close();
            }
        } catch (Throwable t) {

        }
        return bbest;
    }
}