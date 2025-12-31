package net.dreampixel.dreamlevels.sync;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.util.Debugger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowmessenger.common.message.Message;
import top.shadowpixel.shadowmessenger.common.message.messenger.MessageSubscription;

import java.util.UUID;

/**
 * A receiver class of sync messages.
 */
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

        // get and check level data
        var level = parameters.getString("level", "");
        var levelData = data.getLevelData(level);

        if (levelData == null) {
            reply("unknown level");
            return;
        }

        // get modification type for certain usages
        var type = parameters.getString("type");
        
        switch (instruction.toLowerCase()) {
            // reset player's data
            case "reset":
                levelData.reset();
                reply("ok");
                break;
            // reset player's all data
            case "reset-all":
                data.resetAll();
                reply("ok");
                break;
            // modify player's multiple
            case "modify-multiple":
                levelData.setMultiple(parameters.getDouble("value"));
                reply("ok");
                break;
            // modify player's levels
            case "modify-levels":
                var inbValue = parameters.getInt("value");
                if (type == null) {
                    reply("unknown mod type");
                    return;
                }
                
                switch (type) {
                    case "ADD":
                        levelData.addLevels(inbValue);
                        break;
                    case "SET":
                        levelData.setLevels(inbValue);
                        break;
                    case "REMOVE":
                        levelData.removeLevels(inbValue);
                        break;
                }
                reply("ok");
                break;
            // modify player's exp
            case "modify-exp":
                var doubleValue = parameters.getDouble("value");
                if (type == null) {
                    reply("unknown mod type");
                    return;
                }

                switch (type) {
                    case "ADD":
                        levelData.addExp(doubleValue);
                        break;
                    case "SET":
                        levelData.setExp(doubleValue);
                        break;
                    case "REMOVE":
                        levelData.removeExp(doubleValue);
                        break;
                }
                reply("ok");
                break;
        }

        // save the data async
        data.saveAsync();
    }
}
