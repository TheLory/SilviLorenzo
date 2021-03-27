package it.unibo.resumablebw;

import it.unibo.interaction.IssObserver;
import org.json.JSONObject;

public class SecondObserver implements IssObserver {
    @Override
    public void handleInfo(String info) {

        System.out.println("SecondObserver | Sono stato attivato");
    }

    @Override
    public void handleInfo(JSONObject info) {
        System.out.println("SecondObserver | Sono stato attivato");
    }
}
