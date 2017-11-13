package pl.edu.mimuw.cloudatlas.client;

import lombok.Data;

@Data
public class ClientRequest {
    private String agent;
    private String type;
    private String query;

}
