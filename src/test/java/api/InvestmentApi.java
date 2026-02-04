package api;

import api.request.InvestmentRequest;
import io.restassured.response.Response;
import utils.ConfigReader;

public class InvestmentApi extends BaseApi {

	public Response sendInvestmentRequest() {

		InvestmentRequest body = new InvestmentRequest(ConfigReader.get("clientCode"));

		return request.body(body).post("/ui/api/client/Investment/InvestmentRequest");
	}
}
