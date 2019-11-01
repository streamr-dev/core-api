package com.unifina.service

import com.unifina.utils.MapTraversal
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

class NodeService {

	public static final Logger log = Logger.getLogger(NodeService)

	GrailsApplication grailsApplication

	private String cachedIp = null

	String getIPAddress(Map config = grailsApplication.config) {
		// Return cachedIp if set
		if (cachedIp) {
			return cachedIp
		}

		// Check for a configured IP address
		String configuredIp = MapTraversal.getString(config, "streamr.engine.node.ip")
		if (configuredIp) {
			cachedIp = configuredIp
		} else {
			// Else search for a non-loopback non-virtual non-p2p interface with at least one ipv4 address
			List<NetworkInterface> interfaces = getNetworkInterfaces().findAll {
				!(it.isVirtual() || it.isPointToPoint() || it.isLoopback())
			}
			List<Inet4Address> addresses = interfaces.collect {
				it.inetAddresses.findAll { it instanceof Inet4Address }
			}.flatten()

			if (addresses.size() == 1) {
				cachedIp = addresses[0].hostAddress
			} else if (addresses.size() > 1) {
				log.warn("Multiple IPs found: ${addresses*.hostAddress}. By default the first one will be used. To select another aaddress, please set the streamr.engine.node.ip system property.")
				cachedIp = addresses[0].hostAddress
			} else {
				// Fallback to localhost
				log.warn("IP address could not be detected, falling back to 127.0.0.1. To specify an IP address for this node, please set the streamr.engine.node.ip system property.")
				cachedIp = "127.0.0.1"
			}
		}

		log.info("Using IP address: " + cachedIp)
		return cachedIp
	}

	boolean isIpAddressOfCurrentNode(String ipAddress, Map config = grailsApplication.config) {
		return getIPAddress(config).equals(ipAddress)
	}

	Enumeration<NetworkInterface> getNetworkInterfaces() {
		return NetworkInterface.getNetworkInterfaces()
	}
}
