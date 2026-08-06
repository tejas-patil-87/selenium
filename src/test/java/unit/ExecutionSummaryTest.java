package unit;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import utils.ExecutionSummary;

public class ExecutionSummaryTest {

    @BeforeMethod
    public void resetCounters() {
        // Reset all counters before each test to avoid state leaking between tests
        ExecutionSummary.setTotalTests(0);
        ExecutionSummary.setStartTime(0);
        ExecutionSummary.setEndTime(0);
        // Re-initialize by setting known values
        ExecutionSummary.setTotalTests(0);
    }

    @Test(description = "Pass rate is 100 when all tests pass")
    public void passRate_allPassed() {
        ExecutionSummary.incrementPassed();
        ExecutionSummary.incrementPassed();
        ExecutionSummary.incrementPassed();
        Assert.assertEquals(ExecutionSummary.getPassRate(), 100);
    }

    @Test(description = "Pass rate is 0 when all tests fail")
    public void passRate_allFailed() {
        ExecutionSummary.incrementFailed();
        ExecutionSummary.incrementFailed();
        Assert.assertEquals(ExecutionSummary.getPassRate(), 0);
    }

    @Test(description = "Pass rate is 50 when half pass half fail")
    public void passRate_halfHalf() {
        ExecutionSummary.incrementPassed();
        ExecutionSummary.incrementFailed();
        Assert.assertEquals(ExecutionSummary.getPassRate(), 50);
    }

    @Test(description = "Pass rate is 0 when no tests ran — avoids divide by zero")
    public void passRate_noTests_returnsZero() {
        Assert.assertEquals(ExecutionSummary.getPassRate(), 0);
    }

    @Test(description = "Execution time formats correctly in minutes and seconds")
    public void executionTime_format() {
        long start = System.currentTimeMillis();
        long end = start + (2 * 60 * 1000) + (30 * 1000); // 2 min 30 sec
        ExecutionSummary.setStartTime(start);
        ExecutionSummary.setEndTime(end);
        Assert.assertEquals(ExecutionSummary.getExecutionTime(), "2 min 30 sec");
    }

    @Test(description = "Failed test is added to the failed tests list")
    public void addFailedTest_appearsInList() {
        ExecutionSummary.FailedTest ft = new ExecutionSummary.FailedTest(
                "investFlowTest", "tests.NewInvestmentTest", "Element not found");
        ExecutionSummary.addFailedTest(ft);
        Assert.assertTrue(ExecutionSummary.getFailedTests().contains(ft));
    }

    @Test(description = "buildFailedRows returns NA row when no failures")
    public void buildFailedRows_noFailures_returnsNA() {
        String rows = ExecutionSummary.buildFailedRows();
        Assert.assertTrue(rows.contains("NA"), "Expected NA placeholder when no failed tests");
    }
}
