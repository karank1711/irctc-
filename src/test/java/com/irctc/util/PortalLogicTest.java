package com.irctc.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalLogicTest {
    @Test
    void indianGroupingUsesLastThreeThenPairs() {
        assertEquals("8,92,461", Formatters.integer(892461));
        assertEquals("1,200", Formatters.integer(1200));
    }

    @Test
    void rupeesUseCroreAndLakhThresholds() {
        assertEquals("\u20B912.48 Cr", Formatters.rupees(new BigDecimal("124800000")));
        assertEquals("\u20B92.50 L", Formatters.rupees(new BigDecimal("250000")));
    }

    @Test
    void comparisonIsUnavailableWithoutAPreviousBaseline() {
        PeriodComparison comparison = PeriodComparison.of(12, 0);
        assertFalse(comparison.isAvailable());
        assertEquals("Comparison unavailable", comparison.getLabel());
    }

    @Test
    void comparisonUsesThePreviousPeriod() {
        PeriodComparison comparison = PeriodComparison.of(15, 10);
        assertTrue(comparison.isAvailable());
        assertTrue(comparison.isIncrease());
        assertEquals("50.0%", comparison.getLabel());
    }

    @Test
    void sqlScriptKeepsSemicolonsInsideQuotes() {
        var statements = SqlScript.statements("""
                -- comment
                INSERT INTO notes (message) VALUES ('a; b');
                SELECT 1;
                """);
        assertEquals(2, statements.size());
        assertTrue(statements.get(0).contains("'a; b'"));
    }

    @Test
    void likePatternEscapesWildcards() {
        assertEquals("%100\\%\\_%", TextSearch.contains("100%_"));
    }

    @Test
    void csvQuotesCommasAndQuotes() {
        assertEquals("\"a,b\"", Csv.cell("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", Csv.cell("say \"hi\""));
    }

    @Test
    void passwordHashDoesNotStoreThePlainPassword() {
        String hash = PasswordUtil.hash("correct-horse");
        assertFalse(hash.contains("correct-horse"));
        assertTrue(PasswordUtil.matches("correct-horse", hash));
        assertFalse(PasswordUtil.matches("wrong-horse", hash));
    }

    @Test
    void portalLinksStayInsideTheApplication() {
        assertTrue(Catalog.safeDestination("/bookings"));
        assertTrue(Catalog.safeDestination("/manage/banner"));
        assertFalse(Catalog.safeDestination("https://example.com"));
        assertFalse(Catalog.safeDestination("//evil"));
        assertTrue(Catalog.safeImageReference("/assets/images/train-banner.jpg"));
        assertFalse(Catalog.safeImageReference("javascript:alert(1)"));
    }

    @Test
    void sparklineContainsOnlyGeneratedNumbers() {
        String svg = Sparkline.toSvg(java.util.List.of(0L, 2L, 4L));
        assertTrue(svg.startsWith("<svg"));
        assertFalse(svg.contains("<script"));
    }
}
