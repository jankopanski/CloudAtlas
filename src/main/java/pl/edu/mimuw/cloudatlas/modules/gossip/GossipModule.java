package pl.edu.mimuw.cloudatlas.modules.gossip;

import pl.edu.mimuw.cloudatlas.agent.AgentComputer;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.modules.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
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
        ZMI zone = agentComputer.getZone();
        int level = strategy.choseLevel();
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
            int i = 0;
            ZMI tmp = zone;
            while (tmp != null) { //TODO check if not off by one
                tmp = tmp.getFather();
                i++;
            }

            gossipsInProgress.put(chosenCont.getName().getName(), zone);

            GossipPackage gp = new GossipPackage();

            gp.info = null;//new AttributesMap[i];
            gp.freshness = new ValueTime[i];
            gp.type = GossipType.FRESHNESS;
            i = 0;
            zone = chosenSibling;
            while (zone.getFather() != null) {
                //gp.info[i++] = zone.getAttributes();
                gp.freshness[i++] = (ValueTime) zone.getAttributes().getOrNull("timestamp");
                zone = zone.getFather();
            }
            try {
                ObjectOutputStream os = new ObjectOutputStream(stream);
                os.writeObject(gp);
                String data = os.toString();
                os.close();
                System.out.println("Gossip with:" + chosenCont.getName().getName());
                //CommunicationMessage msg = new CommunicationMessage(this, chosenCont.getAddress(), data);
                //CommunicationModule.getInstance().sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

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
                // TODO gossip event driven continuation
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
