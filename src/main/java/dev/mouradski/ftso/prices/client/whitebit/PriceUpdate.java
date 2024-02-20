package dev.mouradski.ftso.prices.client.whitebit;

import java.util.List;

public class PriceUpdate {

    private String method;
    private List<String> params;
    private String id;  // ou Integer id; selon le cas d'utilisation

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return params != null && params.size() > 0 ? params.get(0) : null;
    }

    public Double getLastPrice() {
        var stringValue = params != null && params.size() > 1 ? params.get(1) : null;
        return stringValue == null ? null : Double.valueOf(stringValue);
    }

    @Override
    public String toString() {
        return "PriceUpdate [method=" + method + ", params=" + params + ", id=" + id + "]";
    }
}
