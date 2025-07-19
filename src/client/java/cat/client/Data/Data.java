package cat.client.Data;

import com.google.gson.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class Data {

    private final JsonObject root;
    private final Gson gson;
    private Path configDir = Path.of("config/CatTeleporter");
    private Path configPath = Path.of("config/CatTeleporter/config.json");

    public Data() {
        this.root = new JsonObject();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void addPlayerPoint(String nickName, String pointName, int val1, int val2) {
        JsonArray array = new JsonArray();
        array.add(val1);
        array.add(val2);

        JsonObject playerData;
        if (root.has(nickName)) {
            playerData = root.getAsJsonObject(nickName);
        } else {
            playerData = new JsonObject();
            root.add(nickName, playerData);
        }

        playerData.add(pointName, array);
        saveToFile(String.valueOf(configPath));
    }

    public boolean removePlayerPoint(String nickName, String pointName) {
        if (root.has(nickName)) {
            JsonObject playerData = root.getAsJsonObject(nickName);
            if (playerData.has(pointName)) {
                playerData.remove(pointName);

                if (playerData.entrySet().isEmpty()) {
                    root.remove(nickName);
                }

                saveToFile(String.valueOf(configPath));
                return true;
            }
        }
        return false;
    }

    public Set<String> getPlayerPoints(String nickName) {
        if (root.has(nickName)) {
            JsonObject playerData = root.getAsJsonObject(nickName);
            return playerData.keySet();
        }
        return Collections.emptySet();
    }

    public void saveToFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean removeByName(String name) {
        if (root.has(name)) {
            root.remove(name);
            return true;
        } else {
            return false;
        }
    }


    public int[] getPlayerPointData(String nickName, String pointName) {
        if (root.has(nickName)) {
            JsonObject playerData = root.getAsJsonObject(nickName);
            if (playerData.has(pointName)) {
                JsonArray coords = playerData.getAsJsonArray(pointName);
                if (coords.size() == 2) {
                    int x = coords.get(0).getAsInt();
                    int y = coords.get(1).getAsInt();
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    public void loadFromFile(String filename) {

        try (FileReader reader = new FileReader(filename)) {
            JsonObject loaded = gson.fromJson(reader, JsonObject.class);
            root.entrySet().clear();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : loaded.entrySet()) {
                root.add(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}