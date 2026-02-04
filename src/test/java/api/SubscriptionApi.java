package api;

import io.restassured.response.Response;

public class SubscriptionApi extends ApiUtils {

	public Response getSubscription(String clientCode) {
		return get("/subscription/" + clientCode);
	}

	public Response createSubscription(Object payload) {
		return post("/subscription", payload);
	}
}
