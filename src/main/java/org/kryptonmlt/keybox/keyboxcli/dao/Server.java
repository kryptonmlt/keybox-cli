package org.kryptonmlt.keybox.keyboxcli.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Server {

  private String name;
  private String username;
  private String ip;
  private String status;
}
