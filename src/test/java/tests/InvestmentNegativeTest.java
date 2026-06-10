package tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import base.BaseInvestmentTest;
import listeners.TestListener;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import utils.ExcelDataReader;
import utils.TestUtils;

@Epic("Investment Management Platform")
@Feature("Investment Validations")
public class InvestmentNegativeTest extends BaseInvestmentTest {

	@DataProvider(name = "invalidInvestmentAmounts")
	public Object[][] invalidInvestmentAmounts() {
		String minInvestment = ExcelDataReader.get("product.min.investment");
		String notMultiple = ExcelDataReader.get("invalid.amount.not.multiple");
		String errorNotMultiple = ExcelDataReader.get("error.not.multiple");
		String errorMinAmount = ExcelDataReader.get("error.min.amount");
		String invalidZero = ExcelDataReader.get("invalid.amount.zero");

		return new Object[][] {
				{ minInvestment + notMultiple, errorNotMultiple },
				{ notMultiple, errorMinAmount },
				{ invalidZero, errorMinAmount } };
	}

	@Story("Invalid Amount Validation")
	@Severity(SeverityLevel.NORMAL)
	@Test(priority = 3, dependsOnMethods = "productFlowTest", dataProvider = "invalidInvestmentAmounts", groups = "negative", description = "Verify Invalid Investment Amount Validations")
	public void verifyInvestmentAmountValidations(String amount, String expectedError) {
		TestListener.logStep("Entering invalid amount: " + amount);
		investmentPage.enterInvestmentAmount(amount);
		investmentPage.proceedFromInvestmentAmountPopup();
		TestListener.logStep("Verifying error toast for amount: " + amount);
		Assert.assertTrue(investmentPage.isErrorToastVisible(),
				"Error toast did not appear for amount: " + amount);
		String actualError = investmentPage.getErrorToastText();
		Assert.assertEquals(actualError, expectedError, "Validation failed for investment amount: " + amount
				+ " | Expected: '" + expectedError + "' | Actual: '" + actualError + "'");
	}

	@Story("Investment Flow")
	@Severity(SeverityLevel.NORMAL)
	@Test(priority = 4, dependsOnMethods = "verifyInvestmentAmountValidations", description = "Verify Investment Flow with Activation Model")
	public void investFlowTest() {
		String baseAmount = ExcelDataReader.get("product.min.investment");
		int baseAmountInt = TestUtils.parseAmount(baseAmount);

		TestListener.logStep("Verifying investment amount buttons");
		List<Integer> actualAmounts = investmentPage.getAmountButtonValues();
		for (int i = 0; i < actualAmounts.size(); i++) {
			int multiplier = i + 1;
			int expectedAmount = baseAmountInt * multiplier;
			Assert.assertEquals((int) actualAmounts.get(i), expectedAmount,
					"Investment amount button verification failed | Button multiplier: " + multiplier + "x | Expected: ₹"
							+ expectedAmount + " | Actual: ₹" + actualAmounts.get(i));
		}

		TestListener.logStep("Selecting 2x investment amount");
		investmentPage.selectAmountAndGetExpectedAmount(2, baseAmount);
		investmentPage.proceedFromInvestmentAmountPopup();

		TestListener.logStep("Verifying activation model");
		SoftAssert sa = new SoftAssert();
		sa.assertTrue(investmentPage.isActivationModelVisible(),
				"Activation Model visibility check failed | Expected: visible | Actual: Not visible");
		int iconCount = investmentPage.getListIconCount();
		sa.assertEquals(iconCount, 2,
				"List icon verification failed | Expected: 2 | Actual: " + iconCount);
		sa.assertEquals(investmentPage.getPortfolioDescription(), ExcelDataReader.get("activation.model.description"),
				"Portfolio description mismatch | Expected: '" + ExcelDataReader.get("activation.model.description")
						+ "' | Actual: '" + investmentPage.getPortfolioDescription() + "'");
		sa.assertEquals(investmentPage.getStandardBrokerage(), ExcelDataReader.get("activation.model.brokerage.standard"),
				"Standard brokerage mismatch | Expected: '" + ExcelDataReader.get("activation.model.brokerage.standard")
						+ "' | Actual: '" + investmentPage.getStandardBrokerage() + "'");
		sa.assertEquals(investmentPage.getNextCtaText(), ExcelDataReader.get("activation.model.next.cta.text"),
				"CTA button text mismatch | Expected: '" + ExcelDataReader.get("activation.model.next.cta.text")
						+ "' | Actual: '" + investmentPage.getNextCtaText() + "'");
		sa.assertAll();

		investmentPage.clickActivationModelNextButton();
		investmentPage.clickEditIcon();
	}

	@Story("Edit Amount Validation")
	@Severity(SeverityLevel.MINOR)
	@Test(priority = 5, dataProvider = "invalidInvestmentAmounts", dependsOnMethods = "investFlowTest", description = "Verify Edit Investment Amount Validations")
	public void negativeEditPopup(String amount, String expectedError) {
		TestListener.logStep("Entering invalid edit amount: " + amount);
		investmentPage.enterEditInvestmentAmount(amount);
		investmentPage.clickInvestNow();
		TestListener.logStep("Verifying error toast for edit amount: " + amount);
		Assert.assertTrue(investmentPage.isEditErrorToastVisible(),
				"Error toast did not appear for edit amount: " + amount);
		String actualError = investmentPage.getEditErrorToastText();
		Assert.assertEquals(actualError, expectedError, "Validation failed for edit investment amount: " + amount
				+ " | Expected: '" + expectedError + "' | Actual: '" + actualError + "'");
	}
}
