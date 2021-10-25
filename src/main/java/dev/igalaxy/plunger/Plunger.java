package dev.igalaxy.plunger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Plugin(
        id = "plunger",
        name = "Plunger",
        version = "1.0.0",
        description = "The only way to plunge your proxy servers!",
        url = "https://igalaxy.dev",
        authors = {"iGalaxy"}
)
public class Plunger {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private JSONArray serverList;

    @Inject
    public Plunger(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        loadConfig(dataDirectory);

        logger.info("Plunger initialized!");
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private void loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.json");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONParser jsonParser = new JSONParser();

            Object obj = jsonParser.parse(readFile(file.toPath().toString(), StandardCharsets.UTF_8));

            serverList = (JSONArray) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        serverList.forEach(serv -> {
            JSONObject serverData = (JSONObject) serv;
            Long port = (Long) serverData.get("port");
            server.registerServer(new ServerInfo((String) serverData.get("name"), new InetSocketAddress((String) serverData.get("hostname"), port.intValue())));
        });
    }
}