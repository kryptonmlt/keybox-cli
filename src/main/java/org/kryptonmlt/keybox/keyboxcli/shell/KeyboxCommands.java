package org.kryptonmlt.keybox.keyboxcli.shell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.kryptonmlt.keybox.keyboxcli.dao.Server;
import org.kryptonmlt.keybox.keyboxcli.utils.ChromeHelper;
import org.kryptonmlt.keybox.keyboxcli.utils.KeyboxUtilities;
import org.kryptonmlt.keybox.keyboxcli.utils.ServerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class KeyboxCommands {

  private final static Logger LOGGER = LoggerFactory.getLogger(KeyboxCommands.class);

  @Autowired
  private ServerCache serverCache;
  @Autowired
  private KeyboxUtilities keyboxUtilities;
  @Autowired
  private ChromeHelper chromeHelper;
  @Value("${inventory.ignore}")
  private String[] inventoryIgnore;

  @ShellMethod("Lists all servers from local cache")
  public String list(@ShellOption({"-v", "--verbose"}) boolean verbose,
      @ShellOption(defaultValue = "") String grep) {
    if (serverCache.getServers().isEmpty()) {
      keyboxUtilities.extractInfoFromSystemsPage();
    }
    StringBuilder sB = new StringBuilder();
    for (Server server : serverCache.getServers()) {

      if ((grep == null || grep.isEmpty()) || server.getName().contains(grep)) {
        sB.append(server.getName());
        if (verbose) {
          sB.append(" - ");
          sB.append(server.getUsername());
          sB.append("@");
          sB.append(server.getIp());
          sB.append(":");
          sB.append(server.getPort());
        }
        sB.append("\n");
      }
    }
    return sB.toString();
  }

  @ShellMethod("Generates inventory file for ansible")
  public String inventory() {
    if (serverCache.getServers().isEmpty()) {
      keyboxUtilities.extractInfoFromSystemsPage();
    }
    Map<String, Integer> countMap = new HashMap<>();
    Collection<Server> servers = serverCache.getServers();
    for (Server server : servers) {
      String[] uniqueNames = splitOnItself(server.getName());
      for (String name : uniqueNames) {
        if (countMap.get(name) == null) {
          countMap.put(name, 0);
        }
        countMap.put(name, countMap.get(name) + 1);
      }
    }
    Map<String, List<Server>> inventoryMap = new HashMap<>();
    for (Server server : servers) {
      String[] uniqueNames = splitOnItself(server.getName());
      boolean other = true;
      for (String name : uniqueNames) {
        if (countMap.get(name) > 2) {
          other = false;
          if (inventoryMap.get(name) == null) {
            inventoryMap.put(name, new ArrayList<>());
          }
          inventoryMap.get(name).add(server);
        }
      }
      if (other) {
        if (inventoryMap.get("other") == null) {
          inventoryMap.put("other", new ArrayList<>());
        }
        inventoryMap.get("other").add(server);
      }
    }
    StringBuilder sB = new StringBuilder();
    for (String name : inventoryMap.keySet()) {
      List<Server> subServers = inventoryMap.get(name);
      sB.append("[");
      sB.append(name);
      sB.append("]\n");
      for (Server server : subServers) {
        sB.append(server.getName());
        sB.append(" ansible_port=");
        sB.append(server.getPort());
        sB.append(" ansible_host=");
        sB.append(server.getIp());
        sB.append("\n");
      }
      sB.append("\n");
    }
    try {
      File inv = new File("inventory");
      FileWriter fw = new FileWriter(inv);
      fw.write(sB.toString());
      fw.close();
      return "Inventory generated in: " + inv.getAbsolutePath();
    } catch (IOException e) {
      LOGGER.error("Error writing to file ", e);
      return "Error generating file";
    }
  }

  public String[] splitOnItself(String input) {
    String[] space = input.split(" ");
    String[] dash = input.split("-");
    String[] dot = input.split("\\.");
    String[] all = merge(space, dash, dot);
    Set<String> temp = new LinkedHashSet<>(Arrays.asList(all));
    for (String ignore : inventoryIgnore) {
      temp.remove(ignore);
    }
    List<String> checked = new ArrayList<>();
    for (String s : temp) {
      if (!isInteger(s)) {
        checked.add(s);
      }
    }
    return checked.toArray(new String[checked.size()]);
  }

  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
    // only got here if we didn't return false
    return true;
  }

  // Function to merge multiple arrays in Java 8
  public static String[] merge(String[]... arrays) {
    return Stream.of(arrays)
        .flatMap(Stream::of)    // or use Arrays::stream
        .toArray(String[]::new);
  }

  @ShellMethod("Reloads the local cache with the latest data in keybox")
  public String reload() {
    keyboxUtilities.extractInfoFromSystemsPage();
    return "Reloading done";
  }

  @ShellMethod("Add server to keybox")
  public String add(@ShellOption({"-N", "--name"}) String name,
      @ShellOption({"-U", "--username"}) String username,
      @ShellOption({"-I", "--ip"}) String ip,
      @ShellOption({"-P", "--port"}) String port) {
    if (serverCache.getServers().isEmpty()) {
      keyboxUtilities.extractInfoFromSystemsPage();
    }
    keyboxUtilities.addServer(name, username, ip, port);
    this.reload();
    return "Server added";
  }

  @ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
  public void quit() {
    chromeHelper.exit();
    System.exit(0);
  }
}
