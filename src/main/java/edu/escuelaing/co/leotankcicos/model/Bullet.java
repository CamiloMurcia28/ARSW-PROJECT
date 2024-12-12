package edu.escuelaing.co.leotankcicos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bullets")
public class Bullet {

    @Id
    private String id;
    private int x;
    private int y;
    private int direction;
    private boolean alive;
    private String tankId;

    public Bullet(String id, int x, int y, int direction, String tankId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.alive = alive;
        this.tankId = tankId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getTankId() {
        return tankId;
    }

    public void setTankId(String tankId) {
        this.tankId = tankId;
    }
}
