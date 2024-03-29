package nz.ac.auckland.concert.service.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {

    private Set<Class<?>> _classes = new HashSet<Class<?>>();
    private Set<Object> _singletons = new HashSet<Object>();

    public ConcertApplication() {
        _singletons.add(PersistenceManager.class);
        _classes.add(ConcertResource.class);
        _classes.add(ReservationResource.class);
        _classes.add(PerformerResource.class);
        _classes.add(UserResource.class);
        _singletons.add(new NewsItemResource());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return _classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return _singletons;
    }

}
