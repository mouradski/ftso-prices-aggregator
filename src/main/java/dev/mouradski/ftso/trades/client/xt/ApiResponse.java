package dev.mouradski.ftso.trades.client.xt;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {
    private int rc;
    private String mc;
    private List<Object> ma; // Changez Object si vous avez une structure définie pour les éléments de ma
    private List<Ticker> result;

}
