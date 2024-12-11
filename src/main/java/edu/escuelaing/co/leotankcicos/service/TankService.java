package edu.escuelaing.co.leotankcicos.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import edu.escuelaing.co.leotankcicos.model.Board;
import edu.escuelaing.co.leotankcicos.model.Bullet;
import edu.escuelaing.co.leotankcicos.model.Tank;
import edu.escuelaing.co.leotankcicos.repository.BoardRepository;
import edu.escuelaing.co.leotankcicos.repository.BulletRepository;
import edu.escuelaing.co.leotankcicos.repository.TankRepository;

@Service
public class TankService {

    SimpMessagingTemplate msgt;
    private Queue<int[]> defaultPositions = new LinkedList<>();
    private Queue<String> defaultColors = new LinkedList<>();
    private final Object bulletLock = new Object();
    private static final int MAX_PLAYERS = 3;


    
    private TankRepository tankRepository;
    private BulletRepository bulletRepository;
    private BoardRepository boardRepository;
    private Board board;

    @Autowired
    public TankService(BoardRepository boardRepository, SimpMessagingTemplate msgt, TankRepository tankRepository, BulletRepository bulletRepository) {
        this.boardRepository = boardRepository;
        this.board = boardRepository.findAll().stream()
            .findFirst()
            .orElse(new Board());
        this.msgt = msgt;
        this.tankRepository = tankRepository;
        this.bulletRepository = bulletRepository;
        initialConfig();
    }

    private void initialConfig() {
        defaultPositions.add(new int[]{1, 8});
        defaultPositions.add(new int[]{13, 8});
        defaultPositions.add(new int[]{13, 1});
        defaultPositions.add(new int[]{1, 1});

        defaultColors.add("#fa0a0a");
        defaultColors.add("#001ba1");
        defaultColors.add("#f1c40f");
        defaultColors.add("#0c7036");
    }

    private void saveOrUpdateBoard(){
        boardRepository.save(board);
    }

    public synchronized Tank saveTank(String name) throws Exception {
        if (tankRepository.count() >= MAX_PLAYERS) {
            throw new Exception("The room is full");
        }
        if (tankRepository.findById(name).isPresent() || name.equals("1") || name.equals("0")) {
            throw new Exception("Tank with this name already exists or is invalid");
        }
        int[] position = defaultPositions.poll();
        Tank newTank = new Tank(position[0], position[1], defaultColors.poll(), 0, name);
        board.putTank(name, position[0], position[1]);
        saveOrUpdateBoard();
        tankRepository.save(newTank);
        return newTank;
    }

    public List<Tank> getAllTanks() {
        return tankRepository.findAll();
    }

    public Tank getTankById(String username) {
        return tankRepository.findById(username).orElse(null);
    }

    public Tank updateTankPosition(String username, int x, int y, int newX, int newY, int rotation) throws Exception {
        Tank tank = tankRepository.findById(username).orElse(null);
        if (tank == null) {
            return null;
        }
        String[][] boxes = board.getBoxes();
        int firstX;
        int firstY; 
        int secondX; 
        int secondY;
        if (y * boxes[0].length + x <= newY * boxes[0].length + newX) {
            firstX = x;
            firstY = y;
            secondX = newX;
            secondY = newY;
        } else {
            firstX = newX;
            firstY = newY;
            secondX = x;
            secondY = y;
        }

        synchronized (board.getLock(firstX, firstY)) {
            synchronized (board.getLock(secondX, secondY)) {
                boxes = board.getBoxes();
                if (!boxes[y][x].equals(username)) {
                    throw new Exception("Tank is no longer in the original position");
                }

                String box = boxes[newY][newX];
                if (!box.equals("0")) {
                    System.out.println("This box is already occupied by: " + box);
                    throw new Exception("This box is already occupied by: " + box);
                }

                board.clearBox(x, y);
                board.putTank(tank.getName(), newX, newY);
                saveOrUpdateBoard();
                tank.setPosx(newX);
                tank.setPosy(newY);
                tank.setRotation(rotation);
                tankRepository.save(tank);
            }
        }
        msgt.convertAndSend("/topic/matches/1/movement", tank);
        printBoard(getBoardBoxes());
        return tank;
    }

    public void printBoard(String[][] boxes) {
        for (String[] row : boxes) {
            for (String cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    public Bullet shoot(String username, String bulletId) {
        Tank tank = tankRepository.findById(username).orElse(null);
        if (tank == null) {
            return null;
        }

        Bullet bullet;
        synchronized (bulletLock) {
            bullet = new Bullet(
                    bulletId,
                    tank.getPosx(),
                    tank.getPosy(),
                    tank.getRotation(),
                    true,
                    username
            );
            bulletRepository.save(bullet);
        }
        startBulletMovement(bullet);
        return bullet;
    }

    public Bullet getBulletPosition(String bulletId) {
        return bulletRepository.findById(bulletId).orElse(null);
    }

    private void startBulletMovement(Bullet bullet) {
        new Thread(() -> moveBullet(bullet)).start();
    }

    private void moveBullet(Bullet bullet) {
        while (bullet.isAlive()) {
            int newX = bullet.getX();
            int newY = bullet.getY();

            switch (bullet.getDirection()) {
                case -90 -> newY = bullet.getY() - 1;  
                case 0 -> newX = bullet.getX() + 1;   
                case 90 -> newY = bullet.getY() + 1;  
                case 180 -> newX = bullet.getX() - 1;  
            }
    
            if (isOutOfBounds(newX, newY)) {
                bullet.setAlive(false);
                bulletRepository.deleteById(bullet.getId());
                break;
            }

            bullet.setX(newX);
            bullet.setY(newY);
    
            String[][] boxes = board.getBoxes();
            String boxContent = boxes[newY][newX];
    
            if (!boxContent.equals("0") && !boxContent.equals("1")) {
                Optional<Tank> collidedTank = tankRepository.findById(boxContent);
                
                if (collidedTank.isPresent() && !collidedTank.get().getName().equals(bullet.getTankId())) {
                    handleCollision(bullet, collidedTank.get());
                    bullet.setAlive(false);
                    bulletRepository.deleteById(bullet.getId());
                    break;
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private boolean isOutOfBounds(int x, int y) {
        String[][] boxes = board.getBoxes();
        return x < 0 || x >= boxes[0].length || y < 0 || y >= boxes.length || boxes[y][x].equals("1");
    }

    private void handleCollision(Bullet bullet, Tank tank) {
        tankRepository.deleteById(tank.getName());
        board.clearBox(tank.getPosx(), tank.getPosy());
        saveOrUpdateBoard();
        CompletableFuture.runAsync(() -> {
            Map<String, String> response = new HashMap<>();
            response.put("tank", tank.getName());
            response.put("x", String.valueOf(tank.getPosx()));
            response.put("y", String.valueOf(tank.getPosy()));
            response.put("bulletId", bullet.getId());
            
            msgt.convertAndSend("/topic/matches/1/collisionResult", response);
            System.out.println("¡Colisión! Tanque " + tank.getName() + " ha sido golpeado");
        });
        

        Tank winner = checkVictory();
        if (winner != null) {
            announceVictory(winner);
        }
    }

    public synchronized void handleWinner() {
        Tank winner = checkVictory();
        if (winner != null) {
            announceVictory(winner);
        }
    }

    public String[][] getBoardBoxes() {
        return board.getBoxes();
    }

    private Tank checkVictory() {
        List<Tank> tanks = tankRepository.findAll();
        if (tanks.size() == 1) {
            return tanks.get(0);
        }
        return null;
    }

    private void announceVictory(Tank winner) {
        System.out.println("¡El ganador es: " + winner.getName() + "!");
        tankRepository.deleteAll();
        board.clearBoard();
        saveOrUpdateBoard();
        msgt.convertAndSend("/topic/matches/1/winner", winner);
    }

    public void reset() {
        tankRepository.deleteAll();
        bulletRepository.deleteAll();
        board.clearBoard();
        saveOrUpdateBoard();
        initialConfig();
    }
}
