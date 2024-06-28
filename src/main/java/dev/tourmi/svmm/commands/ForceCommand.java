package dev.tourmi.svmm.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.tourmi.svmm.config.ClientConfig;
import dev.tourmi.svmm.config.SVMMConfig;
import dev.tourmi.svmm.server.ClientStatus;
import dev.tourmi.svmm.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class ForceCommand implements ICommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return net.minecraft.commands.Commands.literal("force")
                .requires(CommandPredicates::isForceVeinMiningEnabled)
                .requires(CommandPredicates::isPlayer)
                .requires(CommandPredicates::canPlayerAccessForceVeinMining)
                .executes(this::defaultExecute); // /svmm force
    }

    public int defaultExecute(CommandContext<CommandSourceStack> cc) {
        if (!checkIsAllowed(cc)) return 0;
        Player player = cc.getSource().getPlayer();
        ClientStatus status = ClientStatus.getClientStatus(player.getUUID());
        status.forceNext = !status.forceNext;
        String message = status.forceNext ? "Next block mined without holding shift will be vein mined" : "Cancelled vein mine";
        player.sendSystemMessage(Component.literal(message));
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getHelpText(CommandContext<CommandSourceStack> cc) {
        if (CommandUtils.isConsole(cc)) {
            return "";
        }

        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        if (!CommandUtils.isModerator(cc) && (cfg.MOD_DISABLED.get() || cfg.FORCE_RESTRICTED.get())) {
            return "";
        }

        return """
                - /svmm force
                    Forces a vein mine on the next block mined
                """;
    }

    private boolean checkIsAllowed(CommandContext<CommandSourceStack> cc) {
        if (CommandUtils.isConsole(cc))  {
            CommandUtils.sendMessage(cc, "Only players may use this command");
            return false;
        }

        ClientConfig cfg = CommandUtils.getSourceConfig(cc);
        boolean isModerator = CommandUtils.isModerator(cc);
        boolean isAllowed = isModerator || !(SVMMConfig.FORCE_DISABLED.get() || cfg.FORCE_RESTRICTED.get() || cfg.MOD_RESTRICTED.get());
        if (!isAllowed) {
            CommandUtils.sendMessage(cc, "You do not have access to this command.");
        }
        return isAllowed;
    }
}
