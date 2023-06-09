package dev.mouradski.ftso.trades.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

    private Counter counter;

    @BeforeEach
    void setUp() {
        counter = new Counter();
    }

    @Test
    void getCount() {
        assertEquals(1, counter.getCount());
        assertEquals(2, counter.getCount());
        assertEquals(3, counter.getCount());

        for (int i = 0; i < 10; i++) {
            counter.getCount();
        }

        assertEquals(14, counter.getCount());
    }
}
