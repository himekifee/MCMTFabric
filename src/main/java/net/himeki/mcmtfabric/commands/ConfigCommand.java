package net.himeki.mcmtfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import net.himeki.mcmtfabric.MCMT;
import net.himeki.mcmtfabric.ParallelProcessor;
import net.himeki.mcmtfabric.config.BlockEntityLists;
import net.himeki.mcmtfabric.config.GeneralConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.BlockEntityTickInvoker;

import static net.minecraft.server.command.CommandManager.literal;

public class ConfigCommand {
    private static GeneralConfig config = MCMT.config;
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> mcmtconfig = literal("mcmt");
        mcmtconfig = mcmtconfig.then(registerConfig(literal("config")));
        mcmtconfig = mcmtconfig.then(DebugCommand.registerDebug(literal("debug")));
        mcmtconfig = StatsCommand.registerStatus(mcmtconfig);
        dispatcher.register(mcmtconfig);
    }

    public static ArgumentBuilder<ServerCommandSource, ?> registerConfig(LiteralArgumentBuilder<ServerCommandSource> root) {
        return root.then(literal("toggle").requires(cmdSrc -> {
            return cmdSrc.hasPermissionLevel(2);
        }).executes(cmdCtx -> {
            config.disabled = !config.disabled;
            LiteralText message = new LiteralText(
                    "MCMT is now " + (config.disabled ? "disabled" : "enabled"));
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        }).then(literal("te").executes(cmdCtx -> {
            config.disableTileEntity = !config.disableTileEntity;
            LiteralText message = new LiteralText("MCMT's tile entity threading is now "
                    + (config.disableTileEntity ? "disabled" : "enabled")) {
            };
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        })).then(literal("entity").executes(cmdCtx -> {
            config.disableEntity = !config.disableEntity;
            LiteralText message = new LiteralText(
                    "MCMT's entity threading is now " + (config.disableEntity ? "disabled" : "enabled"));
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        })).then(literal("environment").executes(cmdCtx -> {
            config.disableEnvironment = !config.disableEnvironment;
            LiteralText message = new LiteralText("MCMT's environment threading is now "
                    + (config.disableEnvironment ? "disabled" : "enabled"));
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        })).then(literal("world").executes(cmdCtx -> {
            config.disableWorld = !config.disableWorld;
            LiteralText message = new LiteralText(
                    "MCMT's world threading is now " + (config.disableWorld ? "disabled" : "enabled"));
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        })).then(literal("chunkprovider").executes(cmdCtx -> {
            config.disableChunkProvider = !config.disableChunkProvider;
            LiteralText message = new LiteralText(
                    "MCMT's SCP threading is now " + (config.disableChunkProvider ? "disabled" : "enabled"));
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        }))).then(literal("state").executes(cmdCtx -> {
            StringBuilder messageString = new StringBuilder(
                    "MCMT is currently " + (config.disabled ? "disabled" : "enabled"));
            if (!config.disabled) {
                messageString.append(" World:" + (config.disableWorld ? "disabled" : "enabled"));
                messageString.append(" Entity:" + (config.disableEntity ? "disabled" : "enabled"));
                messageString.append(" TE:" + (config.disableTileEntity ? "disabled"
                        : "enabled" + (config.chunkLockModded ? "(ChunkLocking Modded)" : "")));
                messageString.append(" Env:" + (config.disableEnvironment ? "disabled" : "enabled"));
                messageString.append(" SCP:" + (config.disableChunkProvider ? "disabled" : "enabled"));
            }
            LiteralText message = new LiteralText(messageString.toString());
            cmdCtx.getSource().sendFeedback(message, true);
            return 1;
        }))
                .then(literal("save").requires(cmdSrc -> {
                    return cmdSrc.hasPermissionLevel(2);
                }).executes(cmdCtx -> {
                    LiteralText message = new LiteralText("Saving MCMT config to disk...");
                    cmdCtx.getSource().sendFeedback(message, true);
                    AutoConfig.getConfigHolder(GeneralConfig.class).save();
                    message = new LiteralText("Done!");
                    cmdCtx.getSource().sendFeedback(message, true);
                    return 1;
                }))
                .then(literal("temanage").requires(cmdSrc -> {
                            return cmdSrc.hasPermissionLevel(2);
                        }).then(literal("list")
                                .executes(cmdCtx -> {
                                    LiteralText message = new LiteralText("NYI");
                                    cmdCtx.getSource().sendFeedback(message, true);
                                    return 1;
                                })).then(literal("target")
                                .requires(cmdSrc -> {
                                    try {
                                        if (cmdSrc.getPlayer() != null) {
                                            return true;
                                        }
                                    } catch (CommandSyntaxException e) {
                                        e.printStackTrace();
                                    }
                                    LiteralText message = new LiteralText("Only runnable by player!");
                                    cmdSrc.sendError(message);
                                    return false;
                                }).then(literal("whitelist").executes(cmdCtx -> {
                                    LiteralText message;
                                    HitResult htr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
                                    if (htr.getType() == HitResult.Type.BLOCK) {
                                        BlockPos bp = ((BlockHitResult) htr).getBlockPos();
                                        BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
                                        if (te != null && isTickableBe(te)) {
                                            if (config.teWhiteListString.contains(te.getClass().getName()))
                                            {
                                                message = new LiteralText("Class " + te.getClass().getName() + " already exists in TE Whitelist");
                                                cmdCtx.getSource().sendFeedback(message, true);
                                                return 0;
                                            }
                                            BlockEntityLists.teWhiteList.add(te.getClass());
                                            config.teWhiteListString.add(te.getClass().getName());
                                            BlockEntityLists.teBlackList.remove(te.getClass());
                                            config.teBlackListString.remove(te.getClass().getName());
                                            message = new LiteralText("Added " + te.getClass().getName() + " to TE Whitelist");
                                            cmdCtx.getSource().sendFeedback(message, true);
                                            return 1;
                                        }
                                        message = new LiteralText("That block doesn't contain a tickable TE!");
                                        cmdCtx.getSource().sendError(message);
                                        return 0;
                                    }
                                    message = new LiteralText("Only runable by player!");
                                    cmdCtx.getSource().sendError(message);
                                    return 0;
                                })).then(literal("blacklist").executes(cmdCtx -> {
                                    LiteralText message;
                                    HitResult htr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
                                    if (htr.getType() == HitResult.Type.BLOCK) {
                                        BlockPos bp = ((BlockHitResult) htr).getBlockPos();
                                        BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
                                        if (te != null && isTickableBe(te)) {
                                            if (config.teBlackListString.contains(te.getClass().getName()))
                                            {
                                                message = new LiteralText("Class " + te.getClass().getName() + " already exists in TE Blacklist");
                                                cmdCtx.getSource().sendFeedback(message, true);
                                                return 0;
                                            }
                                            BlockEntityLists.teBlackList.add(te.getClass());
                                            config.teBlackListString.add(te.getClass().getName());
                                            BlockEntityLists.teWhiteList.remove(te.getClass());
                                            config.teWhiteListString.remove(te.getClass().getName());
                                            message = new LiteralText("Added " + te.getClass().getName() + " to TE Blacklist");
                                            cmdCtx.getSource().sendFeedback(message, true);
                                            return 1;
                                        }
                                        message = new LiteralText("That block doesn't contain a tickable TE!");
                                        cmdCtx.getSource().sendError(message);
                                        return 0;
                                    }
                                    message = new LiteralText("Only runnable by player!");
                                    cmdCtx.getSource().sendError(message);
                                    return 0;
                                })).then(literal("remove").executes(cmdCtx -> {
                                    LiteralText message;
                                    HitResult htr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
                                    if (htr.getType() == HitResult.Type.BLOCK) {
                                        BlockPos bp = ((BlockHitResult) htr).getBlockPos();
                                        BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
                                        if (te != null && isTickableBe(te)) {
                                            BlockEntityLists.teBlackList.remove(te.getClass());
                                            config.teBlackListString.remove(te.getClass().getName());
                                            BlockEntityLists.teWhiteList.remove(te.getClass());
                                            config.teWhiteListString.remove(te.getClass().getName());
                                            message = new LiteralText("Removed " + te.getClass().getName() + " from TE classlists");
                                            cmdCtx.getSource().sendFeedback(message, true);
                                            return 1;
                                        }
                                        message = new LiteralText("That block doesn't contain a tickable TE!");
                                        cmdCtx.getSource().sendError(message);
                                        return 0;
                                    }
                                    message = new LiteralText("Only runable by player!");
                                    cmdCtx.getSource().sendError(message);
                                    return 0;
                                })).then(literal("willtick").executes(cmdCtx -> {
                                    LiteralText message;
                                    HitResult htr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
                                    if (htr.getType() == HitResult.Type.BLOCK) {
                                        BlockPos bp = ((BlockHitResult) htr).getBlockPos();
                                        BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
                                        if (isTickableBe(te)) {
                                            boolean willSerial = ParallelProcessor.filterTE((BlockEntityTickInvoker) te);
                                            message = new LiteralText("That TE " + (!willSerial ? "will" : "will not") + " tick fully parallelised");
                                            cmdCtx.getSource().sendFeedback(message, true);
                                            return 1;
                                        }
                                        message = new LiteralText("That block doesn't contain a tickable TE!");
                                        cmdCtx.getSource().sendError(message);
                                        return 0;
                                    }
                                    message = new LiteralText("Only runable by player!");
                                    cmdCtx.getSource().sendError(message);
                                    return 0;
                                }))
                        )
                );
    }

    public static boolean isTickableBe(BlockEntity be) {
        BlockEntityTicker<?> blockEntityTicker = be.getCachedState().getBlockEntityTicker(be.getWorld(), be.getType());
        return blockEntityTicker != null;
    }
}
