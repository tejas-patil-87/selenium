package api.request;

public class InvestmentRequest {

    private String clientCode;

    public InvestmentRequest(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }
}
