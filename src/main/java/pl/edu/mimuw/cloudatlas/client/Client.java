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
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.Module;
import spark.Spark;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;


public class Client extends Module {

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

    private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
                ip1, ip2, ip3, ip4
        }));
    }

    private static Set<ValueContact> prepareContacts(String input) {
        String[] conts = input.split(";");
        HashSet<ValueContact> result = new HashSet<>();
        for (String cont : conts) {
            String[] pair = cont.split(":");
            if (pair.length != 2) {
                return null;
            }
            String[] bytes = pair[1].trim().split("\\.");
            if (bytes.length != 4) {
                return null;
            }
            try {
                result.add(createContact(pair[0].trim(), Byte.valueOf(bytes[0]), Byte.valueOf(bytes[1]), Byte.valueOf(bytes[2]), Byte.valueOf(bytes[3])));
            } catch (UnknownHostException e) {
                return null;
            }
        }
        return result;
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
                        if (ret.getType() == TypePrimitive.TIME || ret.getType() == TypePrimitive.BOOLEAN)
                            ret = ret.convertTo(TypePrimitive.STRING);
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
                case "setCon":
                    Set<ValueContact> contacts = prepareContacts(req.getQuery());
                    if (contacts != null) {
                        agent.setContacts(contacts);
                        result.setStatus(Status.OK);
                    }
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

        post("/connect", ((request, response) -> {
            String aname = request.body();
            if (agent.getManagedZones().contains(new PathName(aname)))
                response.status(200);
            else {
                response.status(404);
            }
            response.type("text/html");
            return "";
        }));

        post("/request", "application/json", (((request, response) -> {
            String json = request.body();
            ClientRequest req = g.fromJson(json, ClientRequest.class);
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

