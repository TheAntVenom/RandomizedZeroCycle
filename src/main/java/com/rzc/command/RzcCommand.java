package com.rzc.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.rzc.config.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class RzcCommand {

    private static final String MOD_ID = "rzc";

    private static String getModVersion() {
        return FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(ModContainer::getMetadata)
            .map(metadata -> metadata.getVersion().getFriendlyString())
            .orElse("1.0.0");
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("rzc")
            // Root query: /rzc ?
            .then(CommandManager.literal("?")
                .executes(ctx -> sendConfigStatus(ctx.getSource()))
            )

            // Reload command: /rzc reload [?]
            .then(CommandManager.literal("reload")
                .executes(ctx -> {
                    Config.load();
                    return sendFeedback(ctx.getSource(), text("Configuration reloaded from file.", Formatting.GREEN));
                })
                .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "reload", null, "Reloads configuration settings directly from the JSON file.")))
            )

            // Settings subtree
            .then(CommandManager.literal("settings")

                // spawnHeight (1.16.1 Vanilla height limit: 0 to 255)
                .then(CommandManager.literal("spawnHeight")
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "settings spawnHeight", String.valueOf(Config.spawnHeight), "Sets the Y-level height at which the dragon spawns into the world.")))
                    .then(CommandManager.argument("height", IntegerArgumentType.integer(0, 255))
                        .executes(ctx -> {
                            Config.spawnHeight = IntegerArgumentType.getInteger(ctx, "height");
                            Config.save();
                            return sendValueChangeFeedback(ctx.getSource(), "spawnHeight", String.valueOf(Config.spawnHeight));
                        })
                    )
                )

                // yOffset (Allows negative & positive height adjustments)
                .then(CommandManager.literal("yOffset")
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "settings yOffset", String.valueOf(Config.yOffset), "Offsets the target node Y-level relative to vanilla. Accepts positive or negative integers.")))
                    .then(CommandManager.argument("offset", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            Config.yOffset = IntegerArgumentType.getInteger(ctx, "offset");
                            Config.save();
                            return sendValueChangeFeedback(ctx.getSource(), "yOffset", String.valueOf(Config.yOffset));
                        })
                    )
                )

                // spawnMsg
                .then(CommandManager.literal("spawnMsg")
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "settings spawnMsg", String.valueOf(Config.spawnMsg), "Toggles in-chat broadcast of the dragon's target node coordinates when it spawns.")))
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> {
                            Config.spawnMsg = BoolArgumentType.getBool(ctx, "enabled");
                            Config.save();
                            return sendValueChangeFeedback(ctx.getSource(), "spawnMsg", String.valueOf(Config.spawnMsg));
                        })
                    )
                )

                // deathMsg
                .then(CommandManager.literal("deathMsg")
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "settings deathMsg", String.valueOf(Config.deathMsg), "Toggles in-chat broadcast of the final target node and spawn height when the dragon dies.")))
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> {
                            Config.deathMsg = BoolArgumentType.getBool(ctx, "enabled");
                            Config.save();
                            return sendValueChangeFeedback(ctx.getSource(), "deathMsg", String.valueOf(Config.deathMsg));
                        })
                    )
                )

                // nodeMarker subtree
                .then(CommandManager.literal("nodeMarker")
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(
                        ctx.getSource(), 
                        "settings nodeMarker", 
                        Config.nodeMarker ? ("ON (#" + Config.nodeMarkerColor + ")") : "OFF", 
                        "Toggles a vertical particle beacon at the target node coordinate. Usage: /rzc settings nodeMarker <on|off> OR /rzc settings nodeMarker color <preset|HEX|r g b>"
                    )))
                    
                    // Explicit "on" literal
                    .then(CommandManager.literal("on")
                        .executes(ctx -> {
                            Config.nodeMarker = true;
                            Config.save();
                            return sendValueChangeFeedback(ctx.getSource(), "nodeMarker", "ON");
                        })
                    )
                    
                    // Explicit "off" literal
                    .then(CommandManager.literal("off")
                        .executes(ctx -> {
                            Config.nodeMarker = false;
                            Config.save();
                            return sendValueChangeFeedback(ctx.getSource(), "nodeMarker", "OFF");
                        })
                    )
                    
                    // Set custom hex, preset name, or RGB values
                    .then(CommandManager.literal("color")
                        .then(CommandManager.literal("?")
                            .executes(ctx -> sendFormattedHelp(
                                ctx.getSource(),
                                "settings nodeMarker color <preset|HEX|r g b>",
                                "Current: #" + Config.nodeMarkerColor,
                                "Sets beacon color via preset name (red, green, blue...), 6-digit Hex, or RGB (0-255 0-255 0-255)."
                            ))
                        )
                        // String input: Hex codes or Color preset names
                        .then(CommandManager.argument("presetOrHex", StringArgumentType.word())
                            .executes(ctx -> {
                                String val = StringArgumentType.getString(ctx, "presetOrHex").replace("#", "").toLowerCase();
                                switch (val) {
                                    case "red":     Config.nodeMarkerColor = "FF0000"; break;
                                    case "green":   Config.nodeMarkerColor = "00FF00"; break;
                                    case "blue":    Config.nodeMarkerColor = "0000FF"; break;
                                    case "white":   Config.nodeMarkerColor = "FFFFFF"; break;
                                    case "yellow":  Config.nodeMarkerColor = "FFFF00"; break;
                                    case "cyan":    Config.nodeMarkerColor = "00FFFF"; break;
                                    case "magenta": Config.nodeMarkerColor = "FF00FF"; break;
                                    case "black":   Config.nodeMarkerColor = "000000"; break;
                                    default:
                                        if (!val.matches("^[0-9a-fa-f]{6}$")) {
                                            return sendFeedback(
                                                ctx.getSource(), 
                                                text("Invalid Color! Use a preset (red, green...) or 6-digit Hex (e.g. FF0000).", Formatting.RED)
                                            );
                                        }
                                        Config.nodeMarkerColor = val.toUpperCase();
                                        break;
                                }
                                Config.save();
                                return sendValueChangeFeedback(ctx.getSource(), "nodeMarker color", "#" + Config.nodeMarkerColor);
                            })
                        )
                        // RGB Integer input: /rzc settings nodeMarker color <r> <g> <b>
                        .then(CommandManager.argument("r", IntegerArgumentType.integer(0, 255))
                            .then(CommandManager.argument("g", IntegerArgumentType.integer(0, 255))
                                .then(CommandManager.argument("b", IntegerArgumentType.integer(0, 255))
                                    .executes(ctx -> {
                                        int r = IntegerArgumentType.getInteger(ctx, "r");
                                        int g = IntegerArgumentType.getInteger(ctx, "g");
                                        int b = IntegerArgumentType.getInteger(ctx, "b");

                                        Config.nodeMarkerColor = String.format("%02X%02X%02X", r, g, b);
                                        Config.save();
                                        return sendValueChangeFeedback(ctx.getSource(), "nodeMarker color", "#" + Config.nodeMarkerColor);
                                    })
                                )
                            )
                        )
                    )
                )
            )

            // Mode subtree
            .then(CommandManager.literal("mode")
                .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "mode", Config.mode.name(), "Controls dragon targeting logic (Vanilla, FullyRandom, ExpandedZeroCycle, TwelveVanillaNodes, ChooseXZ).")))
                
                // Vanilla
                .then(CommandManager.literal("Vanilla")
                    .executes(ctx -> setMode(ctx.getSource(), Config.Mode.Vanilla))
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "mode Vanilla", null, "Standard Minecraft dragon behavior using vanilla node selection.")))
                )

                // FullyRandom
                .then(CommandManager.literal("FullyRandom")
                    .executes(ctx -> setMode(ctx.getSource(), Config.Mode.FullyRandom))
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "mode FullyRandom", "Min: " + Config.randomRingMin + ", Max: " + Config.randomRingMax, "Sets the minimum and maximum distance from 0,0 for where the node will spawn in FullyRandom mode.")))
                    .then(CommandManager.argument("min", DoubleArgumentType.doubleArg(0.0))
                        .executes(ctx -> {
                            double min = DoubleArgumentType.getDouble(ctx, "min");
                            Config.mode = Config.Mode.FullyRandom;
                            Config.randomRingMin = min;
                            Config.save();
                            return sendModeChangeFeedback(ctx.getSource(), "FullyRandom (Min: " + min + ", Max: " + Config.randomRingMax + ")");
                        })
                        .then(CommandManager.argument("max", DoubleArgumentType.doubleArg(0.0))
                            .executes(ctx -> {
                                double min = DoubleArgumentType.getDouble(ctx, "min");
                                double max = DoubleArgumentType.getDouble(ctx, "max");
                                Config.mode = Config.Mode.FullyRandom;
                                Config.randomRingMin = min;
                                Config.randomRingMax = max;
                                Config.save();
                                return sendModeChangeFeedback(ctx.getSource(), "FullyRandom (Min: " + min + ", Max: " + max + ")");
                            })
                        )
                    )
                )

                // ExpandedZeroCycle
                .then(CommandManager.literal("ExpandedZeroCycle")
                    .executes(ctx -> setMode(ctx.getSource(), Config.Mode.ExpandedZeroCycle))
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "mode ExpandedZeroCycle", null, "Each tower has a 7/8 node & a 1/8 node. Think normal Zero Cycles, but where any tower can get selected.")))
                )

                // TwelveVanillaNodes
                .then(CommandManager.literal("TwelveVanillaNodes")
                    .executes(ctx -> setMode(ctx.getSource(), Config.Mode.TwelveVanillaNodes))
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "mode TwelveVanillaNodes", null, "Randomly selects one of the 12 standard dragon flight nodes. A silly mode.")))
                )
                
                // ChooseXZ
                .then(CommandManager.literal("ChooseXZ")
                    .executes(ctx -> {
                        Config.mode = Config.Mode.ChooseXZ;
                        Config.save();
                        return sendModeChangeFeedback(ctx.getSource(), "ChooseXZ (" + (int)Config.chooseX + "x, " + (int)Config.chooseZ + "z)");
                    })
                    .then(CommandManager.literal("?").executes(ctx -> sendFormattedHelp(ctx.getSource(), "mode ChooseXZ", (int)Config.chooseX + "x, " + (int)Config.chooseZ + "z", "Choose your own perch node X-Z coordinates.")))
                    .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            double x = DoubleArgumentType.getDouble(ctx, "x");
                            Config.mode = Config.Mode.ChooseXZ;
                            Config.chooseX = x;
                            Config.save();
                            return sendModeChangeFeedback(ctx.getSource(), "ChooseXZ (" + (int)x + "x, " + (int)Config.chooseZ + "z)");
                        })
                        .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                            .executes(ctx -> {
                                double x = DoubleArgumentType.getDouble(ctx, "x");
                                double z = DoubleArgumentType.getDouble(ctx, "z");
                                Config.mode = Config.Mode.ChooseXZ;
                                Config.chooseX = x;
                                Config.chooseZ = z;
                                Config.save();
                                return sendModeChangeFeedback(ctx.getSource(), "ChooseXZ (" + (int)x + "x, " + (int)z + "z)");
                            })
                        )
                    )
                )
            )
        );
    }

    private static int setMode(ServerCommandSource source, Config.Mode mode) {
        Config.mode = mode;
        Config.save();
        return sendModeChangeFeedback(source, mode.name());
    }

    private static int sendModeChangeFeedback(ServerCommandSource source, String modeDetails) {
        MutableText msg = text("Mode set to ", Formatting.GRAY)
            .append(text(modeDetails, Formatting.GREEN, Formatting.BOLD));
        return sendFeedback(source, msg);
    }

    private static int sendValueChangeFeedback(ServerCommandSource source, String key, String value) {
        MutableText msg = text(key, Formatting.YELLOW, Formatting.BOLD)
            .append(text(" set to ", Formatting.GRAY))
            .append(text(value, Formatting.GREEN, Formatting.BOLD));
        return sendFeedback(source, msg);
    }

    private static int sendConfigStatus(ServerCommandSource source) {
        MutableText line1 = text("Randomized Zero Cycle Mod ", Formatting.AQUA)
            .append(text("v" + getModVersion(), Formatting.GREEN))
            .append(text(" - By AntVenom w/ AI - ", Formatting.DARK_GRAY))
            .append(text("Tab complete for sub-commands.", Formatting.GRAY, Formatting.ITALIC));
        sendFeedback(source, line1);

        String extraInfo = "";
        if (Config.mode == Config.Mode.ChooseXZ) {
            extraInfo = " (" + (int)Config.chooseX + "x, " + (int)Config.chooseZ + "z)";
        } else if (Config.mode == Config.Mode.FullyRandom) {
            extraInfo = " (Min: " + Config.randomRingMin + ", Max: " + Config.randomRingMax + ")";
        }

        MutableText line2 = text("Mode: ", Formatting.YELLOW, Formatting.BOLD)
            .append(text(Config.mode.name() + extraInfo, Formatting.GREEN))
            .append(text(" | ", Formatting.DARK_GRAY))
            .append(text("spawnHeight: ", Formatting.YELLOW, Formatting.BOLD))
            .append(text(String.valueOf(Config.spawnHeight), Formatting.GREEN))
            .append(text(" | ", Formatting.DARK_GRAY))
            .append(text("yOffset: ", Formatting.YELLOW, Formatting.BOLD))
            .append(text(String.valueOf(Config.yOffset), Formatting.GREEN))
            .append(text(" | ", Formatting.DARK_GRAY))
            .append(text("spawnMsg: ", Formatting.YELLOW, Formatting.BOLD))
            .append(text(String.valueOf(Config.spawnMsg), Formatting.GREEN))
            .append(text(" | ", Formatting.DARK_GRAY))
            .append(text("deathMsg: ", Formatting.YELLOW, Formatting.BOLD))
            .append(text(String.valueOf(Config.deathMsg), Formatting.GREEN))
            .append(text(" | ", Formatting.DARK_GRAY))
            .append(text("nodeMarker: ", Formatting.YELLOW, Formatting.BOLD))
            .append(text(Config.nodeMarker ? ("ON (#" + Config.nodeMarkerColor + ")") : "OFF", Formatting.GREEN));
        sendFeedback(source, line2);

        MutableText line3 = text("Available Modes: ", Formatting.GOLD)
            .append(text("Vanilla, FullyRandom, ExpandedZeroCycle, TwelveVanillaNodes, ChooseXZ", Formatting.GRAY));
        sendFeedback(source, line3);

        return 1;
    }

    private static int sendFormattedHelp(ServerCommandSource source, String label, String value, String description) {
        MutableText msg = text(label, Formatting.YELLOW, Formatting.BOLD);

        if (value != null && !value.isEmpty()) {
            msg.append(text(": ", Formatting.DARK_GRAY))
               .append(text(value, Formatting.GREEN));
        }

        msg.append(text(" - ", Formatting.DARK_GRAY))
           .append(text(description, Formatting.GRAY, Formatting.ITALIC));

        return sendFeedback(source, msg);
    }

    private static int sendFeedback(ServerCommandSource source, MutableText text) {
        LiteralText prefix = new LiteralText("[");
        prefix.formatted(Formatting.DARK_GRAY);

        LiteralText tag = new LiteralText("RZC");
        tag.formatted(Formatting.DARK_AQUA, Formatting.BOLD);

        LiteralText closeBracket = new LiteralText("] ");
        closeBracket.formatted(Formatting.DARK_GRAY);

        prefix.append(tag).append(closeBracket).append(text);
        source.sendFeedback(prefix, false);
        return 1;
    }

    private static MutableText text(String content, Formatting... formattings) {
        LiteralText t = new LiteralText(content);
        t.formatted(formattings);
        return t;
    }
}