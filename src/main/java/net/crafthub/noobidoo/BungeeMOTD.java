package net.crafthub.noobidoo; /**
 * User: Noobs
 * Date: 2.8.2013
 * Time: 17:08
 */

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMOTD  extends Plugin{
    public void onEnable()
    {
        System.out.println("******** [INFO] =================== BungeeMOTD v0.1 ===============");
        System.out.println("******** [INFO] [BadCommands]  BungeeMOTD by noobs has been enabled!");
        System.out.println("******** [INFO] [BadCommands]  Thank you for using this Plugin!");
        System.out.println("******** [INFO] ====================== By noobs =====================");

        ProxyServer.getInstance().getPluginManager().registerListener(this,new MOTDListener());
    }
}
