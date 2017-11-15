package pl.edu.mimuw.cloudatlas.client;

import static spark.Spark.get;
import static spark.Spark.post;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import lombok.Data;
import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Value;
import spark.Spark;

import java.io.IOException;
import java.io.StringWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;


public class Client {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final String AGENT_HOST = "localhost";
    private static Agent agent;
    private static Gson g = new Gson();

    public static String handleRequest(ClientRequest req) throws RemoteException {
        try {
            switch (req.getType()) {
                case "attrQ":
                    AttributesMap map = agent.getValues(new PathName(req.getAgent()));
                    Value ret = map.get(req.getQuery());
                    if (ret == null) {
                        return "";
                    }
                    else {
                        return g.toJson(ret);
                    }
                default:
                    return "";
            }
        } catch (RemoteException e) {
            return "";

        }
    }

    public static void main( String[] args) {
        try {
            agent = initializeAgent();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.exit(1);
        }


        Spark.staticFileLocation("/public");
        // insert a post (using HTTP post method)

        post("/connect", ((request, response) -> {
            String aname = request.queryMap("aname").value();
            response.cookie("aname", aname);
            response.status(200);
            response.type("text/html");
            return "<html><head><script>document.location.href = '/agent.html'</script></head><body></body></html>";
        }));

        post("/request", "application/json", (((request, response) -> {
            String json = request.body();
            ClientRequest req = g.fromJson(json, ClientRequest.class);
            //System.out.println(req.getQuery());
            //TODO tutaj użyć request do rmi z agentem
            //agent.installQuery(req.getQuery());
            String body = handleRequest(req);
            response.type("application/json");
            if (body.equals("")) {
                response.status(404);
                return "";
            }
            response.status(200);
            return body;

        })));
    }

    private static Agent initializeAgent() throws RemoteException, NotBoundException {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        Registry registry = LocateRegistry.getRegistry(AGENT_HOST);
        return (Agent) registry.lookup("Agent");
    }
}

