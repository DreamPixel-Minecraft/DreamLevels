package net.dreampixel.dreamlevels.sync;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.util.Debugger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowmessenger.common.message.Message;
import top.shadowpixel.shadowmessenger.common.message.messenger.MessageSubscription;

import java.util.UUID;

public class SyncSubscription extends MessageSubscription {

    @Override
    public void onMessage(@NotNull Message message) {
        Debugger.info(
                "&eMessage received from sync-mode channel.",
                "channel: \t" + message.channel,
                "sender: \t" + message.sender,
                "message: \t" + message.message,
                "parameters: \t" + message.parameters
        );

        var instruction = message.message;
        var parameters = message.getParameters();

        // get and check player data
        var data = DataManager.getInstance()
                .getPlayerData(UUID.fromString(message.parameters.get("player").toString()));
        if (data == null) {
            Debugger.info("Unknown player " + message.parameters.get("player").toString(), ", skipped.");
            reply("unknown player");
            return;
        }

        // Shitty variables declaration
        // Fuck you Java
        String level, type;
        LevelData levelData;
        double valueDouble;
        int valueInt;
        
        switch (instruction.toLowerCase()) {
            case "reset":
                level = parameters.getString("level", "");
                levelData = data.getLevelData(level);
                if (levelData == null) {
                    reply("unknown level");
                    return;
                }

                levelData.reset();
                reply("ok");
                break;
            case "reset-all":
                data.resetAll();
                reply("ok");
                break;
            case "modify-multiple":
                level = parameters.getString("level", "");
                levelData = data.getLevelData(level);
                valueDouble = parameters.getDouble("value");
                if (levelData == null) {
                    reply("unknown level");
                    return;
                }

                levelData.setMultiple(valueDouble);
                reply("ok");
                break;
            case "modify-levels":
                level = parameters.getString("level", "");
                levelData = data.getLevelData(level);
                valueInt = parameters.getInt("value");
                if (levelData == null) {
                    reply("unknown level");
                    return;
                }

                type = parameters.getString("type");
                if (type == null) {
                    reply("unknown mod type");
                    return;
                }
                
                switch (type) {
                    case "ADD":
                        levelData.addLevels(valueInt);
                        break;
                    case "SET":
                        levelData.setLevels(valueInt);
                        break;
                    case "REMOVE":
                        levelData.removeLevels(valueInt);
                        break;
                }
                reply("ok");
                break;
            case "modify-exp":
                level = parameters.getString("level", "");
                levelData = data.getLevelData(level);
                valueDouble = parameters.getDouble("value");
                if (levelData == null) {
                    reply("unknown level");
                    return;
                }

                type = parameters.getString("type");
                if (type == null) {
                    reply("unknown mod type");
                    return;
                }

                switch (type) {
                    case "ADD":
                        levelData.addExp(valueDouble);
                        break;
                    case "SET":
                        levelData.setExp(valueDouble);
                        break;
                    case "REMOVE":
                        levelData.removeExp(valueDouble);
                        break;
                }
                reply("ok");
                break;
        }

        data.saveAsync();
    }
}
