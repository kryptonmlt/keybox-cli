package org.kryptonmlt.keybox.keyboxcli.utils;

import java.util.Collection;
import java.util.HashMap;
import org.kryptonmlt.keybox.keyboxcli.dao.Server;
import org.springframework.stereotype.Component;

@Component
public class ServerCache {

  private HashMap<String, Server> servers = new HashMap<>();

  public void addServer(String name, String username, String ip, String port, String status) {
    servers.put(name, new Server(name, username, ip, port, status));
  }

  public void clearServers() {
    servers.clear();
  }

  public Collection<Server> getServers() {
    return servers.values();
  }
}
