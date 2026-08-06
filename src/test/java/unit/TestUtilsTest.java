package unit;

import org.testng.Assert;
import org.testng.annotations.Test;

import utils.TestUtils;

public class TestUtilsTest {

    // ─── formatToIndianCurrency ───────────────────────────────────────────────

    @Test(description = "Format amount below 1000 — no commas expected")
    public void formatAmount_below1000() {
        Assert.assertEquals(TestUtils.formatToIndianCurrency(500), "₹500");
    }

    @Test(description = "Format exact 1000")
    public void formatAmount_exactly1000() {
        Assert.assertEquals(TestUtils.formatToIndianCurrency(1000), "₹1,000");
    }

    @Test(description = "Format 1,50,000 — standard min investment amount")
    public void formatAmount_150000() {
        Assert.assertEquals(TestUtils.formatToIndianCurrency(150000), "₹1,50,000");
    }

    @Test(description = "Format 5,00,000 — 2x investment amount")
    public void formatAmount_500000() {
        Assert.assertEquals(TestUtils.formatToIndianCurrency(500000), "₹5,00,000");
    }

    @Test(description = "Format 10,00,000 — large amount")
    public void formatAmount_1000000() {
        Assert.assertEquals(TestUtils.formatToIndianCurrency(1000000), "₹10,00,000");
    }

    // ─── parseAmount ─────────────────────────────────────────────────────────

    @Test(description = "Parse ₹1,50,000 back to integer")
    public void parseAmount_150000() {
        Assert.assertEquals(TestUtils.parseAmount("₹1,50,000"), 150000);
    }

    @Test(description = "Parse ₹5,00,000 back to integer")
    public void parseAmount_500000() {
        Assert.assertEquals(TestUtils.parseAmount("₹5,00,000"), 500000);
    }

    @Test(description = "Parse amount without ₹ symbol")
    public void parseAmount_noSymbol() {
        Assert.assertEquals(TestUtils.parseAmount("1,50,000"), 150000);
    }

    @Test(description = "Parse amount with extra whitespace")
    public void parseAmount_withSpaces() {
        Assert.assertEquals(TestUtils.parseAmount("  ₹1,50,000  "), 150000);
    }

    @Test(description = "formatToIndianCurrency and parseAmount are inverse operations")
    public void formatAndParse_roundTrip() {
        int original = 250000;
        String formatted = TestUtils.formatToIndianCurrency(original);
        int parsed = TestUtils.parseAmount(formatted);
        Assert.assertEquals(parsed, original, "Round-trip format→parse should return original value");
    }

    @Test(description = "parseAmount throws on null input",
          expectedExceptions = IllegalArgumentException.class)
    public void parseAmount_nullInput_throwsException() {
        TestUtils.parseAmount(null);
    }

    @Test(description = "parseAmount throws on blank input",
          expectedExceptions = IllegalArgumentException.class)
    public void parseAmount_blankInput_throwsException() {
        TestUtils.parseAmount("   ");
    }
}
