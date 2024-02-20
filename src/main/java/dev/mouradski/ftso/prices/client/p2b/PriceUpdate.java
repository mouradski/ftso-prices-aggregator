package dev.mouradski.ftso.prices.client.p2b;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceUpdate {
    private String method;
    private String[] params;
    private Object id;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

}
