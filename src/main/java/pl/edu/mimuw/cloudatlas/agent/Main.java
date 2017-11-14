package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ZMI zone = new ZMI();
        Agent agent = new AgentComputer(zone);
        Thread server = new Thread(new AgentServer(agent));
        Thread fetcher = new Thread(new SystemInformationUpdater(agent));
        server.start();
        fetcher.start();
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\n");
        while(scanner.hasNext())
//            executeQueries(root, scanner.next());
            //TODO interpreter
        scanner.close();
        fetcher.interrupt();
        server.interrupt();
    }
}
