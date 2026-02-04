package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import api.InvestmentApi;
import io.restassured.response.Response;

public class InvestmentApiTest {

	@Test
	public void verifyInvestmentRequestApi() {

		InvestmentApi api = new InvestmentApi();
		Response response = api.sendInvestmentRequest();

		Assert.assertEquals(response.getStatusCode(), 200, "Status code mismatch");

		Assert.assertTrue(response.asString().contains("success"), "Response does not indicate success");
	}
}
