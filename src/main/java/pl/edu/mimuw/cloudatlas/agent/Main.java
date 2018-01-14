package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.RMIModule;
import pl.edu.mimuw.cloudatlas.security.KeyReader;
import pl.edu.mimuw.cloudatlas.security.KeyReaderException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Main {
    private static ZMI root, zone;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: Main <public_key> <config_file> <registry host> <registry port>");
            System.exit(1);
        }

        try {
            ZMICreator.createHierarchy(args[1]);
            root = ZMICreator.getRoot();
            zone = ZMICreator.getZone();
        } catch (IOException | org.json.simple.parser.ParseException | InvalidConfigException | ParseException e) {
            e.printStackTrace();
        }

//        try {
//            createTestHierarchy();
//        } catch (ParseException | UnknownHostException e) {
//            System.err.println("createTestHierarchy error");
//            e.printStackTrace();
//            System.exit(1);
//        }

        String publicKeyFile = args[0];
        PublicKey publicKey = null;
        try {
            publicKey = KeyReader.readPublicKey(publicKeyFile);
        } catch (KeyReaderException e) {
            System.err.println("Public key read error");
            e.printStackTrace();
            System.exit(1);
        }

        AgentComputer agent = AgentComputer.getInstance();
        try {
            AgentComputer.initialize(zone, publicKey);
        }
        catch (Exception e) {
            System.err.println("Agent initialization error");
            e.printStackTrace();
            System.exit(1);
        }
        RMIModule rmi = RMIModule.getInstance();


        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    updateQueries(root);
                    try {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e) {

                    }
                }
            }
            private void updateQueries(ZMI zmi) {
                for (ZMI son : zmi.getSons())
                    updateQueries(son);
                for (Map.Entry<Attribute, Value> e : zmi.getAttributes()) {
                    if (Attribute.isQuery(e.getKey())) {
                        ValueCert cert = (ValueCert) e.getValue();
                        agent.executeQueries(zmi, cert.getQuery());
                    }
                }
            }

        };

        new Thread(r).start();

        AgentServer server = new AgentServer(rmi, args[2], Integer.parseInt(args[3]));
        server.run();
    }


    /*private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
                ip1, ip2, ip3, ip4
        }));
    }

    private static void createTestHierarchy() throws ParseException, UnknownHostException {
        ValueContact violet07Contact = createContact("/uw/violet07", (byte)10, (byte)1, (byte)1, (byte)10);
        ValueContact khaki13Contact = createContact("/uw/khaki13", (byte)10, (byte)1, (byte)1, (byte)38);
        ValueContact khaki31Contact = createContact("/uw/khaki31", (byte)10, (byte)1, (byte)1, (byte)39);
        ValueContact whatever01Contact = createContact("/uw/whatever01", (byte)82, (byte)111, (byte)52, (byte)56);
        ValueContact whatever02Contact = createContact("/uw/whatever02", (byte)82, (byte)111, (byte)52, (byte)57);

        List<Value> list;

        root = new ZMI();
        root.getAttributes().add("level", new ValueInt(0l));
        root.getAttributes().add("name", new ValueString(null));
        root.getAttributes().add("owner", new ValueString("/uw/violet07"));
        root.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:10:17.342"));
        root.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
        root.getAttributes().add("cardinality", new ValueInt(0l));

        ZMI uw = new ZMI(root);
        root.addSon(uw);
        uw.getAttributes().add("level", new ValueInt(1l));
        uw.getAttributes().add("name", new ValueString("uw"));
        uw.getAttributes().add("owner", new ValueString("/uw/violet07"));
        uw.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
        uw.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
        uw.getAttributes().add("cardinality", new ValueInt(0l));

        ZMI pjwstk = new ZMI(root);
        root.addSon(pjwstk);
        pjwstk.getAttributes().add("level", new ValueInt(1l));
        pjwstk.getAttributes().add("name", new ValueString("pjwstk"));
        pjwstk.getAttributes().add("owner", new ValueString("/pjwstk/whatever01"));
        pjwstk.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
        pjwstk.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
        pjwstk.getAttributes().add("cardinality", new ValueInt(0l));

        ZMI violet07 = new ZMI(uw);
        zone = violet07;
        uw.addSon(violet07);
        violet07.getAttributes().add("level", new ValueInt(2l));
        violet07.getAttributes().add("name", new ValueString("violet07"));
        violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
        violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
        list = Arrays.asList(new Value[] {
                khaki31Contact, whatever01Contact
        });
        violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        violet07.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[] {
                violet07Contact,
        });
        violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
        violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
        violet07.getAttributes().add("num_cores", new ValueInt(3l));
        violet07.getAttributes().add("has_ups", new ValueBoolean(null));
        list = Arrays.asList(new Value[] {
                new ValueString("tola"), new ValueString("tosia"),
        });
        violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        violet07.getAttributes().add("expiry", new ValueDuration(13l, 12l, 0l, 0l, 0l));

        ZMI khaki31 = new ZMI(uw);
        uw.addSon(khaki31);
        khaki31.getAttributes().add("level", new ValueInt(2l));
        khaki31.getAttributes().add("name", new ValueString("khaki31"));
        khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
        khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
        list = Arrays.asList(new Value[] {
                violet07Contact, whatever02Contact,
        });
        khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki31.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[] {
                khaki31Contact
        });
        khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
        khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
        khaki31.getAttributes().add("num_cores", new ValueInt(3l));
        khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
        list = Arrays.asList(new Value[] {
                new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
        });
        khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        khaki31.getAttributes().add("expiry", new ValueDuration(-13l, -11l, 0l, 0l, 0l));

        ZMI khaki13 = new ZMI(uw);
        uw.addSon(khaki13);
        khaki13.getAttributes().add("level", new ValueInt(2l));
        khaki13.getAttributes().add("name", new ValueString("khaki13"));
        khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
        khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
        list = Arrays.asList(new Value[] {});
        khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki13.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[] {
                khaki13Contact,
        });
        khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        khaki13.getAttributes().add("creation", new ValueTime((Long)null));
        khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
        khaki13.getAttributes().add("num_cores", new ValueInt(null));
        khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
        list = Arrays.asList(new Value[] {});
        khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
        khaki13.getAttributes().add("expiry", new ValueDuration((Long)null));

        ZMI whatever01 = new ZMI(pjwstk);
        pjwstk.addSon(whatever01);
        whatever01.getAttributes().add("level", new ValueInt(2l));
        whatever01.getAttributes().add("name", new ValueString("whatever01"));
        whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
        whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
        list = Arrays.asList(new Value[] {
                violet07Contact, whatever02Contact,
        });
        whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever01.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[] {
                whatever01Contact,
        });
        whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
        whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
        whatever01.getAttributes().add("num_cores", new ValueInt(7l));
        list = Arrays.asList(new Value[] {
                new ValueString("rewrite")
        });
        whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

        ZMI whatever02 = new ZMI(pjwstk);
        pjwstk.addSon(whatever02);
        whatever02.getAttributes().add("level", new ValueInt(2l));
        whatever02.getAttributes().add("name", new ValueString("whatever02"));
        whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
        whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
        list = Arrays.asList(new Value[] {
                khaki31Contact, whatever01Contact,
        });
        whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever02.getAttributes().add("cardinality", new ValueInt(1l));
        list = Arrays.asList(new Value[] {
                whatever02Contact,
        });
        whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
        whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
        whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
        whatever02.getAttributes().add("num_cores", new ValueInt(13l));
        list = Arrays.asList(new Value[] {
                new ValueString("odbc")
        });
        whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
    }*/
}
