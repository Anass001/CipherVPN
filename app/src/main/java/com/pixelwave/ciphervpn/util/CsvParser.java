package com.pixelwave.ciphervpn.util;

import android.util.Base64;

import com.pixelwave.ciphervpn.data.model.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class CsvParser {

    private static final int HOST_NAME = 0;
    private static final int IP_ADDRESS = 1;
    private static final int SCORE = 2;
    private static final int PING = 3;
    private static final int SPEED = 4;
    private static final int COUNTRY_LONG = 5;
    private static final int COUNTRY_SHORT = 6;
    private static final int VPN_SESSION = 7;
    private static final int UPTIME = 8;
    private static final int TOTAL_USERS = 9;
    private static final int TOTAL_TRAFFIC = 10;
    private static final int LOG_TYPE = 11;
    private static final int OPERATOR = 12;
    private static final int MESSAGE = 13;
    private static final int OVPN_CONFIG_DATA = 14;
    private static final int PORT_INDEX = 2;
    private static final int PROTOCOL_INDEX = 1;

    public static Server stringToServer(String line) {
        String[] vpn = line.split(",");

        return new Server(vpn[HOST_NAME],
                vpn[IP_ADDRESS],
                Integer.parseInt(vpn[SCORE]),
                vpn[PING],
                Long.parseLong(vpn[SPEED]),
                vpn[COUNTRY_LONG],
                vpn[COUNTRY_SHORT],
                Long.parseLong(vpn[VPN_SESSION]),
                Long.parseLong(vpn[UPTIME]),
                Long.parseLong(vpn[TOTAL_USERS]),
                vpn[TOTAL_TRAFFIC],
                vpn[LOG_TYPE],
                vpn[OPERATOR],
                vpn[MESSAGE],
                new String(Base64.decode(vpn[OVPN_CONFIG_DATA], Base64.DEFAULT)),
                getPort(vpn[OVPN_CONFIG_DATA].split("[\\r\\n]+")),
                getProtocol(vpn[OVPN_CONFIG_DATA].split("[\\r\\n]+")),
                false);
    }

    public static List<Server> parse(ResponseBody response) {
        List<Server> servers = new ArrayList<>();
        InputStream in = null;
        BufferedReader reader = null;

        try {
            in = response.byteStream();
            reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("*") && !line.startsWith("#")) {
                    servers.add(stringToServer(line));
                }
            }

        } catch (IOException ignored) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (in != null)
                    in.close();
            } catch (IOException ignored) {
            }
        }
        return servers;
    }

    /**
     * @return Port used in OVPN file ("remote <HOSTNAME> <PORT>")
     */
    private static int getPort(String[] lines) {
        int port = 0;
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("remote")) {
                    port = Integer.parseInt(line.split(" ")[PORT_INDEX]);
                    break;
                }
            }
        }
        return port;
    }

    /**
     * @return Protocol used in OVPN file. ("proto <TCP/UDP>")
     */
    private static String getProtocol(String[] lines) {
        String protocol = "";
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("proto")) {
                    protocol = line.split(" ")[PROTOCOL_INDEX];
                    break;
                }
            }
        }
        return protocol;
    }
}
