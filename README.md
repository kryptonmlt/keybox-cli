# keybox-cli
CLI / Terminal mode for keybox (ssh manager) - https://github.com/signedbytes/KeyBox

## Latest Release
* Download latest release from here: https://github.com/kryptonmlt/keybox-cli/releases

## How to Run
* Once latest release is downloaded, fill up the application.properties file as needed
* keybox.qrCodeText can be left empty if it is the first time the keybox user you are using is going to login
* run the keybox-cli.bat or keybox-cli.sh

# Commands
* help
  * will show you all commands available
* list
  * list all servers in short format
* list -v
  * list more details
* inventory
  * generate ansible inventory file
* add -I ip -N name -U user -P port
  * adds a server to keybox
