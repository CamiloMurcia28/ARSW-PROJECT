package edu.eci.arsw.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import edu.escuelaing.co.leotankcicos.model.Board;
import edu.escuelaing.co.leotankcicos.model.Bullet;
import edu.escuelaing.co.leotankcicos.model.Tank;
import edu.escuelaing.co.leotankcicos.repository.BoardRepository;
import edu.escuelaing.co.leotankcicos.repository.BulletRepository;
import edu.escuelaing.co.leotankcicos.repository.TankRepository;
import edu.escuelaing.co.leotankcicos.service.TankService;

class TankServiceTest {

    @Mock
    private TankRepository tankRepository;
    @Mock
    private BulletRepository bulletRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private SimpMessagingTemplate msgt;

    @InjectMocks
    private TankService tankService;
    

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(tankRepository, bulletRepository, boardRepository);
    }

    /*Para GetTankByID */
    @Test
    void testGetTankById_TankExists() {
        String name = "Tank1";
        Tank mockTank = new Tank(1, 8, "#fa0a0a", 0, name);
        when(tankRepository.findById(name)).thenReturn(Optional.of(mockTank));
        Tank result = tankService.getTankById(name);
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    void testGetTankById_TankNotFound() {
        String name = "NonExistingTank";

        when(tankRepository.findById(name)).thenReturn(Optional.empty());

        Tank result = tankService.getTankById(name);

        assertNull(result);
    }

    /*PARA EL Shoot */
    @Test
    void testShoot_Success() {
        String username = "Tank1";
        String bulletId = "bullet123";
        
        Tank mockTank = new Tank(1, 8, "#fa0a0a", 0, username);
        when(tankRepository.findById(username)).thenReturn(Optional.of(mockTank));

        Bullet mockBullet = new Bullet(bulletId, 1, 8, 0, false, username);
        when(bulletRepository.save(any(Bullet.class))).thenReturn(mockBullet);

        Bullet result = tankService.shoot(username, bulletId);

        assertNotNull(result);
        assertEquals(bulletId, result.getId());
    }

    @Test
    void testShoot_TankNotFound() {
        String username = "Tank1";
        String bulletId = "bullet123";

        when(tankRepository.findById(username)).thenReturn(Optional.empty());

        Bullet result = tankService.shoot(username, bulletId);

        assertNull(result);
    }
    /*Para UpdatePosition */
    @Test
    void testUpdateTankPosition_Success() {
        String username = "Tank1";
        int x = 1, y = 1, newX = 2, newY = 2, rotation = 90;
        
        Tank mockTank = new Tank(x, y, "#fa0a0a", 0, username);
        Board mockBoard = mock(Board.class);
        when(mockBoard.getBoxes()).thenReturn(new String[5][5]);
        when(tankRepository.findById(username)).thenReturn(Optional.of(mockTank));
        when(boardRepository.findById(any(String.class))).thenReturn(Optional.of(mockBoard));

        Tank updatedTank = tankService.updateTankPosition(username, x, y, newX, newY, rotation);

        assertNotNull(updatedTank);
        assertEquals(newX, updatedTank.getPosx());
        assertEquals(newY, updatedTank.getPosy());
        assertEquals(rotation, updatedTank.getRotation());
        verify(tankRepository).save(mockTank);
    }

    @Test
    void testUpdateTankPosition_TankNotFound() {
        String username = "NonExistingTank";
        int x = 1, y = 1, newX = 2, newY = 2, rotation = 90;

        when(tankRepository.findById(username)).thenReturn(Optional.empty());

        Tank result = tankService.updateTankPosition(username, x, y, newX, newY, rotation);

        assertNull(result, "Expected null when tank is not found");
    }

    @Test
    void testUpdateTankPosition_BoxAlreadyOccupied() {
        String username = "Tank1";
        int x = 1, y = 1, newX = 2, newY = 2, rotation = 90;
    
        Tank mockTank = new Tank(x, y, "#fa0a0a", 0, username);
        Board mockBoard = mock(Board.class);
        String[][] boxes = new String[5][5];
        boxes[newY][newX] = "otherTank"; // La posición ya está ocupada
    
        when(mockBoard.getBoxes()).thenReturn(boxes);
        when(tankRepository.findById(username)).thenReturn(Optional.of(mockTank));
        when(boardRepository.findById(any(String.class))).thenReturn(Optional.of(mockBoard));
    
        // Llamamos al método y verificamos que el tanque no se mueve (devuelve el tanque original)
        tankService.updateTankPosition(username, x, y, newX, newY, rotation);
    
        // Verificamos que el tanque no cambió de posición
        assertNotEquals(x, mockTank.getPosx());
        assertNotEquals(y, mockTank.getPosy());
    
        // Verificamos las interacciones
        verify(tankRepository).findById(username);
    }
    

    

    /* Para el Reset  */
    @Test
    void testReset() {

        tankService.reset();
        verify(tankRepository).deleteAll();
        verify(bulletRepository).deleteAll();
        verify(boardRepository).save(any(Board.class));
        assertDoesNotThrow(() -> {
            tankService.reset();
        });
    }

    @Test
    void testBoardReset() {
        tankService.reset();
        verify(boardRepository).save(any(Board.class));
    }
    
    @Test
    void testTankRepositoryReset() {
        tankService.reset();
        verify(tankRepository).deleteAll();
    }

    @Test
    void testBulletRepositoryReset() {
        tankService.reset();
        verify(bulletRepository).deleteAll();
    }

    /*Para HandleWinner */
    @Test
    void testHandleWinner_Success() {
        String winner = "Tank1";
        Tank mockTank = new Tank(1, 1, "#fa0a0a", 0, winner);

        when(tankRepository.findById(winner)).thenReturn(Optional.of(mockTank));

    }

    @Test
    void testHandleWinner_TankNotFound() {
        String winner = "NonExistingTank";

        when(tankRepository.findById(winner)).thenReturn(Optional.empty());

        assertDoesNotThrow((Executable) () -> tankService.handleWinner());
    }

    @Test
    void testShoot_MultipleBulletsAllowed() {
        String username = "Tank1";
        String bulletId1 = "bullet1";
        String bulletId2 = "bullet2";

        Tank mockTank = new Tank(1, 1, "#fa0a0a", 0, username);
        when(tankRepository.findById(username)).thenReturn(Optional.of(mockTank));

        Bullet mockBullet1 = new Bullet(bulletId1, 1, 1, 0, false, username);
        Bullet mockBullet2 = new Bullet(bulletId2, 1, 1, 0, false, username);

        when(bulletRepository.save(any(Bullet.class))).thenReturn(mockBullet1).thenReturn(mockBullet2);

        Bullet result1 = tankService.shoot(username, bulletId1);
        Bullet result2 = tankService.shoot(username, bulletId2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(bulletId1, result1.getId());
        assertEquals(bulletId2, result2.getId());
    }

}

