package com.unifina.utils

class NetworkInterfaceUtils {
	public static Inet4Address getIPAddress() {
		//		InetAddress[] all = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())
		//		InetAddress ip = all.find {!it.isLoopbackAddress() && it.getHostAddress().indexOf(":")==-1}
		def interfaces = NetworkInterface.getNetworkInterfaces()

		// Find non-loopback non-virtual non-p2p interface with at least one ipv4 address
		def i = interfaces.find {
			!(it.virtual || it.pointToPoint || it.loopback) && it.inetAddresses.find {addr-> addr instanceof Inet4Address} != null
		}
		def ip = i.inetAddresses.find {addr-> addr instanceof Inet4Address}
		//		interfaces.each {
		//			println "-------"
		//			println "Name: $it.name"
		//			println "Parent: $it.parent"
		//			println "Loopback: $it.loopback"
		//			println "P2P: $it.pointToPoint"
		//			println "Virtual: $it.virtual"
		//			it.inetAddresses.each {addr->
		//				println "  address: $addr"
		//			}
		//		}
		
		return ip
	}
}
