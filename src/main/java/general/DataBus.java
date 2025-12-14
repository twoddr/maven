package general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DataBus {
    private static final DataBus instance = new DataBus();
    private final Map<String, List<Consumer<Object>>> listeners = new HashMap<>();

    public static DataBus getInstance() {
        return instance;
    }

    public void subscribe(String channel, Consumer<Object> listener) {
        listeners.computeIfAbsent(channel, k -> new ArrayList<>()).add(listener);
    }

    public void publish(String channel, Object data) {
        List<Consumer<Object>> channelListeners = listeners.get(channel);
        if (channelListeners != null) {
            channelListeners.forEach(listener -> listener.accept(data));
        }
    }
}
