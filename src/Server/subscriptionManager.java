package Server;

import java.util.HashSet;
import java.util.Set;


public class subscriptionManager {

    private final Set<String> subscribedUsers = new HashSet<>();

    public void subcribe(String username){
        subscribedUsers.add(username);
    }

    public void unsubscribe(String username){
        subscribedUsers.remove(username);
    }

    public boolean isSubscribed(String username){
        return subscribedUsers.contains(username);
    }
    
}
