package edu.escuelaing.co.leotankcicos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "boards")
public class Board {
    @Id
    private String id;
    private String[][] boxes;

    @Transient
    private final Object[][] locks;

    public Board() {
        initializeBoard();
        this.locks = new Object[boxes.length][boxes[0].length];
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes[0].length; j++) {
                locks[i][j] = new Object();
            }
        }
    }

    public void initializeBoard(){
        this.boxes = new String[][]{
            {"1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1"},
            {"1", "0", "0", "0", "0", "0", "0", "1", "0", "0", "0", "0", "0", "0", "1"},
            {"1", "0", "1", "1", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "1"},
            {"1", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "1", "0", "0", "1"},
            {"1", "0", "0", "0", "0", "0", "0", "1", "0", "0", "0", "0", "0", "0", "1"},
            {"1", "0", "0", "0", "1", "0", "0", "1", "0", "0", "0", "1", "0", "0", "1"},
            {"1", "0", "0", "0", "1", "0", "0", "1", "0", "0", "0", "0", "1", "0", "1"},
            {"1", "0", "0", "0", "0", "0", "0", "0", "0", "1", "1", "0", "0", "0", "1"},
            {"1", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "1"},
            {"1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1"}
        };
    }
    
    public String getId(){
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }
    
    public String getValue(int x, int y) {
        return boxes[y][x];
    }

    public void putTank(String username, int x, int y) {
        boxes[y][x] = username;
    }

    public void clearBox(int x, int y) {
        boxes[y][x] = "0";
    }

    public String[][] getBoxes() {
        return boxes;
    }

    public void clearBoard() {
        initializeBoard();
    }

    public Object getLock(int x, int y) {
        return locks[y][x];
    }
}
