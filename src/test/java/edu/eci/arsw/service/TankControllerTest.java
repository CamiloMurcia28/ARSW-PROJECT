package edu.eci.arsw.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import edu.escuelaing.co.exception.InvalidHashException;
import edu.escuelaing.co.exception.RoomFullException;
import edu.escuelaing.co.exception.TankExistsException;
import edu.escuelaing.co.leotankcicos.controller.TankController;
import edu.escuelaing.co.leotankcicos.model.Bullet;
import edu.escuelaing.co.leotankcicos.model.Tank;
import edu.escuelaing.co.leotankcicos.service.TankService;
import jakarta.servlet.http.HttpSession;

class TankControllerTest {

    @Mock
    private TankService tankService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TankController tankController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOkEndpoint() {
        ResponseEntity<Void> response = tankController.ok();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateTankSuccess() throws InvalidHashException, RoomFullException, TankExistsException, NoSuchAlgorithmException, InvalidKeyException {
        String username = "TankUser";
        String hash = "hash123";
        Map<String, Object> request = Map.of("username", username, "hash", hash);

        Tank mockTank = new Tank(1, 8, "#fa0a0a", 0,username);
        when(tankService.saveTank(username, hash)).thenReturn(mockTank);

        ResponseEntity<Tank> response = tankController.createTank(request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockTank, response.getBody());
        verify(session).setAttribute("username", username);
    }

    @Test
    void testCreateTankFailure() {
        Map<String, Object> invalidRequest = Collections.emptyMap();

        ResponseEntity<Tank> response = tankController.createTank(invalidRequest, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetAllTanks() {
        Tank tank1 = new Tank(1, 8, "#fa0a0a", 0,"tank1");
        Tank tank2 = new Tank(1, 8, "#fa0a0a", 0,"tank2");
        when(tankService.getAllTanks()).thenReturn(List.of(tank1, tank2));

        ResponseEntity<List<Tank>> response = tankController.getAllTanks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(tank1));
        assertTrue(response.getBody().contains(tank2));
    }

    @Test
    void testGetUsernameFound() {
        String mockUsername = "TankUser";
        when(session.getAttribute("username")).thenReturn(mockUsername);

        ResponseEntity<String> response = tankController.getUsername(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUsername, response.getBody());
    }

    @Test
    void testGetUsernameNotFound() {
        when(session.getAttribute("username")).thenReturn(null);

        ResponseEntity<String> response = tankController.getUsername(session);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Username not found", response.getBody());
    }

    @Test
    void testGetTankByUsernameFound() {
        String username = "TankUser";
        Tank mockTank = new Tank(1, 8, "#fa0a0a", 0,"tank1");
        when(tankService.getTankById(username)).thenReturn(mockTank);

        ResponseEntity<Tank> response = tankController.getTank(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockTank, response.getBody());
    }

    @Test
    void testGetTankByUsernameNotFound() {
        String username = "UnknownUser";
        when(tankService.getTankById(username)).thenReturn(null);

        ResponseEntity<Tank> response = tankController.getTank(username);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetBulletPositionFound() {
        String bulletId = "Bullet1";
        Bullet mockBullet = new Bullet (bulletId, 5, 5,90,false, "tankid");
        when(tankService.getBulletPosition(bulletId)).thenReturn(mockBullet);

        ResponseEntity<Bullet> response = tankController.getBulletPosition(bulletId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBullet, response.getBody());
    }

    @Test
    void testGetBulletPositionNotFound() {
        String bulletId = "UnknownBullet";
        when(tankService.getBulletPosition(bulletId)).thenReturn(null);

        ResponseEntity<Bullet> response = tankController.getBulletPosition(bulletId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}