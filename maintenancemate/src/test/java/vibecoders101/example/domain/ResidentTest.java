package vibecoders101.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResidentTest {
    @Test
    void residentTrimsAndStoresRequiredLocationFields() {
        var resident = new Resident("resident-1", "Demo Resident", "  Maple Hostel, Block B  ", " A-12 ");

        assertEquals("Maple Hostel, Block B", resident.getHostelBlock());
        assertEquals("A-12", resident.getRoomNumber());
    }

    @Test
    void residentRejectsBlankOrOverlongLocationFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new Resident("resident-1", "Demo Resident", "   ", "A-12"));
        assertThrows(IllegalArgumentException.class,
                () -> new Resident("resident-1", "Demo Resident", "Maple", "x".repeat(31)));
    }

    @Test
    void identityAndLocationAreNotExposedThroughPublicMutators() {
        assertFalse(java.util.Arrays.stream(Resident.class.getMethods())
                .anyMatch(method -> method.getName().startsWith("set")));
        assertTrue(java.util.Arrays.stream(Resident.class.getDeclaredFields())
                .allMatch(field -> java.lang.reflect.Modifier.isPrivate(field.getModifiers())));
        assertTrue(java.util.Arrays.stream(User.class.getDeclaredFields())
                .allMatch(field -> java.lang.reflect.Modifier.isPrivate(field.getModifiers())));
    }
}
