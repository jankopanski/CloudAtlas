package pl.edu.mimuw.cloudatlas.modules;

import pl.edu.mimuw.cloudatlas.agent.AgentMethod;

public class RMIReturnMessage extends Message {
    public AgentMethod method;
    public Object returnValue;
    public msgType type = msgType.RMICallback;
}
