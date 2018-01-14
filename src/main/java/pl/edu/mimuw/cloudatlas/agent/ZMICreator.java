package pl.edu.mimuw.cloudatlas.agent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class ZMICreator {
    private static ZMI root, zmi;

    private static JSONObject readConfig(String file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(new FileReader(file));
    }

    static void createHierarchy(String file) throws IOException, ParseException, InvalidConfigException, java.text.ParseException {
        JSONObject obj = readConfig(file);
        Long timestamp = System.currentTimeMillis() / 1000L;
        PathName path = new PathName((String) obj.get("path"));
        String expiry = (String) obj.get("expiry");
        JSONArray contactsObj = (JSONArray) obj.get("contacts");
        obj.remove("path");
        obj.remove("expiry");
        obj.remove("contacts");

        int size = path.getComponents().size();
        if (path.getComponents().isEmpty()) throw new InvalidConfigException();

        List<ValueContact> contacts = new ArrayList<>();
        for (Object entryObj : contactsObj) {
            JSONObject entry = (JSONObject) entryObj;
            PathName contactPath = new PathName((String) entry.get("path"));
            String host = (String) entry.get("host");
            InetAddress address = InetAddress.getByName(host);
            int port = Math.toIntExact((Long) entry.get("port"));
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            contacts.add(new ValueContact(contactPath, socketAddress));
        }

        root = new ZMI();
        zmi = root;

        for (int i = 0; i < size; ++i) {
            zmi.getAttributes().add("level", new ValueInt((long) i));
            zmi.getAttributes().add("name",
                    i == 0 ? new ValueString(null) : new ValueString(path.getComponents().get(i - 1)));
            zmi.getAttributes().add("owner", new ValueString(path.getName()));
            zmi.getAttributes().add("timestamp", new ValueTime(timestamp));
            zmi.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
            zmi.getAttributes().add("cardinality", new ValueInt(0L));
            zmi = new ZMI(zmi);
        }

        zmi.getAttributes().add("level", new ValueInt((long) size));
        zmi.getAttributes().add("name", new ValueString(path.getSingletonName()));
        zmi.getAttributes().add("owner", new ValueString(path.getName()));
        zmi.getAttributes().add("timestamp", new ValueTime(timestamp));
        zmi.getAttributes().add("creation", new ValueTime(timestamp));
        zmi.getAttributes().add("expiry", new ValueDuration(expiry));
        zmi.getAttributes().add("cardinality", new ValueInt(1L));
        zmi.getAttributes().add("contacts", new ValueSet(new HashSet<>(contacts), TypePrimitive.CONTACT));

        obj.forEach((key, value) -> {
            Value modelValue;
            if (value == null) {
                modelValue = ValueNull.getInstance();
            }
            else if (value instanceof List) {
                modelValue = new ValueList(TypePrimitive.STRING);
                for (String elem : (List<String>) value) {
                    ((ValueList)modelValue).add(new ValueString(elem));
                }
            }
            else if (value instanceof Boolean) {
                modelValue = new ValueBoolean((Boolean) value);
            }
            else if (value instanceof Long) {
                modelValue = new ValueInt((Long) value);
            }
            else if (value instanceof Double) {
                modelValue = new ValueDouble((Double) value);
            }
            else {
                modelValue = new ValueString((String) value);
            }
            zmi.getAttributes().add((String) key, modelValue);
        });
    }

    static ZMI getRoot() {
        return root;
    }

    static ZMI getZone() {
        return zmi;
    }
}
