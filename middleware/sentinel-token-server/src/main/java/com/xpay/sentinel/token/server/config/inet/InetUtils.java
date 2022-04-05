package com.xpay.sentinel.token.server.config.inet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InetUtils {
    private InetUtilsProperties properties;
    private final Log log = LogFactory.getLog(InetUtils.class);

    public InetUtils(InetUtilsProperties properties) {
        this.properties = properties;
    }

    public HostInfo findFirstNonLoopbackHostInfo() {
        InetAddress address = this.findFirstNonLoopbackAddress();
        if (address != null) {
            return this.convertAddress(address);
        } else {
            HostInfo hostInfo = new HostInfo();
            hostInfo.setHostname(this.properties.getDefaultHostname());
            hostInfo.setIpAddress(this.properties.getDefaultIpAddress());
            return hostInfo;
        }
    }

    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = 2147483647;
            Enumeration nics = NetworkInterface.getNetworkInterfaces();

            label61:
            while(true) {
                NetworkInterface ifc;
                do {
                    while(true) {
                        do {
                            if (!nics.hasMoreElements()) {
                                break label61;
                            }

                            ifc = (NetworkInterface)nics.nextElement();
                        } while(!ifc.isUp());

                        this.log.trace("Testing interface: " + ifc.getDisplayName());
                        if (ifc.getIndex() >= lowest && result != null) {
                            if (result != null) {
                                continue;
                            }
                            break;
                        }

                        lowest = ifc.getIndex();
                        break;
                    }
                } while(this.ignoreInterface(ifc.getDisplayName()));

                Enumeration addrs = ifc.getInetAddresses();

                while(addrs.hasMoreElements()) {
                    InetAddress address = (InetAddress)addrs.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress() && this.isPreferredAddress(address)) {
                        this.log.trace("Found non-loopback interface: " + ifc.getDisplayName());
                        result = address;
                    }
                }
            }
        } catch (IOException var8) {
            this.log.error("Cannot get first non-loopback address", var8);
        }

        if (result != null) {
            return result;
        } else {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException var7) {
                this.log.warn("Unable to retrieve localhost");
                return null;
            }
        }
    }

    boolean isPreferredAddress(InetAddress address) {
        if (this.properties.isUseOnlySiteLocalInterfaces()) {
            boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
                this.log.trace("Ignoring address: " + address.getHostAddress());
            }

            return siteLocalAddress;
        } else {
            List<String> preferredNetworks = this.properties.getPreferredNetworks();
            if (preferredNetworks.isEmpty()) {
                return true;
            } else {
                Iterator var3 = preferredNetworks.iterator();

                String regex;
                String hostAddress;
                do {
                    if (!var3.hasNext()) {
                        this.log.trace("Ignoring address: " + address.getHostAddress());
                        return false;
                    }

                    regex = (String)var3.next();
                    hostAddress = address.getHostAddress();
                } while(!hostAddress.matches(regex) && !hostAddress.startsWith(regex));

                return true;
            }
        }
    }

    boolean ignoreInterface(String interfaceName) {
        Iterator var2 = this.properties.getIgnoredInterfaces().iterator();

        String regex;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            regex = (String)var2.next();
        } while(!interfaceName.matches(regex));

        this.log.trace("Ignoring interface: " + interfaceName);
        return true;
    }

    public HostInfo convertAddress(final InetAddress address) {
        HostInfo hostInfo = new HostInfo();
        address.getClass();
        CompletableFuture result = CompletableFuture.supplyAsync(() -> address.getHostName());

        String hostname;
        try {
            hostname = (String)result.get((long)this.properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (Exception var6) {
            this.log.info("Cannot determine local hostname");
            hostname = "localhost";
        }

        hostInfo.setHostname(hostname);
        hostInfo.setIpAddress(address.getHostAddress());
        return hostInfo;
    }

    public static class HostInfo {
        public boolean override;
        private String ipAddress;
        private String hostname;

        public HostInfo(String hostname) {
            this.hostname = hostname;
        }

        public HostInfo() {
        }

        public int getIpAddressAsInt() {
            InetAddress inetAddress = null;
            String host = this.ipAddress;
            if (host == null) {
                host = this.hostname;
            }

            try {
                inetAddress = InetAddress.getByName(host);
            } catch (UnknownHostException var4) {
                throw new IllegalArgumentException(var4);
            }

            return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
        }

        public boolean isOverride() {
            return this.override;
        }

        public void setOverride(boolean override) {
            this.override = override;
        }

        public String getIpAddress() {
            return this.ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getHostname() {
            return this.hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }

    public static class InetUtilsProperties {
        private String defaultHostname = "localhost";
        private String defaultIpAddress = "127.0.0.1";
        private int timeoutSeconds = 1;
        private List<String> ignoredInterfaces = new ArrayList();
        private boolean useOnlySiteLocalInterfaces = false;
        private List<String> preferredNetworks = new ArrayList();

        public InetUtilsProperties() {
        }

        public String getDefaultHostname() {
            return this.defaultHostname;
        }

        public void setDefaultHostname(String defaultHostname) {
            this.defaultHostname = defaultHostname;
        }

        public String getDefaultIpAddress() {
            return this.defaultIpAddress;
        }

        public void setDefaultIpAddress(String defaultIpAddress) {
            this.defaultIpAddress = defaultIpAddress;
        }

        public int getTimeoutSeconds() {
            return this.timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public List<String> getIgnoredInterfaces() {
            return this.ignoredInterfaces;
        }

        public void setIgnoredInterfaces(List<String> ignoredInterfaces) {
            this.ignoredInterfaces = ignoredInterfaces;
        }

        public boolean isUseOnlySiteLocalInterfaces() {
            return this.useOnlySiteLocalInterfaces;
        }

        public void setUseOnlySiteLocalInterfaces(boolean useOnlySiteLocalInterfaces) {
            this.useOnlySiteLocalInterfaces = useOnlySiteLocalInterfaces;
        }

        public List<String> getPreferredNetworks() {
            return this.preferredNetworks;
        }

        public void setPreferredNetworks(List<String> preferredNetworks) {
            this.preferredNetworks = preferredNetworks;
        }
    }
}
