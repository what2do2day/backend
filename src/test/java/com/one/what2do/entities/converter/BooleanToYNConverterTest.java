package com.one.what2do.entities.converter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BooleanToYNConverterTest {

    private final BooleanToYNConverter converter = new BooleanToYNConverter();

    @Test
    void convertToDatabaseColumn_True_ReturnsY() {
        String result = converter.convertToDatabaseColumn(true);
        assertEquals("Y", result);
    }

    @Test
    void convertToDatabaseColumn_False_ReturnsN() {
        String result = converter.convertToDatabaseColumn(false);
        assertEquals("N", result);
    }

    @Test
    void convertToDatabaseColumn_Null_ReturnsN() {
        String result = converter.convertToDatabaseColumn(null);
        assertEquals("N", result);
    }

    @Test
    void convertToEntityAttribute_Y_ReturnsTrue() {
        Boolean result = converter.convertToEntityAttribute("Y");
        assertTrue(result);
    }

    @Test
    void convertToEntityAttribute_y_ReturnsTrue() {
        Boolean result = converter.convertToEntityAttribute("y");
        assertTrue(result);
    }

    @Test
    void convertToEntityAttribute_N_ReturnsFalse() {
        Boolean result = converter.convertToEntityAttribute("N");
        assertFalse(result);
    }

    @Test
    void convertToEntityAttribute_n_ReturnsFalse() {
        Boolean result = converter.convertToEntityAttribute("n");
        assertFalse(result);
    }

    @Test
    void convertToEntityAttribute_Null_ReturnsFalse() {
        Boolean result = converter.convertToEntityAttribute(null);
        assertFalse(result);
    }

    @Test
    void convertToEntityAttribute_OtherValue_ReturnsFalse() {
        Boolean result = converter.convertToEntityAttribute("OTHER");
        assertFalse(result);
    }
} 