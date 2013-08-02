package net.crafthub.noobidoo;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.reconnect.AbstractReconnectManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: Noobs
 * Date: 2.8.2013
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class MOTDListener implements Listener {

    private int timeout = 4000;

    private int pingVersion = -1;
    private Byte protocolVersion = -1;
    private String gameVersion;
    private String motd;
    private int playersOnline = -1;
    private int maxPlayers = -1;

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return this.timeout;
    }

    private void setPingVersion(int pingVersion) {
        this.pingVersion = pingVersion;
    }

    public int getPingVersion() {
        return this.pingVersion;
    }

    private void setProtocolVersion(Byte protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Byte getProtocolVersion() {
        return this.protocolVersion;
    }

    private void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getGameVersion() {
        return this.gameVersion;
    }

    private void setMotd(String motd) {
        this.motd = motd;
    }

    public String getMotd() {
        return this.motd;
    }

    private void setPlayersOnline(int playersOnline) {
        this.playersOnline = playersOnline;
    }

    public int getPlayersOnline() {
        return this.playersOnline;
    }

    private void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @EventHandler
    public void ProxyPingEvent(ProxyPingEvent ev) {
        PendingConnection connection = ev.getConnection();
        ServerInfo forced = AbstractReconnectManager.getForcedHost(connection);

        ServerPing r = ev.getResponse();
        this.setProtocolVersion(r.getProtocolVersion());
        this.setGameVersion(r.getGameVersion());
        this.setMotd(r.getMotd());
        this.setPlayersOnline(r.getCurrentPlayers());
        this.setMaxPlayers(r.getMaxPlayers());


        if(forced != null){
            this.fetchData(forced.getAddress());
        }

        ServerPing BF = new ServerPing(this.getProtocolVersion().byteValue(), this.getGameVersion(), this.getMotd(), this.getPlayersOnline(), this.getMaxPlayers());
        ev.setResponse(BF);
    }

    public boolean fetchData(InetSocketAddress adress) {
        try {
            Socket socket = new Socket();
            OutputStream outputStream;
            DataOutputStream dataOutputStream;
            InputStream inputStream;
            InputStreamReader inputStreamReader;

            socket.setSoTimeout(this.timeout);

            socket.connect(adress,this.getTimeout());

            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);

            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-16BE"));

            dataOutputStream.write(new byte[]{
                    (byte) 0xFE,
                    (byte) 0x01
            });

            int packetId = inputStream.read();

            if (packetId == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (packetId != 0xFF) {
                throw new IOException("Invalid packet ID (" + packetId + ").");
            }

            int length = inputStreamReader.read();

            if (length == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (length == 0) {
                throw new IOException("Invalid string length.");
            }

            char[] chars = new char[length];

            if (inputStreamReader.read(chars,0,length) != length) {
                throw new IOException("Premature end of stream.");
            }

            String string = new String(chars);


            if (string.startsWith("§")) {
                String[] data = string.split("\0");
                this.setPingVersion(Integer.parseInt(data[0].substring(1)));
                this.setProtocolVersion(Byte.valueOf(data[1]));
                this.setGameVersion(data[2]);
                this.setMotd(data[3]);
                this.setPlayersOnline(Integer.parseInt(data[4]));
                this.setMaxPlayers(Integer.parseInt(data[5]));
            } else {
                String[] data = string.split("§");

                this.setMotd(data[0]);
                this.setPlayersOnline(Integer.parseInt(data[1]));
                this.setMaxPlayers(Integer.parseInt(data[2]));
            }

            dataOutputStream.close();
            outputStream.close();

            inputStreamReader.close();
            inputStream.close();

            socket.close();
        } catch (SocketException exception) {
            return false;
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            return false;
        }

        return true;
    }
}

