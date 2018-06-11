package com.unifina.utils

class NetworkInterfaceUtils {
	static Inet4Address getIPAddress(List<String> prefixes=null) {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces()

		// Find non-loopback non-virtual non-p2p interface with at least one ipv4 address
		List<Inet4Address> candidates = []
		for (NetworkInterface it : interfaces.findAll {!(it.virtual || it.pointToPoint || it.loopback)}) {
			for (InetAddress addr : it.inetAddresses.findAll {it instanceof Inet4Address}) {
				if (prefixes == null || prefixes.isEmpty()) {
					return addr
				} else {
					candidates.add(addr)
				}
			}
		}
		
		// Search through candidates in the order specified by the prefixes
		for (def prefix : prefixes) {
			Inet4Address found = candidates.find {it.hostAddress.startsWith(prefix)}
			if (found) {
				return found
			}
		}
		
		throw new RuntimeException("IP address could not be determined! Prefixes: $prefixes")
	}

	static boolean isIpAddressOfCurrentNode(String ipAddress) {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces()
		for (NetworkInterface anInterface : interfaces.findAll {!(it.isVirtual() || it.isPointToPoint() )}) {
			for (InetAddress addr : anInterface.inetAddresses.findAll {it instanceof Inet4Address}) {
				if (addr.hostAddress.equals(ipAddress)) {
					return true
				}
			}
		}
		return false
	}
	
	public static void main(String[] args) {
		println getIPAddress()
		println getIPAddress(["192.168.10.", "192.168."])
	}
}