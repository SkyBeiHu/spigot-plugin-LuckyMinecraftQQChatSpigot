package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public class ConnectionPacketSendUtil {
    public static Packet getConnectPacket(
            long sessionId,
            String serverName,
            String joinFormatString,
            String quitFormatString,
            String msgFormatString,
            String deathFormatString,
            String kickFormatString,
            List<String> onlinePlayersCommands,
            String onlinePlayersCommandResponseFormat,
            String onlinePlayersCommandResponseSeparator,
            String rconCommandPrefix,
            String rconCommandResultFormat
    ) {
        VarInt packetId = new VarInt(0x00);

        VarLong si = new VarLong(sessionId);
        VarIntString sn = new VarIntString(serverName);
        VarIntString jfs = new VarIntString(joinFormatString);
        VarIntString qfs = new VarIntString(quitFormatString);
        VarIntString mfs = new VarIntString(msgFormatString);
        VarIntString dfs = new VarIntString(deathFormatString);
        VarIntString kfs = new VarIntString(kickFormatString);

        VarInt opcc = new VarInt(onlinePlayersCommands.size());
        VarIntString[] opccs = new VarIntString[opcc.getValue()];
        for (int i = 0; i < opccs.length; i++) {
            opccs[i] = new VarIntString(onlinePlayersCommands.get(i));
        }
        VarIntString opcrf = new VarIntString(onlinePlayersCommandResponseFormat);
        VarIntString opcrs = new VarIntString(onlinePlayersCommandResponseSeparator);

        VarIntString rcp = new VarIntString(rconCommandPrefix);
        VarIntString rcrf = new VarIntString(rconCommandResultFormat);

        int totalLengthInt = packetId.getBytesLength() + si.getBytesLength() + sn.getBytesLength() +
                jfs.getBytesLength() + qfs.getBytesLength() + mfs.getBytesLength() + dfs.getBytesLength() +
                kfs.getBytesLength() + opcc.getBytesLength() + opcrf.getBytesLength() + opcrs.getBytesLength() +
                rcp.getBytesLength() + rcrf.getBytesLength();
        for (VarIntString opcca : opccs) {
            totalLengthInt += opcca.getBytesLength();
        }

        byte[] data = ByteUtil.byteMergeAll(
                si.getBytes(),
                sn.getBytes(),
                jfs.getBytes(),
                qfs.getBytes(),
                mfs.getBytes(),
                dfs.getBytes(),
                kfs.getBytes(),
                opcc.getBytes()
        );
        for (VarIntString opcca : opccs) {
            data = ByteUtil.byteMergeAll(data, opcca.getBytes());
        }
        data = ByteUtil.byteMergeAll(
                data,
                opcrf.getBytes(),
                opcrs.getBytes(),
                rcp.getBytes(),
                rcrf.getBytes()
        );

        return new Packet(
                new VarInt(totalLengthInt),
                packetId,
                data
        );
    }

    public static Packet getJoinPacket(String joinPlayer) {
        VarInt packetId = new VarInt(0x10);
        VarIntString joinPlayerString = new VarIntString(joinPlayer);
        return new Packet(new VarInt(packetId.getBytesLength() + joinPlayerString.getBytesLength()), packetId, joinPlayerString.getBytes());
    }

    public static Packet getQuitPacket(String quitPlayer) {
        VarInt packetId = new VarInt(0x11);
        VarIntString quitPlayerString = new VarIntString(quitPlayer);
        return new Packet(new VarInt(packetId.getBytesLength() + quitPlayerString.getBytesLength()), packetId, quitPlayerString.getBytes());
    }

    public static Packet getMessagePacket(String player, String message) {
        VarInt packetId = new VarInt(0x12);
        VarIntString playerString = new VarIntString(player);
        VarIntString messageString = new VarIntString(message);
        return new Packet(
                new VarInt(packetId.getBytesLength() + playerString.getBytesLength() + messageString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(playerString.getBytes(), messageString.getBytes())
        );
    }

    public static Packet getDeathMessagePacket(String player, String deathMessage) {
        VarInt packetId = new VarInt(0x13);
        VarIntString playerString = new VarIntString(player);
        VarIntString deathMessageString = new VarIntString(deathMessage);
        return new Packet(
                new VarInt(packetId.getBytesLength() + playerString.getBytesLength() + deathMessageString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(playerString.getBytes(), deathMessageString.getBytes())
        );
    }

    public static Packet getKickMessagePacket(String player, String kickReason) {
        VarInt packetId = new VarInt(0x14);
        VarIntString playerString = new VarIntString(player);
        VarIntString kickReasonString = new VarIntString(kickReason);
        return new Packet(
                new VarInt(packetId.getBytesLength() + playerString.getBytesLength() + kickReasonString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(playerString.getBytes(), kickReasonString.getBytes())
        );
    }

    public static Packet getClosePacket(String info) {
        VarInt packetId = new VarInt(0xF0);
        VarIntString infoString = new VarIntString(info);
        return new Packet(
                new VarInt(packetId.getBytesLength() + infoString.getBytesLength()),
                packetId,
                infoString.getBytes()
        );
    }

    public static Packet getPongPacket(long ping) {
        VarInt packetId = new VarInt(0x20);
        VarLong pong = new VarLong(ping);
        return new Packet(
                new VarInt(packetId.getBytesLength() + pong.getBytesLength()),
                packetId,
                pong.getBytes()
        );
    }

    public static Packet getOnlinePlayersPacket() throws VarIntStringLengthNotMatchException, IOException, VarIntTooBigException {
        VarInt packetId = new VarInt(0x21);
        Collection<? extends Player> onlinePlayerList =
                MinecraftMessageUtil.getOnlinePlayerList();
        VarInt onlinePlayersCount = new VarInt(onlinePlayerList.size());
        byte[][] onlinePlayerData = new byte[onlinePlayersCount.getValue()][];

        int index = 0;
        for (Player player : onlinePlayerList) {
            onlinePlayerData[index] = new VarIntString(player.getName()).getBytes();
            index += 1;
        }

        byte[] mergeAll = ByteUtil.byteMergeAll(onlinePlayerData);

        return new Packet(
                new VarInt(packetId.getBytesLength() + onlinePlayersCount.getBytesLength() + mergeAll.length),
                packetId,
                ByteUtil.byteMergeAll(onlinePlayersCount.getBytes(), mergeAll)
        );
    }

    public static Packet getRconCommandRefusedPacket(boolean rconEnable) {
        VarInt packetId = new VarInt(0x22);
        VarIntString commandResult;
        if (!rconEnable) {
            commandResult = new VarIntString("MC服务端未开启RCON指令操作，指令执行失败");
        } else {
            commandResult = new VarIntString("无操作权限");
        }
        return new Packet(
                new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                packetId,
                commandResult.getBytes()
        );
    }

    public static Packet getRconCommandResultPacket(boolean rconEnable, String command) {
        VarInt packetId = new VarInt(0x22);
        VarIntString commandResult;
        if (!rconEnable) {
            commandResult = new VarIntString("MC服务端未开启RCON指令操作，指令执行失败");
        } else {
            if (command.contains("reload")) {
                commandResult = new VarIntString("拒绝执行具有reload的指令");
            } else {
//                执行指令
                try {
                    commandResult = new VarIntString(RconUtil.sendMcCommad(command));
                } catch (Exception e) {
                    commandResult = new VarIntString("指令执行失败");
                }
            }
        }

        return new Packet(
                new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                packetId,
                commandResult.getBytes()
        );
    }


}
