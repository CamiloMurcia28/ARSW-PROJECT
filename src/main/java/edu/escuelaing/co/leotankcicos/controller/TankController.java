package edu.escuelaing.co.leotankcicos.controller;

import java.util.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.escuelaing.co.leotankcicos.model.Tank;
import edu.escuelaing.co.leotankcicos.service.TankService;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tanks")
public class TankController {

    private final TankService tankService;

    @Autowired
    public TankController(TankService tankService){
        this.tankService = tankService;
    }

    //Crea los tanques
    @PostMapping("/login")
    public ResponseEntity<Tank> createTank(@RequestParam String username, HttpSession session) {
        session.setAttribute("username", username);
        Tank newTank = tankService.saveTank(username);
        return new ResponseEntity<>(newTank, HttpStatus.CREATED);
    }

    //Obtiene todos los tanques
    @GetMapping
    public ResponseEntity<List<Tank>> getAllTanks() {
        List<Tank> tanks = tankService.getAllTanks();
        return new ResponseEntity<>(tanks, HttpStatus.OK);
    }

    // Ruta para obtener el nombre de usuario
    @GetMapping("/username")  
    public ResponseEntity<String> getUsername(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            return new ResponseEntity<>(username, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Username not found", HttpStatus.NOT_FOUND);
        }
    }

    // Mover tanque 
    @PutMapping("/{id}/move")  
    public ResponseEntity<Tank> moveTank(@PathVariable String username, @RequestBody Map<String, Integer> moveRequest) {
        Tank tank = tankService.getTankById(username);
        if (tank != null) {
            Integer posX = moveRequest.get("posX");
            Integer posY = moveRequest.get("posY");
            Tank updatedTank =  tankService.updateTankPosition(tank, posX, posY);
            return new ResponseEntity<>(updatedTank, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtener un tanque específico
    @GetMapping("/{id}")  
    public ResponseEntity<Tank> getTank(@PathVariable String username) {
        Tank tank = tankService.getTankById(username);
        if (tank == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tank, HttpStatus.OK);
    }

}

