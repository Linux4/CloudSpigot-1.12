package org.spigotmc;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class RestartCommand extends Command
{

    public RestartCommand(String name)
    {
        super( name );
        this.description = "Restarts the server";
        this.usageMessage = "/restart";
        this.setPermission( "bukkit.command.restart" );
    }

    @SuppressWarnings("deprecation")
	@Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args)
    {
        if ( testPermission( sender ) )
        {
            MinecraftServer.getServer().processQueue.add( new Runnable()
            {
                @Override
                public void run()
                {
                    restart();
                }
            } );
        }
        return true;
    }

    public static void restart()
    {
        restart( new File( SpigotConfig.restartScript ) );
    }

    public static void restart(final File script)
    {
        AsyncCatcher.enabled = false; // Disable async catcher incase it interferes with us
        org.spigotmc.AsyncCatcher.shuttingDown = true; // Paper
        try
        {
            // Paper - extract method and cleanup
            boolean isRestarting = addShutdownHook(script);
            if (isRestarting) {
                System.out.println("Attempting to restart with " + SpigotConfig.restartScript);
            } else {
                System.out.println( "Startup script '" + SpigotConfig.restartScript + "' does not exist! Stopping server." );
            }

            // Stop the watchdog
            WatchdogThread.doStop();

            shutdownServer(isRestarting);
        } catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    // Paper start - sync copied from above with minor changes, async added
    @SuppressWarnings("deprecation")
	private static void shutdownServer(boolean isRestarting)
    {
        if (MinecraftServer.getServer().isMainThread())
        {
            // Kick all players
            for ( EntityPlayer p : com.google.common.collect.ImmutableList.copyOf( MinecraftServer.getServer().getPlayerList().players ) )
            {
                p.playerConnection.disconnect(SpigotConfig.restartMessage);
            }
            // Give the socket a chance to send the packets
            try
            {
                Thread.sleep( 100 );
            } catch ( InterruptedException ex )
            {
            }

            closeSocket();

            // Actually shutdown
            try
            {
                MinecraftServer.getServer().stop();
            } catch ( Throwable t )
            {
            }

            // Actually stop the JVM
            System.exit(0);

        } else
        {
            // Mark the server to shutdown at the end of the tick
            MinecraftServer.getServer().safeShutdown(isRestarting);


            // wait 10 seconds to see if we're actually going to try shutdown
            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException ignored)
            {
            }

            // Check if we've actually hit a state where the server is going to safely shutdown
            // if we have, let the server stop as usual
            if (MinecraftServer.getServer().isStopped()) return;

            // If the server hasn't stopped by now, assume worse case and kill
            closeSocket();
            System.exit( 0 );
        }
    }

    // Paper - Split from moved code
    @SuppressWarnings("deprecation")
	private static void closeSocket() {
        // Close the socket so we can rebind with the new process
        MinecraftServer.getServer().getServerConnection().b();

        // Give time for it to kick in
        try
        {
            Thread.sleep( 100 );
        } catch ( InterruptedException ex )
        {
        }
    }
    // Paper end

    // Paper - copied from above and modified to return if the hook registered
    private static boolean addShutdownHook(final File script) {

        if (script.isFile()) {
            Thread shutdownHook = new Thread() {
                @Override
                public void run() {
                    try {
                        String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ENGLISH);
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("cmd /c start " + script.getPath());
                        } else {
                            Runtime.getRuntime().exec(new String[]
                                    {
                                            "sh", script.getPath()
                                    });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            shutdownHook.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            return true;
        } else {
            return false;
        }
    }
}
