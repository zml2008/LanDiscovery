/**
 * This file is part of LanDiscovery.
 *
 * LanDiscovery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LanDiscovery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with LanDiscovery.  If not, see <http://www.gnu.org/licenses/>.
 */
package ninja.leaping.landiscovery;

import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * This class originally from Spout's Vanilla project, adapted for Sponge and changes in Minecraft.
 * The original file from Spout is Copyright (c) 2011 Spout LLC <http://www.spout.org/>, available under the terms of the MIT license
 */
class LanThread extends Thread {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    /**
     * broadcast interval in seconds
     */
    private static final int BROADCAST_INTERVAL = 2;
    private static final InetAddress BROADCAST_ADDRESS;
    private static final int BROADCAST_PORT = 4445;

    static {
        try {
            BROADCAST_ADDRESS = InetAddress.getByName("224.0.2.60");
        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final LanDiscoveryPlugin plugin;
    private MulticastSocket socket;
    private int broadcastInterval = BROADCAST_INTERVAL;

    public LanThread(LanDiscoveryPlugin plugin) {
        super("LAN Discovery");
        setDaemon(true);
        this.plugin = plugin;
    }

    public void start() {
        try {
            socket = new MulticastSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.start();
    }

    private byte[] getContents() {
        // format: [MOTD]<motd in legacy formatting>[/MOTD][AD]<port number>[/AD]
        return String.format("[MOTD]%s[/MOTD][AD]%d[/AD]",
                TextSerializers.LEGACY_FORMATTING_CODE.serialize(plugin.getGame().getServer().getMotd()),
                plugin.getGame().getServer().getBoundAddress().get().getPort()).getBytes(UTF8_CHARSET);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            if (plugin.isMuted()) {
                continue;
            }

            try {
                byte[] contents = getContents();
                socket.send(new DatagramPacket(contents, contents.length, BROADCAST_ADDRESS, BROADCAST_PORT));
                broadcastInterval = BROADCAST_INTERVAL; // reset to default once we can successfully send a broadcast
            } catch (IOException e) {
                broadcastInterval *= 2;
                plugin.getLogger().error("Error sending out discovery broadcast, increasing delay to " + broadcastInterval + ": " + e.getLocalizedMessage(), e);
            }

            try {
                Thread.sleep(broadcastInterval * 1000);
            } catch (InterruptedException e1) {
                break;
            }
        }
    }
}
