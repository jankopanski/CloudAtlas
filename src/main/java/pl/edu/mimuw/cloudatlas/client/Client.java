package pl.edu.mimuw.cloudatlas.client;

import static spark.Spark.get;
import static spark.Spark.post;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.model.Attribute;
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

    private enum Status {OK, NOT_FOUND, INVALID, EXCEPTION};

    @AllArgsConstructor @Data
    public static class RequestResult {
        public Status status;
        public String response;

        public int statusCode() {
            switch (status) {
                case OK:
                    return 200;
                case NOT_FOUND:
                    return 404;
                case INVALID:
                    return 400;
                case EXCEPTION:
                    return 500;
                default:
                    return 500;

            }
        }

    }

    private static RequestResult handleRequest(ClientRequest req) throws RemoteException {
        AttributesMap map;
        RequestResult result = new RequestResult(Status.INVALID, "");
        try {
            switch (req.getType()) {
                case "attrQ":
                    map = agent.getValues(new PathName(req.getAgent()));
                    Value ret = map.getOrNull(req.getQuery());
                    if (ret == null) {
                       result.setStatus(Status.NOT_FOUND);
                    }
                    else {
                        result.setStatus(Status.OK);
                        result.setResponse(g.toJson(ret));
                    }
                    break;
                case "getAttrs":
                    map = agent.getValues(new PathName(req.getAgent()));
                    StringBuilder response = new StringBuilder();
                    for (Map.Entry<Attribute, Value> e: map) {
                        if (!Attribute.isQuery(e.getKey()))
                            response.append(e.getKey()).append("<br>");
                    }
                    result.setStatus(Status.OK);
                    result.setResponse(response.toString());
                    break;
                case "installQ":
                    if (agent.installQuery(new PathName(req.getAgent()), req.getQuery()))
                        result.setStatus(Status.OK);
                    break;
                case "uninstallQ":
                    if (agent.uninstallQuery(new PathName(req.getAgent()), req.getQuery()))
                        result.setStatus(Status.OK);
                    break;
                default:
                    break;
            }
            return result;
        } catch (RemoteException e) {
            result.setStatus(Status.EXCEPTION);
            return result;
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
            RequestResult result = handleRequest(req);
            response.type("application/json");
            response.status(result.statusCode());
            return result.getResponse();

        })));
    }

    private static Agent initializeAgent() throws RemoteException, NotBoundException {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        Registry registry = LocateRegistry.getRegistry(AGENT_HOST);
        return (Agent) registry.lookup("Agent");
    }
}

