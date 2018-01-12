package pl.edu.mimuw.cloudatlas.modules;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

public class CommunicationModule extends Module {
    //private static final CommunicationModule INSTANCE = new CommunicationModule();
    private static String NodeName = null;
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

    //static CommunicationModule getInstance() {
    //    return INSTANCE;
    //}

//    private static final int rcvPort = 1234;



    private class udpWaitingRoom {
        int msgCount;
        udpMessage[] msges;

        udpWaitingRoom(int cnt) {
            msges = new udpMessage[cnt];
            msgCount = 0;
        }
    }



    void completeMessage(udpWaitingRoom room) {
         StringBuilder sb = new StringBuilder();

        for (udpMessage msg : room.msges)
            sb.append(msg.data);

        CommunicationMessage completeMessage = new CommunicationMessage();
        completeMessage.data = sb.toString();
        //completeMessage.source = CommunicationModule.getInstance();
        completeMessage.destination = null; //TODO destination and send message
        System.out.println(completeMessage.data);
    }

    void receiverFunction() {
        byte[] rcvData = new byte[512];
            DatagramPacket rcvPckt = new DatagramPacket(rcvData, rcvData.length);
            //udpMessage msg;
            HashMap<String, HashMap<Integer, udpWaitingRoom>> fragments = new HashMap<>();

            while (true) {
                try {
                    rcvSocket.receive(rcvPckt);
                    System.out.println("got msg");
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
                                completeMessage(room);
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
        System.out.println("pass");
        udpMessage uMsg = new udpMessage();
        uMsg.nodeName = NodeName;
        uMsg.number = msgCounter++;
        uMsg.parts = msg.data.length() / 256 + 1;
        uMsg.counter = 0;

        for (int i = 0; i < msg.data.length(); i += 256) {
            uMsg.data = msg.data.substring(i, (i + 255 < msg.data.length() ? i + 255 : msg.data.length()));
            try {
                ObjectOutputStream os = new ObjectOutputStream(stream);
                os.writeObject(uMsg);
                byte[] sndArr = stream.toByteArray();
                DatagramPacket sndPkt = new DatagramPacket(sndArr, sndArr.length, msg.destIP, sPort);
                sndSocket.send(sndPkt);
                System.out.println("msg sent");
            } catch (Exception e) {
                e.printStackTrace();
            }
            uMsg.counter++;
        }

    }

    @Override
    void handleMsg(Message msg) {
        switch (msg.type) {
            case Comunication:
                passMsg((CommunicationMessage) msg);
                break;
            default:
                super.handleMsg(msg);
        }
    }
}
