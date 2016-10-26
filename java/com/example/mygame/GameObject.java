package com.example.mygame;


import android.graphics.Rect;

public abstract class GameObject {
    protected int x;
    protected int y;
    protected int dx;
    protected int dy;
    protected int height;
    protected int width;
    public void setX(int x){
        this.x = x;
    }
    public void setY(int y){
        this.y=y;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public void setHeight(int height){
        this.height= height;
    }
    public void setWidth(int width){
        this.width = width;
    }
    public Rect getRect(){
        Rect rect = new Rect(this.x, this.y, this.width+this.x, this.height+this.y);
        return rect;
    }
}
