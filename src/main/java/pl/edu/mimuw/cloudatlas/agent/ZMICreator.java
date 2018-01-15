package pl.edu.mimuw.cloudatlas.agent;

import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.gossip.GossipConfig;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;

public class ZMICreator {
    @Getter private static ZMI root, zone;
    @Getter private static String host;
    @Getter private static int port;
    @Getter private static GossipConfig gossipConfig = new GossipConfig();

    private static JSONObject readConfig(String file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(new FileReader(file));
    }

    static void createHierarchy(String file) throws IOException, ParseException, InvalidConfigException, java.text.ParseException {
        Object tmpObj = null;
        JSONObject obj = readConfig(file);
        Long timestamp = System.currentTimeMillis() / 1000L;
        PathName path = new PathName((String) obj.get("path"));
        JSONObject gossip = (JSONObject) obj.get("gossip");
        String expiry = (String) obj.get("expiry");
        JSONArray contactsObj = (JSONArray) obj.get("contacts");
        obj.remove("path");
        obj.remove("expiry");
        obj.remove("contacts");
        obj.remove("gossip");

        int size = path.getComponents().size();
        if (path.getComponents().isEmpty()) throw new InvalidConfigException();

        tmpObj = obj.get("gossip_timeout");
        gossipConfig.setGossipTimeout(tmpObj == null ? Duration.ofSeconds(5) : Duration.ofSeconds((Long) tmpObj));
        tmpObj = obj.get("update_timeout");
        gossipConfig.setUpdateTimeout(tmpObj == null ? Duration.ofSeconds(5) : Duration.ofSeconds((Long) tmpObj));
        gossipConfig.setStrategy((String) gossip.get("strategy"));
        gossipConfig.setLevels(((Long) gossip.get("levels")).intValue());
        gossipConfig.setSwitches(((Long) gossip.get("switches")).intValue());
        gossipConfig.setMaxContacts(((Long) gossip.get("max_contacts")).intValue());
        gossipConfig.setPort(((Long) gossip.get("port")).intValue());
        gossipConfig.setPath(path);


        List<ValueContact> contacts = new ArrayList<>();
        for (Object entryObj : contactsObj) {
            JSONObject entry = (JSONObject) entryObj;
            PathName contactPath = new PathName((String) entry.get("path"));
            String contactHost = (String) entry.get("host");
            InetAddress address = InetAddress.getByName(contactHost);
            int contactPort = Math.toIntExact((Long) entry.get("port"));
            InetSocketAddress socketAddress = new InetSocketAddress(address, contactPort);
            contacts.add(new ValueContact(contactPath, socketAddress));
            if (contactPath.equals(path)) {
                host = contactHost;
                port = contactPort;
            }
        }

        root = new ZMI();
        zone = root;

        for (int i = 0; i < size; ++i) {
            zone.getAttributes().add("level", new ValueInt((long) i));
            zone.getAttributes().add("name",
                    i == 0 ? new ValueString(null) : new ValueString(path.getComponents().get(i - 1)));
            zone.getAttributes().add("owner", new ValueString(path.getName()));
            zone.getAttributes().add("timestamp", new ValueTime(timestamp));
            zone.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
            zone.getAttributes().add("cardinality", new ValueInt(0L));
            zone = new ZMI(zone);
        }

        zone.getAttributes().add("level", new ValueInt((long) size));
        zone.getAttributes().add("name", new ValueString(path.getSingletonName()));
        zone.getAttributes().add("owner", new ValueString(path.getName()));
        zone.getAttributes().add("timestamp", new ValueTime(timestamp));
        zone.getAttributes().add("creation", new ValueTime(timestamp));
        zone.getAttributes().add("expiry", expiry == null ? ValueNull.getInstance() : new ValueDuration(expiry));
        zone.getAttributes().add("cardinality", new ValueInt(1L));
        zone.getAttributes().add("contacts", new ValueSet(new HashSet<>(contacts), TypePrimitive.CONTACT));

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
            zone.getAttributes().add((String) key, modelValue);
        });
    }
}
