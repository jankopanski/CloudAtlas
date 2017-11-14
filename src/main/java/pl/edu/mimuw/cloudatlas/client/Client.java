package pl.edu.mimuw.cloudatlas.client;

import static spark.Spark.get;
import static spark.Spark.post;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import lombok.Data;
import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.fetcher.Fetcher;
import spark.Spark;

import java.io.IOException;
import java.io.StringWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;


//Duzo bsu bo to z tutoriala, zostawione, żeby było widać jak to napisać
public class Client {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final String AGENT_HOST = "localhost";

    interface Validable {
        boolean isValid();
    }

    @Data
    static class NewPostPayload {
        private String title;
        private List categories = new LinkedList<>();
        private String content;

        public boolean isValid() {
            return title != null && !title.isEmpty() && !categories.isEmpty();
        }
    }

    // In a real application you may want to use a DB, for this example we just store the posts in memory
    public static class Model {
        private int nextId = 1;
        private Map posts = new HashMap<>();

        @Data
        class Post {
            private int id;
            private String title;
            private List categories;
            private String content;
        }

        public int createPost(String title, String content, List categories){
            int id = nextId++;
            Post post = new Post();
            post.setId(id);
            post.setTitle(title);
            post.setContent(content);
            post.setCategories(categories);
            posts.put(id, post);
            return id;
        }

        public List getAllPosts(){
            return null;//posts.keySet().stream().sorted().map((id) -> posts.get(id)).collect(Collectors.toList());
        }
    }

    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e){
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }

    public static void main( String[] args) {
        try {
            Agent agent = initializeAgent();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Model model = new Model();

        Spark.staticFileLocation("/public");
        // insert a post (using HTTP post method)
        post("/posts", (request, response) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                NewPostPayload creation = mapper.readValue(request.body(), NewPostPayload.class);
                if (!creation.isValid()) {
                    response.status(HTTP_BAD_REQUEST);
                    return "";
                }
                int id = model.createPost(creation.getTitle(), creation.getContent(), creation.getCategories());
                response.status(200);
                response.type("application/json");
                return id;
            } catch (JsonParseException jpe) {
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
        });

        post("/connect", ((request, response) -> {
            String aname = request.queryMap("aname").value();
            response.cookie("aname", aname);
            response.status(200);
            response.type("text/html");
            return "<html><head><script>document.location.href = '/agent.html'</script></head><body></body></html>";
        }));

        post("/request", "application/json", (((request, response) -> {
            String json = request.body();
            Gson g = new Gson();
            ClientRequest req = g.fromJson(json, ClientRequest.class);
            System.out.println(req.getQuery());
            //TODO tutaj użyć request do rmi z agentem
            agent.installQuery();

            response.status(200);
            response.type("text/html");
            return "";
        })));
    }

    private static Agent initializeAgent() throws RemoteException, NotBoundException {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        Registry registry = LocateRegistry.getRegistry(AGENT_HOST);
        return (Agent) registry.lookup("Fetcher");
    }
}

