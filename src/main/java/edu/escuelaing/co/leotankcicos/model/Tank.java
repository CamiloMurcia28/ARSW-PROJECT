package edu.escuelaing.co.leotankcicos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tanks")
public class Tank {
    
    @Id
    private String name;
    private String color;
    private boolean alive;
    private int posx;
    private int posy;
    private int rotation;

    public Tank() {}

    public Tank(int x, int y, String color, int rotation, String name) {
        this.posx = x;
        this.posy = y;
        this.color = color;
        this.rotation = rotation;
        this.name = name;
        alive = true;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getPosx() {
        return posx;
    }
    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }
    public void setPosy(int posy) {
        this.posy = posy;
    }
    public int getRotation() { 
        return rotation; 
    }

    public void setRotation(int i) {
        this.rotation = i;
    }
}