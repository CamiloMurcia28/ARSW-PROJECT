package edu.escuelaing.co.leotankcicos.service;

import java.util.*;

import edu.escuelaing.co.leotankcicos.model.Bullet;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.escuelaing.co.leotankcicos.model.Tank;
import edu.escuelaing.co.leotankcicos.repository.TankRepository;

@Service
public class TankService {

    private Queue<int[]> defaultPositions = new LinkedList<>();
    private Queue<String> defaultColors = new LinkedList<>();

    private TankRepository tankRepository;
    private Map<Integer, Bullet> bullets;
    private int bulletId;
    private Board board;

    private static final int MAX_PLAYERS = 3;

    @Autowired
    public TankService(TankRepository tankRepository, Board board){
        this.tankRepository = tankRepository;
        this.board = board;
        initialConfig();
    }

    private void initialConfig(){
        
        defaultPositions.add(new int[]{1,8});
        defaultPositions.add(new int[]{13,8});
        defaultPositions.add(new int[]{13,1});
        defaultPositions.add(new int[]{1,1});

        defaultColors.add("#a569bd");
        defaultColors.add("#f1948a");
        defaultColors.add("#f1c40f");
        defaultColors.add("#1e8449");
    }

    public synchronized Tank saveTank(String name) throws Exception {

        if(tankRepository.count() >= MAX_PLAYERS){
            throw new Exception("The room is full");
        }
        if(tankRepository.findById(name).isPresent() || name.equals("1") || name.equals("0")){
            throw new Exception("Tank with this name already exists or is invalid");
        }
        int[] position = defaultPositions.poll();
        Tank newTank = new Tank(position[0], position[1], defaultColors.poll(), 0, name);
        board.putTank(name, position[0], position[1]);
        tankRepository.save(newTank);
        return newTank;
    }

    public List<Tank> getAllTanks() {
        return new ArrayList<>(tankRepository.findAll());
    }

    public Tank getTankById(String username) {
        Tank tank = null;
        if(tankRepository.findById(username).isPresent()){
            tank = tankRepository.findById(username).get(); 
        }
        return tank;
    }

    public Tank updateTankPosition(Tank tank, int x, int y, int newX, int newY) throws Exception { 
        String[][] boxes = board.getBoxes();
        String box = boxes[newY][newX];
        synchronized(box){
            if(box.equals("0")){
                System.out.println("initialx" + tank.getPosx() + "initialy" + tank.getPosy());
                board.putTank(tank.getName(), newX, newY);
                board.clearBox(y, x);
                tank.setPosx(newX);
                tank.setPosy(newY);
                tankRepository.save(tank);
                boxes = board.getBoxes();
                printBoard(boxes);
                System.out.println("siiiiiiiiiiiiiiiiiiix" + tank.getPosx() + "aaay" + tank.getPosy());
            }else{
                System.out.println("This box is already occupied by:" + box);
                throw new Exception("This box is already occupied" + box);
            }
        }
        
        return tank;
    }
    public void printBoard(String [][] boxes) {
        for (String[] row : boxes) {
            for (String cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    public Bullet shoot(String username) {
        Tank tank = tankRepository.findById(username).get();
        if (tank == null) {
            return null;
        }
        Bullet bullet = new Bullet(
                bulletId++,
                tank.getPosx(),
                tank.getPosy(),
                tank.getRotation(),
                true,
                username
        );
        bullets.put(bullet.getId(), bullet);
        startBulletMovement(bullet);
        return bullet;
    }

    public Bullet getBulletPosition(int bulletId) {
        return bullets.get(bulletId);
    }

    private void startBulletMovement(Bullet bullet) {
        new Thread(() -> moveBullet(bullet)).start();
    }

    private void moveBullet(Bullet bullet) {
        while (bullet.isAlive()) {
            // Actualizar la posición de la bala según su rotación
            int newX = bullet.getX();
            int newY = bullet.getY();

            switch (bullet.getDirection()) {
                case -90: // Arriba
                    newY = bullet.getY() - 1;
                    break;
                case 0: // Derecha
                    newX = bullet.getX() + 1;
                    break;
                case 90: // Abajo
                    newY = bullet.getY() + 1;
                    break;
                case 180: // Izquierda
                    newX = bullet.getX() - 1;
                    break;
            }

            // Verificar si la bala está dentro de los límites del tablero
            if (isOutOfBounds(newX, newY)) {
                bullet.setAlive(false);
                break;
            }

            // Actualizar posición de la bala
            bullet.setX(newX);
            bullet.setY(newY);

            // Obtener todos los tanques de la base de datos
            List<Tank> tanks = tankRepository.findAll();

            // Comprobar colisiones con los tanques
            boolean collision = false;
            for (Tank tank : tanks) {
                if (!tank.getName().equals(bullet.getTankId())) { // No golpear al propio tanque
                    if (checkCollision(bullet, tank)) {
                        collision = true;
                        handleCollision(bullet, tank);
                        break;
                    }
                }
            }

            // Si hubo colisión, terminar el movimiento de la bala
            if (collision) {
                bullet.setAlive(false);
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private boolean isOutOfBounds(int x, int y) {
        String[][] boxes = board.getBoxes();
        return x < 0 || x >= boxes[0].length || y < 0 || y >= boxes.length;
    }

    private boolean checkCollision(Bullet bullet, Tank tank) {
        // Comprobar si la bala está lo suficientemente cerca del tanque
        return Math.abs(tank.getPosx() - bullet.getX()) < 1 &&
                Math.abs(tank.getPosy() - bullet.getY()) < 1;
    }

    private void handleCollision(Bullet bullet, Tank tank) {
        // Aquí puedes implementar la lógica de lo que sucede cuando una bala golpea un tanque
        // Por ejemplo, reducir la vida del tanque, eliminarlo, etc.

        // Actualizar el estado del tanque en la base de datos
        tankRepository.save(tank);

        // Actualizar el tablero
        board.clearBox(tank.getPosy(), tank.getPosx());

        System.out.println("¡Colisión! Tanque " + tank.getName() + " ha sido golpeado");
    }

    // Método auxiliar para actualizar el tablero después de mover la bala
    private void updateBoard(Bullet bullet) {
        String[][] boxes = board.getBoxes();
        // Limpiar la posición anterior de la bala
        board.clearBox(bullet.getY(), bullet.getX());
        // Colocar la bala en su nueva posición
        boxes[bullet.getY()][bullet.getY()] = "B"; // "B" para representar una bala
    }

    public String[][] getBoard() {
        return board.getBoxes();
    }
}