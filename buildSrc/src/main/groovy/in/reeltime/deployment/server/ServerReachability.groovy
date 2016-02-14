package in.reeltime.deployment.server

import static in.reeltime.deployment.log.StatusLogger.*

class ServerReachability {

    private static final int POLL_INTERVAL_SECONDS = 20
    private static final int MAX_ATTEMPTS = 12

    static boolean waitUntilReachable(Server server) {
        validateServer(server)
        displayStatus("Waiting for server [${server}] to be reachable")

        int attempt = 0
        boolean reachable = isServerReachable(server)

        while (!reachable && attempt < MAX_ATTEMPTS) {
            displayStatus("Server unreachable. Waiting ${POLL_INTERVAL_SECONDS} seconds before trying again...")
            sleep(POLL_INTERVAL_SECONDS * 1000)
            reachable = isServerReachable(server)
        }
        return reachable
    }

    private static void validateServer(Server server) {
        if (!server.hostname) {
            throw new IllegalArgumentException("Server hostname must be specified")
        }
        if (server.port <= 0 || server.port > 65535) {
            throw new IllegalArgumentException("Server port number [${server.port}] is invalid")
        }
    }

    private static boolean isServerReachable(Server server) {
        Socket socket = null
        boolean reachable = false

        try {
            socket = new Socket(server.hostname, server.port)
            reachable = true
        }
        catch (Exception e) {
            displayStatus("Failed to open socket: " + e)
        }
        finally {
            if (socket != null) {
                try {
                    socket.close()
                }
                catch (IOException e) {
                    displayStatus("Failed to close socket" + e)
                }
            }
        }
        return reachable
    }
}
