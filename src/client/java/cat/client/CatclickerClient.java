package cat.client;



import cat.client.Data.Data;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import cat.client.Data.ColorText;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CatclickerClient implements ClientModInitializer {
    private static final Data storage = new Data();
    private static List<String> playerpoints = List.of();


    public static final SuggestionProvider<FabricClientCommandSource> tab_force = (context, builder) -> {
        return CommandSource.suggestMatching(List.of("--force"), builder);
    };


    public static final SuggestionProvider<FabricClientCommandSource> dynamic_names_player = (context, builder) -> {
        updateDynamicNames();
        return CommandSource.suggestMatching(playerpoints, builder);
    };

    public static void updateDynamicNames() {
        var p = MinecraftClient.getInstance().player;
        if (p == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        String paddress = Objects.requireNonNull(client.getNetworkHandler().getConnection().getAddress())
                .toString().replaceAll("^([^/]+)/.*:(\\d+)$", "$1:$2");

        Set<String> points = storage.getPlayerPoints(paddress);
        playerpoints = new ArrayList<>(points);
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(
                        literal("cat").executes(CatclickerClient::help)
                                .then(literal("help").executes(CatclickerClient::help))


                                .then(literal("go")
                                        .then(argument("name", StringArgumentType.word())
                                                .suggests(dynamic_names_player)
                                                .executes(CatclickerClient::go)
                                        ))
                                .then(literal("del")
                                        .then(argument("name", StringArgumentType.word())
                                                .suggests(dynamic_names_player)
                                                .executes(CatclickerClient::del)
                                        ))


                                .then(literal("list").executes(CatclickerClient::list))
                                .then(literal("add")
                                        .then(argument("name", StringArgumentType.word())
                                                .then(argument("t1", StringArgumentType.string())
                                                        .then(argument("t2", StringArgumentType.string())
                                                                .executes(CatclickerClient::add)
                                                                .then(argument("force", StringArgumentType.string())
                                                                .suggests(tab_force)
                                                                .executes(CatclickerClient::add)
                                                        )))





                                )
                )));
    }

    private static int help(CommandContext<FabricClientCommandSource> ctx) {
        var p = MinecraftClient.getInstance().player;

        Text text = Text.empty()
                .append(ColorText.create("Доступные команды: ", 0x189AB4))
                .append(ColorText.create("\n- ", 0x00ffd1))

                .append(ColorText.create("help", 0x58f79b))
                .append(ColorText.create(" (это сообщение)", 0x52be80))

                .append(ColorText.create("\n- ", 0x00ffd1))
                .append(ColorText.create("add", 0x58f79b))
                .append(ColorText.create(" <название> <t1> <t2>", 0xc0fe27))
                .append(ColorText.create(" (Добавить новую точку)", 0x52be80))

                .append(ColorText.create("\n- ", 0x00ffd1))
                .append(ColorText.create("list", 0x58f79b))
                .append(ColorText.create(" (Список сохранённых точек)", 0x52be80))

                .append(ColorText.create("\n- ", 0x00ffd1))
                .append(ColorText.create("go", 0x58f79b))
                .append(ColorText.create(" <название>", 0xc0fe27))
                .append(ColorText.create(" (Переместиться на существующую точку)", 0x52be80))

                .append(ColorText.create("\n- ", 0x00ffd1))
                .append(ColorText.create("del", 0x58f79b))
                .append(ColorText.create(" <название>", 0xc0fe27))
                .append(ColorText.create(" (Удалить существующую точку)", 0x52be80))
                ;


        p.sendMessage(text, false);

        return 1;
    }

    private static int add(CommandContext<FabricClientCommandSource> ctx) {
        var p = MinecraftClient.getInstance().player;

        MinecraftClient client = MinecraftClient.getInstance();
        String paddress = Objects.requireNonNull(client.getNetworkHandler().getConnection().getAddress())
                .toString().replaceAll("^([^/]+)/.*:(\\d+)$", "$1:$2");

        Set<String> points = storage.getPlayerPoints(paddress);
        String name = StringArgumentType.getString(ctx, "name");
        int t1 = Integer.parseInt(StringArgumentType.getString(ctx, "t1"));
        int t2 = Integer.parseInt(StringArgumentType.getString(ctx, "t2"));


        String force = "";
        try {
            force = StringArgumentType.getString(ctx, "force");
        }
        catch (IllegalArgumentException ignored) {}
        boolean fourpoint = force.equals("--force");


        if (!points.contains(name) | fourpoint) {
            storage.addPlayerPoint(paddress, name, t1, t2);
            p.sendMessage(
                    Text.empty()
                            .append(ColorText.create("Точка ", 0x47ff52))
                            .append(ColorText.create(name, 0x2ce6ff))
                            .append(ColorText.create(" успешно добавлена:", 0x47ff52))

                            .append(ColorText.create(" [", 0x189AB4))
                            .append(ColorText.create(String.valueOf(t1), 0x00ffd1))
                            .append(ColorText.create(", ", 0x189AB4))
                            .append(ColorText.create(String.valueOf(t2), 0x00ffd1))
                            .append(ColorText.create("]", 0x189AB4))

                    , false);
        }
        else {
            p.sendMessage(
                    Text.empty()
                            .append(ColorText.create("У вас есть эта точка: ", 0xf9ff2f))
                            .append(ColorText.create(name, 0xff9b00))

                            .append(ColorText.create(" [", 0xf9ff2f))
                            .append(ColorText.create(String.valueOf(t1), 0xe58407))
                            .append(ColorText.create(", ", 0xf9ff2f))
                            .append(ColorText.create(String.valueOf(t2), 0xe58407))
                            .append(ColorText.create("]", 0xf9ff2f))



                    , false);
            p.sendMessage(
                    Text.empty()
                            .append(ColorText.create("Вы можете использовать ", 0x14b9a5))
                            .append(ColorText.create("--force", 0x385bd0))

                            .append(ColorText.create(" как последний аргумент, чтобы ", 0x14b9a5))
                            .append(ColorText.create("перезаписать ", 0xf78938))
                            .append(ColorText.create("эту точку.", 0x14b9a5))

                    , false);

        }
        return 1;
    }

    private static int list(CommandContext<FabricClientCommandSource> ctx) {
        var p = MinecraftClient.getInstance().player;

        MinecraftClient client = MinecraftClient.getInstance();
        String paddress = Objects.requireNonNull(client.getNetworkHandler().getConnection().getAddress())
                .toString().replaceAll("^([^/]+)/.*:(\\d+)$", "$1:$2");
        Set<String> pts = storage.getPlayerPoints(paddress);
        if (pts.isEmpty()) {
            p.sendMessage(Text.literal("У вас нет сохранённых точек.").setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(0xF88379))
            ), false);

        } else {
            p.sendMessage(Text.literal("Ваши точки:").setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(0x47ff52))
            ), false);
            for (String n : pts) {
                int[] arr = storage.getPlayerPointData(paddress, n);
                Text text = Text.empty()
                        .append(ColorText.create("- ", 0x189AB4))
                        .append(ColorText.create(n, 0x00ffd1))
                        .append(ColorText.create(": [", 0x189AB4))
                        .append(ColorText.create(String.valueOf(arr[0]), 0x00ffd1))
                        .append(ColorText.create(", ", 0x189AB4))
                        .append(ColorText.create(String.valueOf(arr[1]), 0x00ffd1))
                        .append(ColorText.create("]", 0x189AB4))
                        ;

                p.sendMessage(text, false);
            }
        }
        return 1;
    }

    private static int del(CommandContext<FabricClientCommandSource> ctx) {
        var p = MinecraftClient.getInstance().player;

        MinecraftClient client = MinecraftClient.getInstance();
        String paddress = Objects.requireNonNull(client.getNetworkHandler().getConnection().getAddress())
                .toString().replaceAll("^([^/]+)/.*:(\\d+)$", "$1:$2");
        String name = StringArgumentType.getString(ctx, "name");
        int[] arr = storage.getPlayerPointData(paddress, name);
        if (arr == null) {
            p.sendMessage(Text.empty()
                    .append(ColorText.create("У вас нет точки ", 0xF88379))
                    .append(ColorText.create(name, 0xfeb027))
                    .append(ColorText.create("!", 0xF88379))

                    , false);
        } else {
            storage.removePlayerPoint(paddress, name);
            p.sendMessage(Text.empty()
                            .append(ColorText.create("Точка ", 0xf5ff59))
                            .append(ColorText.create(name, 0xfeb027))
                            .append(ColorText.create(" успешно удалена", 0xf5ff59))

                    , false);


        }
        return 1;
    }

    private static int go(CommandContext<FabricClientCommandSource> ctx) {
        Data data = new Data();
        data.loadFromFile("config/CatTeleporter/config.json");

        var p = MinecraftClient.getInstance().player;
        MinecraftClient client = MinecraftClient.getInstance();

        String paddress = Objects.requireNonNull(client.getNetworkHandler().getConnection().getAddress())
                .toString().replaceAll("^([^/]+)/.*:(\\d+)$", "$1:$2");
        String name = StringArgumentType.getString(ctx, "name");
        int[] arr = storage.getPlayerPointData(paddress, name);
        if (arr == null) {
            p.sendMessage(Text.empty()
                            .append(ColorText.create("У вас нет точки ", 0xF88379))
                            .append(ColorText.create(name, 0xfeb027))
                            .append(ColorText.create("!", 0xF88379))

                    , false);
        } else {
            new Disconnet(data).disconnectAndReconnect(paddress, name);
        }
        return 1;
    }


    @Override
    public void onInitializeClient() {
        CatclickerClient.register();


        String filePath = "config/CatTeleporter/config.json";
        Path configDir = Path.of("config/CatTeleporter");

        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            System.out.print("You can check json file here: " + filePath);
        } else {
            try {
                Files.createDirectories(configDir);
                String filename = "config.json";
                String jsonContent = "{}";

                try (FileWriter fileconfig = new FileWriter(filePath)) {
                    fileconfig.write(jsonContent);
                    System.out.print("File " + filename + " successfully created");
                    System.out.print("You can check json file here: " + filePath);
                } catch (IOException e) {}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        storage.loadFromFile(filePath);
    }
}
