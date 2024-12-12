package edu.escuelaing.co.leotankcicos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.escuelaing.co.exception.BoxOccupiedException;
import edu.escuelaing.co.exception.TankPositionException;
import edu.escuelaing.co.leotankcicos.model.Bullet;
import edu.escuelaing.co.leotankcicos.model.Tank;
import edu.escuelaing.co.leotankcicos.service.TankService;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = {"https://frontarsw.z22.web.core.windows.net", "https://leotanksload.duckdns.org"})
public class TankController {

    private final TankService tankService;
    Tank updatedTank;
    private static final String USERNAME_STRING = "username";

    @Autowired
    public TankController(TankService tankService) {
        this.tankService = tankService;
    }

    @GetMapping("/")
    public ResponseEntity<Void> ok(){
        return ResponseEntity.ok().build();
    }

    //Crea los tanques
    @PostMapping("/api/tanks/loginTank")
    public ResponseEntity<Tank> createTank(@RequestBody Map<String, Object> request,  HttpSession session) {
        try {
            String username = (String) request.get(USERNAME_STRING);
            session.setAttribute(USERNAME_STRING, username);
            String receivedHash = (String) request.get("hash");

            Tank tank = tankService.saveTank(username, receivedHash);
            return ResponseEntity.ok(tank);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }

    }

    //Obtiene todos los tanques
    @GetMapping("/api/tanks")
    public ResponseEntity<List<Tank>> getAllTanks() {
        List<Tank> tanks = tankService.getAllTanks();
        return new ResponseEntity<>(tanks, HttpStatus.OK);
    }

    // Ruta para obtener el nombre de usuario
    @GetMapping("/api/tanks/username")
    public ResponseEntity<String> getUsername(HttpSession session) {
        String username = (String) session.getAttribute(USERNAME_STRING);
        if (username != null) {
            return new ResponseEntity<>(username, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Username not found", HttpStatus.NOT_FOUND);
        }
    }

    // Mover tanque 
    @MessageMapping("/{username}/move")
    public void moveTank(@DestinationVariable String username, @RequestBody Map<String, Integer> moveRequest) throws TankPositionException, BoxOccupiedException{
        Integer posX = moveRequest.get("posX");
        Integer posY = moveRequest.get("posY");
        Integer newPosX = moveRequest.get("newPosX");
        Integer newPosY = moveRequest.get("newPosY");
        Integer rotation = moveRequest.get("rotation");
            updatedTank = tankService.updateTankPosition(username, posX, posY, newPosX, newPosY, rotation);
    }

    // Obtener un tanque específico
    @GetMapping("/api/tanks/{username}")
    public ResponseEntity<Tank> getTank(@PathVariable String username) {
        Tank tank = tankService.getTankById(username);
        if (tank == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tank, HttpStatus.OK);
    }

    @GetMapping("/api/tanks/board")
    public ResponseEntity<String[][]> getBoard() {
        String[][] board = tankService.getBoardBoxes();
        if (board == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(board, HttpStatus.OK);
    }

    @GetMapping("/api/tanks/bullets/{bulletId}/position")
    public ResponseEntity<Bullet> getBulletPosition(@PathVariable String bulletId) {
        Bullet bullet = tankService.getBulletPosition(bulletId);
        if (bullet == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bullet, HttpStatus.OK);
    }

    @MessageMapping("/{username}/shoot")
    public void handleShootEvent(@DestinationVariable String username, @RequestBody String bulletId) {
        tankService.shoot(username, bulletId);
    }

    @MessageMapping("/matches/1/winner")
    public void handleWinnerEvent() {
        tankService.handleWinner();
    }

    @GetMapping("/api/tanks/matches/1/reset")
    public ResponseEntity<String> resetGame() {
        tankService.reset();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

}
