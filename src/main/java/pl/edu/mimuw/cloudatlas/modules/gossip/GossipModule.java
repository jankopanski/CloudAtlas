package pl.edu.mimuw.cloudatlas.modules.gossip;

import org.eclipse.jetty.util.ArrayQueue;
import pl.edu.mimuw.cloudatlas.agent.AgentComputer;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class GossipModule extends Module {
    private static final GossipModule INSTANCE = new GossipModule();
    private static final AgentComputer agentComputer = AgentComputer.getInstance();
    private TimerMessage recomputeRequest;
    private TimerMessage doGossipRequest;
    private TimerResponse recomputeMessge;
    private TimerResponse doGossipMessage;
    private Duration recomputePeriod;
    private Duration gossipPeriod;
    private Random rng = new Random();
    private ByteArrayOutputStream stream = new ByteArrayOutputStream();

    private HashMap<String, ZMI> gossipsInProgress = new HashMap<>();

    private HashMap<String, LinkedList<ValueContact>> myContacts;
    private PathName myPathName;
    private int maxLevelContacts;

    private GossipStrategy strategy;


    GossipModule() {
        recomputeMessge = new TimerResponse(this, 1);
        doGossipMessage = new TimerResponse(this, 2);
        recomputeRequest = new TimerMessage(this, null, () -> {
           GossipModule.getInstance().sendMessage(recomputeMessge);
        });
        doGossipRequest = new TimerMessage(this, null, () -> {
           GossipModule.getInstance().sendMessage(doGossipMessage);
        });
    }

    public static GossipModule getInstance() {
        return INSTANCE;
    }

    public void initialize(Duration rp, Duration gp, GossipStrategy strat, ZMI zone, PathName myPath, int maxContacts) {
        recomputePeriod = rp;
        gossipPeriod = gp;
        strategy = strat;
        myContacts = new HashMap<>();
        myPathName = myPath;
        maxLevelContacts = maxContacts;
        updateContactInfo(zone);
    }

    public void startModule() {
        new Thread(() -> {
            recomputeRequest.setFireTime(LocalTime.now().plus(recomputePeriod));
            doGossipRequest.setFireTime(LocalTime.now().plus(gossipPeriod));
            TimerModule.getInstance().sendMessage(recomputeRequest);
            TimerModule.getInstance().sendMessage(doGossipRequest);

            runModule();
        }).start();
    }

    private void updateContactInfo(ZMI zmi) {
        ValueSet conts = (ValueSet) zmi.getAttributes().getOrNull("contacts");
        if (conts != null) {
            Set<Value> contacts = conts.getValue();
            for (Value v: contacts) {
                ValueContact contact = (ValueContact) v;
                if (!myPathName.equals(((ValueContact) v).getName())) {
                    Iterator<String> myCompo, contCompo;
                    myCompo = myPathName.getComponents().iterator();
                    contCompo = contact.getName().getComponents().iterator();
                    int i = 0;
                    String cStr;
                    do {
                        cStr = contCompo.next();
                    } while (myCompo.next().equals(cStr));
                    myContacts.compute(cStr, (key, val) -> {
                        if (val == null) {
                            LinkedList<ValueContact> ret = new LinkedList<>();
                            ret.add(contact);
                            return ret;
                        }
                        else {
                            val.add(contact);
                            if (val.size() > maxLevelContacts)
                                val.remove(rng.nextInt(maxLevelContacts));
                            return val;
                        }
                    });
                }
            }
        }

    }

    private void doGossip() {
        System.out.println("Gossip");
        ZMI zone = agentComputer.getZone();
        int level = 0;//strategy.choseLevel();
        for (int i = 0; i <= level; ++i) {
            zone = zone.getFather();
        }
        List<ZMI> zoneZMIS = new ArrayList<>();
        zoneZMIS.addAll(zone.getSons());
        ValueContact chosenCont = null;
        ZMI chosenSibling = null;
        while (!zoneZMIS.isEmpty()) {
            chosenSibling = zoneZMIS.get(rng.nextInt(zoneZMIS.size()));
            ValueString siblingNameValue = (ValueString) chosenSibling.getAttributes().getOrNull("name");
            if (siblingNameValue != null && !siblingNameValue.getValue().equals(myPathName.getSingletonName())) {
                LinkedList<ValueContact> contacts = myContacts.get(siblingNameValue.getValue());
                if (contacts == null || contacts.isEmpty()) {
                    zoneZMIS.remove(chosenSibling);
                } else {
                    chosenCont = contacts.get(rng.nextInt(contacts.size()));
                    break;
                }

            }
            else
                zoneZMIS.remove(chosenSibling);
        }
        if (chosenCont == null) {
            //Use fallback
        }
        else {
            gossipsInProgress.put(chosenCont.getName().getSingletonName(), chosenSibling);

            GossipPackage gp = new GossipPackage();
            int i = 0;
            while(chosenSibling != null) {
                chosenSibling = chosenSibling.getFather();
                ++i;
            }
            gp.info = null;
            gp.zonesCnt = i;
            gp.nodeName = myPathName.getSingletonName();
            gp.type = GossipType.INITIAL;

            sendGossipPackage(gp, chosenCont.getAddress());
            System.out.println("Gossip with:" + chosenCont.getName().getName());

        }

    }

    private void fillGossipPackage(ZMI zone, GossipPackage gp) {

        gp.info = new AttributesMap[gp.zonesCnt];
        gp.nodeName = myPathName.getSingletonName();
        gp.type = GossipType.RETURN;

        int i = 0;
        while (zone.getFather() != null) {
            gp.info[i++] = zone.getAttributes();
            zone = zone.getFather();
        }
    }

    private void sendGossipPackage(GossipPackage gp, InetAddress addr) {
        try {
                stream.reset();
                ObjectOutputStream os = new ObjectOutputStream(stream);
                os.writeObject(gp);
                byte[] data = stream.toByteArray();
                os.close();
                CommunicationMessage msg = new CommunicationMessage(this, addr, data);
                CommunicationModule.getInstance().sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
        }
    }

    private ZMI findZone(GossipPackage gp) {
        ZMI zone = AgentComputer.getInstance().getZone();
        Queue<ZMI> q = new ArrayQueue<>();
        while (zone != null) {
            q.add(zone);
            zone = zone.getFather();
            if (q.size() > gp.zonesCnt + 1)
                q.poll();
        }
        zone = q.peek();
        gossipsInProgress.put(gp.nodeName, zone);
        return zone;
    }

    private ZMI findZoneInProgress(String nodeName) {
        ZMI zone = gossipsInProgress.get(nodeName);
        if (zone != null) {
            gossipsInProgress.remove(nodeName);
        }
        return zone;
    }

    private void compareAndAdoptZMI(ZMI zone, GossipPackage gp, long SndTimestamp, long RcvTimestamp) {
        long rtd = (RcvTimestamp - SndTimestamp) + (gp.previousRcvTimestamp - gp.previousSndTimestamp);
        long dT = SndTimestamp + rtd / 2 - RcvTimestamp;
        for (int i = 0; i < gp.info.length; ++i) {
            AttributesMap fromGossip = gp.info[i];
            if (fromGossip == null)
                continue;
            ValueTime myTs, theirTs;
            myTs = (ValueTime) zone.getAttributes().getOrNull("timestamp");
            theirTs = (ValueTime) fromGossip.getOrNull("timestamp");

            if (myTs == null || theirTs == null) {
                gp.info[i] = null;
                continue;
            }

            String s1 = ((ValueString) fromGossip.getOrNull("name")).getValue();
            String s2 = ((ValueString) zone.getAttributes().getOrNull("name")).getValue();
            System.out.println("my " + s2 + " vs " + s1);
            System.out.println(myTs);
            System.out.println(theirTs);
            if (myTs.isLowerThan(theirTs.subtract(new ValueDuration(dT))).getValue()) {
                zone.setAttributes(fromGossip);
                System.out.println("ZMI adopted: " + ((ValueString) fromGossip.getOrNull("name")).getValue());
                gp.info[i] = null;
            } else {
                gp.info[i] = zone.getAttributes();

            }
            zone = zone.getFather();
        }
    }

    private void fillTimeStamps(GossipPackage gp, CommunicationMessage msg) {
        gp.previousSndTimestamp = msg.sndTimestamp;
        gp.previousRcvTimestamp = msg.rcvTimestamp;
    }

    private void handleGossipRequest(CommunicationMessage msg) {
        ByteArrayInputStream bstream = new ByteArrayInputStream(msg.data);
        GossipPackage gp;
        try {
            ObjectInputStream os = new ObjectInputStream(bstream);
            gp = (GossipPackage) os.readObject();
            os.close();
        } catch (Exception e) {e.printStackTrace(); return;}

        ZMI zone;
        switch (gp.type) {
            case INITIAL:
                zone = findZone(gp);
                if (zone == null)
                    break;
                fillGossipPackage(zone, gp);
                fillTimeStamps(gp, msg);
                sendGossipPackage(gp, msg.IP);
                return;
            case RETURN:
                zone = findZoneInProgress(gp.nodeName);
                if (zone == null)
                    break;
                compareAndAdoptZMI(zone, gp, msg.sndTimestamp, msg.rcvTimestamp);
                gp.type = GossipType.FINAL;
                gp.nodeName = myPathName.getSingletonName();
                fillTimeStamps(gp, msg);
                sendGossipPackage(gp, msg.IP);
                return;
            case FINAL:
                zone = findZoneInProgress(gp.nodeName);
                if (zone == null)
                    break;
                compareAndAdoptZMI(zone, gp, msg.sndTimestamp, msg.rcvTimestamp);
                return;
        }

        System.out.println("no zone found " + gp.nodeName);
    }

    private boolean handleTimerResponse(TimerResponse msg) {
        if (msg.equals(recomputeMessge)) {
            agentComputer.updateQueries();
            recomputeRequest.setFireTime(LocalTime.now().plus(recomputePeriod));
            TimerModule.getInstance().sendMessage(recomputeRequest);
            return true;
        }
        else if (msg.equals(doGossipMessage)) {
            doGossip();
            doGossipRequest.setFireTime(LocalTime.now().plus(gossipPeriod));
            TimerModule.getInstance().sendMessage(doGossipRequest);
            return true;
        }
        return false;
    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.type) {
            case Communication:
                System.out.println("got msg");
                handleGossipRequest((CommunicationMessage) msg);
                break;
            case TimerResponse:
                if (!handleTimerResponse((TimerResponse) msg)) {
                    super.handleMsg(msg);
                }
                break;
            default:
                super.handleMsg(msg);
                break;
        }
    }
}
