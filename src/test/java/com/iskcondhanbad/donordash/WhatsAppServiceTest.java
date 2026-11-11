package com.iskcondhanbad.donordash;

import com.iskcondhanbad.donordash.service.WhatsAppService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WhatsAppServiceTest {

    private final WhatsAppService service = new WhatsAppService();

    @Test
    void testNormalizePhoneNumber() {
        // Standard 10-digit Indian number -> prepends 91 -> 918489140739
        assertEquals("918489140739", service.normalizePhoneNumber("8489140739"));

        // With +91 prefix
        assertEquals("918489140739", service.normalizePhoneNumber("+918489140739"));

        // With leading 0 (11 digits trunk prefix)
        assertEquals("918489140739", service.normalizePhoneNumber("08489140739"));

        // With 0091 prefix
        assertEquals("918489140739", service.normalizePhoneNumber("00918489140739"));

        // With spaces, dashes, or parentheses
        assertEquals("918489140739", service.normalizePhoneNumber("+91 84891-40739"));
        assertEquals("918489140739", service.normalizePhoneNumber("(+91) 84891 40739"));
        assertEquals("918489140739", service.normalizePhoneNumber("84891 40739"));

        // Already 12-digit international format starting with 91
        assertEquals("918489140739", service.normalizePhoneNumber("918489140739"));

        // Empty/null
        assertEquals("", service.normalizePhoneNumber(null));
        assertEquals("", service.normalizePhoneNumber(""));
        assertEquals("", service.normalizePhoneNumber("   "));
    }
}
