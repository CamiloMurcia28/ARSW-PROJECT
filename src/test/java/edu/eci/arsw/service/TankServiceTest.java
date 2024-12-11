package edu.eci.arsw.service;

import java.util.List;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        // Inicializamos los mocks
        MockitoAnnotations.openMocks(this);

        // Limpiar el estado de los repositorios mockeados
        reset(tankRepository, bulletRepository, boardRepository);

        // Crear y guardar un tablero en el repositorio simulado
        Board board = new Board();
        board.setId("board1");
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardRepository.findById("board1")).thenReturn(java.util.Optional.of(board));
    }
    private static final String SECRET_KEY = "shared_secret_key";

    private String calculateHash(String message) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hashBytes = sha256Hmac.doFinal(message.getBytes());
        return Hex.encodeHexString(hashBytes);
    }

    /*PARA EL SAVETANK */
     @Test
    void testSaveTank_HashMismatch() throws Exception {
        // Datos de entrada
        String username = "Tank1";
        String receivedHash = "wrongHash";

        // Comportamiento simulado: el hash no coincide
        String expectedHash = calculateHash(username);
        assertNotEquals(expectedHash, receivedHash, "El hash recibido es incorrecto.");

        // Llamada al método
        Exception exception = assertThrows(Exception.class, () -> {
            tankService.saveTank(username, receivedHash);
        });

        // Verificar el mensaje de la excepción
        assertEquals("El hash del mensaje no coincide. El mensaje puede haber sido alterado.", exception.getMessage());
    }

    @Test
    void testSaveTank_RoomFull() throws Exception {
        // Datos de entrada
        String username = "Tank2";
        String receivedHash = calculateHash(username);

        // Comportamiento simulado: la sala está llena
        when(tankRepository.count()).thenReturn(10L);  // Suponiendo que el máximo de jugadores es 10

        // Llamada al método
        Exception exception = assertThrows(Exception.class, () -> {
            tankService.saveTank(username, receivedHash);
        });

        // Verificar el mensaje de la excepción
        assertEquals("The room is full", exception.getMessage());
    }

    @Test
    void testSaveTank_TankAlreadyExists() throws Exception {
        // Datos de entrada
        String username = "Tank1";
        String receivedHash = calculateHash(username);

        // Comportamiento simulado: el tanque ya existe
        when(tankRepository.findById(username)).thenReturn(java.util.Optional.of(new Tank(0, 0, "Red", 0, username)));

        // Llamada al método
        Exception exception = assertThrows(Exception.class, () -> {
            tankService.saveTank(username, receivedHash);
        });

        // Verificar el mensaje de la excepción
        assertEquals("Tank with this name already exists or is invalid", exception.getMessage());
    }

    @Test
    void testSaveTank_InvalidName() throws Exception {
        // Datos de entrada
        String username = "1";  // Nombre inválido
        String receivedHash = calculateHash(username);

        // Llamada al método
        Exception exception = assertThrows(Exception.class, () -> {
            tankService.saveTank(username, receivedHash);
        });

        // Verificar el mensaje de la excepción
        assertEquals("Tank with this name already exists or is invalid", exception.getMessage());
    }


    /*Para GetTankByID */
    @Test
    public void testGetTankById_TankExists() {
        String name = "Tank1";
        Tank mockTank = new Tank(1, 8, "#fa0a0a", 0, name);
        
        when(tankRepository.findById(name)).thenReturn(Optional.of(mockTank));

        Tank result = tankService.getTankById(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    public void testGetTankById_TankNotFound() {
        String name = "NonExistingTank";

        when(tankRepository.findById(name)).thenReturn(Optional.empty());

        Tank result = tankService.getTankById(name);

        assertNull(result);
    }

    /*PARA EL Shoot */
    @Test
    public void testShoot_Success() {
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
    public void testShoot_TankNotFound() {
        String username = "Tank1";
        String bulletId = "bullet123";

        when(tankRepository.findById(username)).thenReturn(Optional.empty());

        Bullet result = tankService.shoot(username, bulletId);

        assertNull(result);
    }
    /*Para UpdatePosition */
    @Test
    public void testUpdateTankPosition_TankNotInOriginalPosition() throws Exception {
        String username = "Tank1";
        int x = 1, y = 4, newX = 2, newY = 4, rotation = 90;
    
        // Mock del tanque en el repositorio (posición original)
        Tank mockTank = new Tank(x, y, "#fa0a0a", 0, username);
        when(tankRepository.findById(username)).thenReturn(Optional.of(mockTank));
    
        // Simulamos el estado del tablero con un array 2D de casillas
        String[][] mockBoxes = new String[5][5]; // Tamaño de tablero 5x5
        for (int i = 0; i < mockBoxes.length; i++) {
            for (int j = 0; j < mockBoxes[i].length; j++) {
                if (i == y && j == x) {
                    mockBoxes[i][j] = username;  // Colocamos el nombre del tanque en la casilla original
                } else {
                    mockBoxes[i][j] = "0";  // Las demás casillas están vacías
                }
            }
        }
    
        // Simulamos que la casilla de destino (newX, newY) está ocupada por el tanque
        mockBoxes[newY][newX] = username;  // Simulamos que el tanque se movió a la nueva posición
    
        // Mock del tablero
        Board mockBoard = mock(Board.class);
        when(mockBoard.getBoxes()).thenReturn(mockBoxes); // Retorna el estado simulado del tablero
        when(boardRepository.findAll()).thenReturn(List.of(mockBoard));
    
        // Ejecutamos el método de actualización, lo que debería lanzar la excepción
        Exception exception = assertThrows(Exception.class, () -> {
            tankService.updateTankPosition(username, x, y, newX, newY, rotation);
        });
    
        // Verificamos que la excepción correcta sea lanzada
        assertEquals("Tank is no longer in the original position", exception.getMessage());
    }

    // @Test
    // public void testCheckVictory_SingleTank() {
    //     // Setup
    //     Tank tank = new Tank(1, 8, "#fa0a0a", 100, "tank1");
    //     when(tankRepository.findAll()).thenReturn(Collections.singletonList(tank));

    //     // Execute
    //     Tank result = tankService.checkVictory();

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals("tank1", result.getName());
    // }

    // @Test
    // public void testCheckVictory_MultipleTanks() {
    //     // Setup
    //     Tank tank1 = new Tank(1, 8, "#fa0a0a", 100, "tank1");
    //     Tank tank2 = new Tank(2, 9, "#00ff00", 100, "tank2");
    //     when(tankRepository.findAll()).thenReturn(Arrays.asList(tank1, tank2));

    //     // Execute
    //     Tank result = tankService.checkVictory();

    //     // Assert
    //     assertNull(result);
    // }

}
