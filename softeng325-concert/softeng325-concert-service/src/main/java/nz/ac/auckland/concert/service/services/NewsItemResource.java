package nz.ac.auckland.concert.service.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.ArrayList;
import java.util.List;

public class NewsItemResource {

    protected List<AsyncResponse> responses = new ArrayList<>();

    @GET
    public void subscribe(@Suspended AsyncResponse response) {
        responses.add(response);
    }

    @POST
    public void send(NewsItemResource newsItemResource) {
        for (AsyncResponse asyncResponse : responses) {
            asyncResponse.resume(newsItemResource);
        }
    }


}
