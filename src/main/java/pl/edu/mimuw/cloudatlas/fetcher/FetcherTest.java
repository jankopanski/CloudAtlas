package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Value;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FetcherTest {

    public static void main(String[] args) {
        System.out.println("Test active");
        FetcherClient fetcher = new FetcherClient();
        System.out.println("Featcher created");
        while (true) {
            try {
                System.out.println("fetchInfo()");
                AttributesMap map = fetcher.fetchInfo();
                System.out.println("iterate");
                for (Map.Entry<Attribute, Value> entry : map) {
                    System.out.println(entry.getKey().getName() + ": " + entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
