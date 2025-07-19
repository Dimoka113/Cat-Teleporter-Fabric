package cat.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import net.minecraft.client.gui.screen.Screen;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import cat.client.Data.Data;

public class Disconnet {

    private Data data;

    public Disconnet(Data data) {
        this.data = data;
    }

    public void disconnectAndReconnect(String playerName, String pointName) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.getCurrentServerEntry() == null || client.player == null) {
            client.player.sendMessage(Text.literal("Вы не подключены к серверу."), false);
            return;
        }

        int[] pointData = data.getPlayerPointData(playerName, pointName);
        if (pointData == null || pointData.length != 2) {
            client.player.sendMessage(Text.literal("Точка не найдена в json."), false);
            return;
        }

        int val1 = pointData[0];
        int val2 = pointData[1];

        double calculatedTime = (val1 / 2.5 + (val2 - 3) / 2.5) / 2.0;
        long delayMillis = Math.round(calculatedTime * 1000);

        ServerInfo serverInfo = client.getCurrentServerEntry();

        // Отключение игрока
        client.execute(() -> {
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().getConnection().disconnect(
                        Text.literal(
                                "Перемещение: " + pointName + " " + Arrays.toString(pointData)
                        ));
            }
        });
        // Переподключение
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            client.execute(() -> {
                ServerAddress address = ServerAddress.parse(serverInfo.address);
                ConnectScreen.connect(new TitleScreen(), client, address, serverInfo, false, null);
            });
        }, delayMillis, TimeUnit.MILLISECONDS);
    }
}

