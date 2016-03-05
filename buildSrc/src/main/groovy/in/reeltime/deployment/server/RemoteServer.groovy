package in.reeltime.deployment.server

import groovy.transform.ToString

@ToString(includeFields = true, includePackage = false)
class RemoteServer implements Server {

    private final String hostname
    private final int port

    RemoteServer(String hostname, int port) {
        this.hostname = hostname
        this.port = port
    }

    @Override
    String getHostname() {
        return hostname
    }

    @Override
    int getPort() {
        return port
    }
}
