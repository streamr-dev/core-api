package com.unifina.utils

import grails.util.Holders
import org.apache.log4j.Logger

class NetworkInterfaceUtils {

	public static final Logger log = Logger.getLogger(NetworkInterfaceUtils)

	private static String cachedIp = null

	static String getIPAddress(Map config = Holders.getConfig()) {
		// Return cachedIp if set
		if (cachedIp) {
			return cachedIp
		}

		// Check for a configured IP address
		String configuredIp = config?.streamr?.node?.ip
		if (configuredIp) {
			cachedIp = configuredIp
		} else {
			// Else search for a non-loopback non-virtual non-p2p interface with at least one ipv4 address
			List<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces().findAll {
				!(it.isVirtual() || it.isPointToPoint() || it.isLoopback())
			}
			List<Inet4Address> addresses = interfaces.collect {
				it.inetAddresses.findAll { it instanceof Inet4Address }
			}.flatten()

			if (addresses.size() == 1) {
				cachedIp = addresses[0].hostAddress
			} else if (addresses.size() > 1) {
				log.warn("Multiple IPs found: ${addresses*.hostAddress}. To select which address to use, please set the streamr.node.ip system property.")
				cachedIp = addresses[0].hostAddress
			} else {
				// Fallback to localhost
				log.warn("IP address could not be detected, falling back to 127.0.0.1. To specify an IP address for this node, please set the streamr.node.ip system property.")
				cachedIp = "127.0.0.1"
			}
		}

		return cachedIp
	}

	static boolean isIpAddressOfCurrentNode(String ipAddress, Map config = Holders.getConfig()) {
		return getIPAddress(config).equals(ipAddress)
	}

}
