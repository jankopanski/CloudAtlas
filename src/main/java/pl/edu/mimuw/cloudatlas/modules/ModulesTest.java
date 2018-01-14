package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.agent.AgentComputer;
import pl.edu.mimuw.cloudatlas.modules.gossip.GossipModule;
import pl.edu.mimuw.cloudatlas.modules.gossip.GossipPackage;
import pl.edu.mimuw.cloudatlas.modules.gossip.GossipType;
import pl.edu.mimuw.cloudatlas.modules.gossip.RandomStrategy;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;


import static java.lang.Thread.sleep;

public class ModulesTest {
    static ZMI zone, root;

    public static void main(String args[]) {
        //timerSimpleTest();
        //commSimpleTest();
        gossipTest();
    }


    public static void timerSimpleTest() {
        for (int i = 0; i < 5; ++i) {
            TimerModule timer = TimerModule.getInstance();
            TimerMessage msg = new TimerMessage(null, LocalTime.now().plusSeconds(5), () -> {System.out.println("hello world");});
            timer.sendMessage(msg);
            try {sleep(5000);} catch (InterruptedException e) {};
            System.out.println("Hello time");
        }
        System.out.println("timer simple test done");
    }

    private static class CommTestMdl extends Module {
       @Override
       public void handleMsg(Message msg) {
           if (msg.type == msgType.Communication) {
               CommunicationMessage cMsg = (CommunicationMessage) msg;
               System.out.println(new String(cMsg.data));
               System.out.println(cMsg.data.length);
               System.out.println(cMsg.IP);
               try {sleep(2000);} catch (Exception e) {e.printStackTrace();}
               Module tmp = cMsg.destination;
               cMsg.destination = cMsg.source;
               cMsg.source = tmp;
               tmp.sendMessage(cMsg);
           }
           else {
               System.out.println("Wrong message type");
           }
       }

       CommTestMdl() {
           new Thread(() -> {this.runModule();}).start();
       }
    }

    public static void gossipTest() {
        try {
            createTestHierarchy();
        } catch (Exception e) {}
        //AgentComputer.initialize(zone);

        GossipModule gm = GossipModule.getInstance();
        gm.initialize(Duration.ofSeconds(10), Duration.ofSeconds(10), new RandomStrategy(2, 1), zone, new PathName("/uw/violet07"), 10);
        CommunicationModule cm = CommunicationModule.getInstance();
        cm.setDstModule(gm);
        cm.setNodeNameAndPorts("a", 1234, 1234);
        gm.startModule();

        /*GossipPackage gp = new GossipPackage();
        gp.nodeName = "idk";
        //TODO add info
        gp.type = GossipType.INITIAL;
        gp.info = null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] data = null;
        try {
            ObjectOutputStream os = new ObjectOutputStream(stream);
            os.writeObject(gp);
            data = stream.toByteArray();
            os.close();

        } catch (Exception e) {e.printStackTrace();}

        CommunicationMessage msg = new CommunicationMessage(CommunicationModule.getInstance(), null, data);


        gm.sendMessage(msg);*/
    }

    public static void commSimpleTest() {
        CommunicationModule m1, m2;
        CommTestMdl tMdl = new CommTestMdl();
        m1 = CommunicationModule.getInstance();//new CommunicationModule();
        m1.setNodeNameAndPorts("a", 1234, 5678);
        m1.setDstModule(tMdl);
        //m2 = new CommunicationModule();
        //m2.setNodeNameAndPorts("a", 5678, 1234);
        //m2.setDstModule(tMdl);
        CommunicationMessage msg1 = new CommunicationMessage();
        CommunicationMessage msg2 = new CommunicationMessage();
        msg1.destination = m1;
        msg2.destination = m1;
        msg1.source = m1;
        msg2.source = m1;
        msg1.type = msgType.Communication;
        msg2.type = msgType.Communication;
        try {
            msg1.IP = InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, 0, (byte)80});
            msg2.IP = InetAddress.getByAddress(new byte[]{(byte)192, (byte)168, 0, (byte)80});
            msg1.data = "Hello world".getBytes();
            m1.sendMessage(msg1);
            String longMsg = "aaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababaababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbabababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababababbababababababababab";
            msg2.data = longMsg.getBytes();
            System.out.println(longMsg.length());
            m1.sendMessage(msg2);
            m1.sendMessage(msg1);
        } catch (Exception e) {}
    }
    private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
            throws UnknownHostException {
        return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
                ip1, ip2, ip3, ip4
        }));
    }
    private static void createTestHierarchy() throws ParseException, UnknownHostException {
        ValueContact violet07Contact = createContact("/uw/violet07", (byte)192, (byte)168, (byte)0, (byte)206);
        ValueContact khaki31Contact = createContact("/uw/khaki31", (byte)10, (byte)1, (byte)1, (byte)39);
        ValueContact whatever01Contact = createContact("/pjwstk/whatever01", (byte)192, (byte)168, (byte)0, (byte)80);

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
                violet07Contact,
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


        ZMI whatever01 = new ZMI(pjwstk);
        pjwstk.addSon(whatever01);
        whatever01.getAttributes().add("level", new ValueInt(2l));
        whatever01.getAttributes().add("name", new ValueString("whatever01"));
        whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
        whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
        list = Arrays.asList(new Value[] {
                violet07Contact,
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
    }
}
