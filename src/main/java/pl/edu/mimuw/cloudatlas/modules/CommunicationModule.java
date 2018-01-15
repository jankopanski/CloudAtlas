package pl.edu.mimuw.cloudatlas.modules;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class CommunicationModule extends Module {
    private static final CommunicationModule INSTANCE = new CommunicationModule();
    private static String NodeName = null;
    private final int udpMaxLen = 256;
    private Module dstModule = null;
    int sPort;
    DatagramSocket rcvSocket;
    DatagramSocket sndSocket;
    int msgCounter = 0;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public void setNodeNameAndPorts(String name, int sndPort, int rcvPort) {
        NodeName = name;
        try {
            rcvSocket = new DatagramSocket(rcvPort);
            sPort = sndPort;
            sndSocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
           receiverFunction();
        }).start();
        new Thread(() -> {
           runModule();
        }).start();
    }

    public void setDstModule(Module dModule) {
        dstModule = dModule;
    }

    public static CommunicationModule getInstance() {
        return INSTANCE;
    }

    private class udpWaitingRoom {
        int msgCount;
        udpMessage[] msges;

        udpWaitingRoom(int cnt) {
            msges = new udpMessage[cnt];
            msgCount = 0;
        }
    }

    void completeMessage(udpWaitingRoom room, InetAddress addr) {
        int i = 0;
        long avgSend = 0, avgRcv = 0;
        for (udpMessage msg : room.msges) {
            i += msg.data.length;
            avgSend += msg.timestamps[0];
            avgRcv += msg.timestamps[1];
        }
        byte[] data = new byte[i];

        i = 0;
        for (udpMessage msg : room.msges) {
            System.arraycopy(msg.data, 0, data, i, msg.data.length);
            i += msg.data.length;
        }
        avgSend /= room.msgCount;
        avgRcv /= room.msgCount;
        CommunicationMessage completeMessage = new CommunicationMessage(this, addr, data);
        i = 0;

        completeMessage.sndTimestamp = avgSend;
        completeMessage.rcvTimestamp = avgRcv;
        dstModule.sendMessage(completeMessage);
    }

    void receiverFunction() {
            HashMap<String, HashMap<Integer, udpWaitingRoom>> fragments = new HashMap<>();
            byte[] rcvData = new byte[udpMaxLen * 2];
            DatagramPacket rcvPckt = new DatagramPacket(rcvData, rcvData.length);
            while (true) {
                try {
                    rcvSocket.receive(rcvPckt);
                    ByteArrayInputStream bstream = new ByteArrayInputStream(rcvPckt.getData());
                    ObjectInputStream os;
                    os = new ObjectInputStream(bstream);
                    udpMessage msg = (udpMessage) os.readObject();
                    msg.timestamps[1] = System.currentTimeMillis();
                    os.close();

                    fragments.compute(msg.nodeName, (name, currVal) -> {
                        HashMap<Integer, udpWaitingRoom> nodeMap;
                        if (currVal == null) {
                            nodeMap = new HashMap<>();
                        } else {
                            nodeMap = currVal;
                        }
                        nodeMap.compute(msg.number, (number, currWaitRoom) -> {
                            udpWaitingRoom room;
                            if (currWaitRoom == null) {
                                room = new udpWaitingRoom(msg.parts);
                            }
                            else {
                                room = currWaitRoom;
                            }
                            room.msges[msg.counter] = msg;
                            room.msgCount++;
                            if (room.msgCount == room.msges.length) {
                                completeMessage(room, rcvPckt.getAddress());
                                return null;
                            }
                            return room;
                        });
                        return nodeMap;
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
    }

    void passMsg(CommunicationMessage msg) {
        udpMessage uMsg = new udpMessage();
        uMsg.nodeName = NodeName;
        uMsg.number = msgCounter++;
        uMsg.parts = msg.data.length / udpMaxLen + 1;
        uMsg.counter = 0;
        uMsg.timestamps = new long[2];

        for (int i = 0; i < msg.data.length; i += udpMaxLen) {
            int len = msg.data.length - i < udpMaxLen ? msg.data.length - i : udpMaxLen;
            uMsg.data = new byte[len];
            System.arraycopy(msg.data, i, uMsg.data, 0, len);
            try {
                ObjectOutputStream os = new ObjectOutputStream(stream);
                uMsg.timestamps[0] = System.currentTimeMillis();
                os.writeObject(uMsg);
                byte[] sndArr = stream.toByteArray();
                DatagramPacket sndPkt = new DatagramPacket(sndArr, sndArr.length, msg.IP, sPort);
                sndSocket.send(sndPkt);
                sleep(5);
                stream.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            uMsg.counter++;
        }

    }

    @Override
    public void handleMsg(Message msg) {
        switch (msg.type) {
            case Communication:
                passMsg((CommunicationMessage) msg);
                break;
            default:
                super.handleMsg(msg);
        }
    }
}
