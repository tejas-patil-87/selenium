package api;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import utils.ConfigReader;

public class BaseApi {

	protected RequestSpecification request;

	public BaseApi() {
		RestAssured.baseURI = ConfigReader.get("api.base.url");

		request = RestAssured.given().header("Content-Type", "application/json")
				.header("x-api-key", ConfigReader.get("api.key")).log().all();
	}
}
