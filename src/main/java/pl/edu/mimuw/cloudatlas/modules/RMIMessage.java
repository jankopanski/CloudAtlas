package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.agent.AgentMethod;

import java.util.ArrayList;

public class RMIMessage extends Message {
    public AgentMethod method;
    public ArrayList<Object> params;
    public boolean ret = false;
    public msgType type = msgType.RMICall;
}
