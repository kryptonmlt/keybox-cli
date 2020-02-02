package org.kryptonmlt.keybox.keyboxcli.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class KeyboxPrompt implements PromptProvider {

  @Override
  public AttributedString getPrompt() {
    return new AttributedString("keybox-cli:>",
        AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE)
    );
  }
}
