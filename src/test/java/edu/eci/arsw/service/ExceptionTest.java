package edu.eci.arsw.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.escuelaing.co.exception.InvalidHashException;
import edu.escuelaing.co.exception.RoomFullException;
import edu.escuelaing.co.exception.TankExistsException;

class ExceptionTests {

    @Test
    void testInvalidHashExceptionMessage() {
        // Arrange
        String errorMessage = "Invalid hash provided";

        // Act
        InvalidHashException exception = new InvalidHashException(errorMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testRoomFullExceptionMessage() {
        // Arrange
        String errorMessage = "Room is full";

        // Act
        RoomFullException exception = new RoomFullException(errorMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTankExistsExceptionMessage() {
        // Arrange
        String errorMessage = "Tank already exists";

        // Act
        TankExistsException exception = new TankExistsException(errorMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testInvalidHashExceptionIsInstanceOfException() {
        // Act
        InvalidHashException exception = new InvalidHashException("Invalid hash");

        // Assert
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testRoomFullExceptionIsInstanceOfException() {
        // Act
        RoomFullException exception = new RoomFullException("Room is full");

        // Assert
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testTankExistsExceptionIsInstanceOfException() {
        // Act
        TankExistsException exception = new TankExistsException("Tank exists");

        // Assert
        assertTrue(exception instanceof Exception);
    }
}
