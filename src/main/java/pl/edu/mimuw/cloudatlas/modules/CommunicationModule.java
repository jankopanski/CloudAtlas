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

    void setNodeNameAndPorts(String name, int sndPort, int rcvPort) {
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

    void setDstModule(Module dModule) {
        dstModule = dModule;
    }

    static CommunicationModule getInstance() {
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
         StringBuilder sb = new StringBuilder();

        for (udpMessage msg : room.msges)
            sb.append(msg.data);

        CommunicationMessage completeMessage = new CommunicationMessage();
        completeMessage.type = msgType.Communication;
        completeMessage.data = sb.toString();
        completeMessage.IP = addr;
        completeMessage.source = this;
        completeMessage.destination = dstModule;
        dstModule.sendMessage(completeMessage);
    }

    void receiverFunction() {
            HashMap<String, HashMap<Integer, udpWaitingRoom>> fragments = new HashMap<>();
            byte[] rcvData = new byte[512];
            DatagramPacket rcvPckt = new DatagramPacket(rcvData, rcvData.length);
            while (true) {
                try {
                    rcvSocket.receive(rcvPckt);
                    ByteArrayInputStream bstream = new ByteArrayInputStream(rcvPckt.getData());
                    ObjectInputStream os;
                    os = new ObjectInputStream(bstream);
                    final udpMessage msg = (udpMessage) os.readObject();
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
        uMsg.parts = msg.data.length() / udpMaxLen + 1;
        uMsg.counter = 0;

        for (int i = 0; i < msg.data.length(); i += udpMaxLen) {
            uMsg.data = msg.data.substring(i, (i + udpMaxLen <= msg.data.length() ? i + udpMaxLen : msg.data.length()));
            try {
                ObjectOutputStream os = new ObjectOutputStream(stream);
                os.writeObject(uMsg);
                byte[] sndArr = stream.toByteArray();
                DatagramPacket sndPkt = new DatagramPacket(sndArr, sndArr.length, msg.IP, sPort);
                sndSocket.send(sndPkt);

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
