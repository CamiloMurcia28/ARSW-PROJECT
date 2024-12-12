package edu.eci.arsw.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        
        Board board = new Board();
        board.setId("board1");
        
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById("board1")).thenReturn(Optional.of(board));
    }

    /*Para saveTank*/


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

        Bullet mockBullet = new Bullet(bulletId, 1, 8, 0, true, username);
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
    void testUpdateTankPosition_TankNotInOriginalPosition(){
        String username = "Tank1";
        int x = 1, y = 4, newX = 2, newY = 4, rotation = 90;
        Tank mockTank = new Tank(x, y, "#fa0a0a", 0, username);
        when(tankRepository.findById(username)).thenReturn(Optional.of(mockTank));
    
       
        String[][] mockBoxes = new String[5][5]; 
        for (int i = 0; i < mockBoxes.length; i++) {
            for (int j = 0; j < mockBoxes[i].length; j++) {
                if (i == y && j == x) {
                    mockBoxes[i][j] = username;  
                } else {
                    mockBoxes[i][j] = "0";  
                }
            }
        }
    
        mockBoxes[newY][newX] = username; 
        Board mockBoard = mock(Board.class);
        when(mockBoard.getBoxes()).thenReturn(mockBoxes); 
        when(boardRepository.findAll()).thenReturn(List.of(mockBoard));
   
        Exception exception = assertThrows(Exception.class, () -> {
            tankService.updateTankPosition(username, x, y, newX, newY, rotation);
        });
    
        assertEquals("Tank is no longer in the original position", exception.getMessage());
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




}

