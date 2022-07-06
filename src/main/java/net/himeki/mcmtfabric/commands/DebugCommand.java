package net.himeki.mcmtfabric.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.LocateCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.ConfiguredStructureFeatureTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DebugCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> registerDebug(LiteralArgumentBuilder<ServerCommandSource> root) {
        return root.then(literal("getBlockState")
                        .then(argument("location", Vec3ArgumentType.vec3()).executes(cmdCtx -> {
                            PosArgument loc = Vec3ArgumentType.getPosArgument(cmdCtx, "location");
                            BlockPos bp = loc.toAbsoluteBlockPos(cmdCtx.getSource());
                            ServerWorld sw = cmdCtx.getSource().getWorld();
                            BlockState bs = sw.getBlockState(bp);
                            LiteralText message = new LiteralText(
                                    "Block at " + bp + " is " + bs.getBlock().getName());
                            cmdCtx.getSource().sendFeedback(message, true);
                            System.out.println(message.toString());
                            return 1;
                        }))).then(literal("nbtdump")
                        .then(argument("location", Vec3ArgumentType.vec3()).executes(cmdCtx -> {
                            PosArgument loc = Vec3ArgumentType.getPosArgument(cmdCtx, "location");
                            BlockPos bp = loc.toAbsoluteBlockPos(cmdCtx.getSource());
                            ServerWorld sw = cmdCtx.getSource().getWorld();
                            BlockState bs = sw.getBlockState(bp);
                            BlockEntity te = sw.getBlockEntity(bp);
                            if (te == null) {
                                LiteralText message = new LiteralText(
                                        "Block at " + bp + " is " + bs.getBlock().getName() + " has no NBT");
                                cmdCtx.getSource().sendFeedback(message, true);
                                return 1;
                            }
                            NbtCompound nbt = te.toInitialChunkDataNbt();
                            String nbtStr = nbt.toString();
                            LiteralText message = new LiteralText(
                                    "Block at " + bp + " is " + bs.getBlock().getName() + " with TE NBT:");
                            cmdCtx.getSource().sendFeedback(message, true);
                            cmdCtx.getSource().sendFeedback(Text.of(nbtStr), true);
                            //System.out.println(message.toString());
                            return 1;
                        }))).then(literal("tick").requires(cmdSrc -> {
                            return cmdSrc.hasPermissionLevel(2);
                        }).then(literal("te"))
                        .then(argument("location", Vec3ArgumentType.vec3()).executes(cmdCtx -> {
                            PosArgument loc = Vec3ArgumentType.getPosArgument(cmdCtx, "location");
                            BlockPos bp = loc.toAbsoluteBlockPos(cmdCtx.getSource());
                            ServerWorld sw = cmdCtx.getSource().getWorld();
                            BlockEntity te = sw.getBlockEntity(bp);
                            if (te != null && ConfigCommand.isTickableBe(te)) {
                                ((BlockEntityTickInvoker) te).tick();
                                LiteralText message = new LiteralText(
                                        "Ticked " + te.getClass().getName() + " at " + bp);
                                cmdCtx.getSource().sendFeedback(message, true);
                            } else {
                                LiteralText message = new LiteralText("No tickable TE at " + bp);
                                cmdCtx.getSource().sendError(message);
                            }
                            return 1;
                        })))
                .then(literal("classpathDump").requires(cmdSrc -> {
                    return cmdSrc.hasPermissionLevel(2);
                }).executes(cmdCtx -> {
                    java.nio.file.Path base = Paths.get("classpath_dump/");
                    try {
                        Files.createDirectories(base);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    // Copypasta from syncfu;
                    Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).flatMap(path -> {
                                File file = new File(path);
                                if (file.isDirectory()) {
                                    return Arrays.stream(file.list((d, n) -> n.endsWith(".jar")));
                                }
                                return Arrays.stream(new String[]{path});
                            }).filter(s -> s.endsWith(".jar"))
                            .map(Paths::get).forEach(path -> {
                                Path name = path.getFileName();
                                try {
                                    Files.copy(path, Paths.get(base.toString(), name.toString()), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });


                    LiteralText message = new LiteralText("Classpath Dumped to: " + base.toAbsolutePath().toString());
                    cmdCtx.getSource().sendFeedback(message, true);
                    System.out.println(message.toString());
                    return 1;
                }))
                /* 1.16.1 code; AKA the only thing that changed  */
                .then(literal("test").requires(cmdSrc -> {
                    return cmdSrc.hasPermissionLevel(2);
                })/*.then(literal("structures").executes(cmdCtx -> {
                    ServerPlayerEntity p = cmdCtx.getSource().getPlayer();
                    BlockPos srcPos = p.getBlockPos();
                    UUID id = PlayerEntity.getUuidFromProfile(p.getGameProfile());
                    int index = structureIdx.computeIfAbsent(id.toString(), (s) -> new AtomicInteger()).getAndIncrement();
                    StructureFeature<?>[] targets = Registry.STRUCTURE_FEATURE.getEntrySet().toArray(new StructureFeature<?>[10]);
                    StructureFeature<?> target = null;
                    if (index >= targets.length) {
                        target = targets[0];
                        structureIdx.computeIfAbsent(id.toString(), (s) -> new AtomicInteger()).set(0);
                    } else {
                        target = targets[index];
                    }
                    BlockPos dst = cmdCtx.getSource().getWorld().locateStructure(ConfiguredStructureFeatureTags.of(target.toString()), srcPos, 100, false);
                    if (dst == null) {
                        LiteralText message = new LiteralText("Failed locating " + target + " from " + srcPos);
                        cmdCtx.getSource().sendFeedback(message, true);
                        return 1;
                    }
                    p.teleport(dst.getX(), srcPos.getY(), dst.getZ());
                    LocateCommand.sendCoordinates(cmdCtx.getSource(), target, srcPos, dst, "commands.locate.success");
                    return 1;
                })))*/
                /* */
				/*
				.then(literal("goinf").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).executes(cmdCtx -> {
					ServerPlayerEntity p = cmdCtx.getSource().asPlayer();
					p.setPosition(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
					return 1;
				}))
				*/
                );
    }

    private static Map<String, AtomicInteger> structureIdx = new ConcurrentHashMap<>();
}
