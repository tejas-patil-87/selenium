package api;

import io.restassured.response.Response;

public class ApiUtils extends BaseApi {

    public Response get(String endpoint) {
        return request.get(endpoint);
    }

    public Response post(String endpoint, Object body) {
        return request.body(body).post(endpoint);
    }

    public Response delete(String endpoint) {
        return request.delete(endpoint);
    }
}
