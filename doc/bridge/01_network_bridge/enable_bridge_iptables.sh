#!/bin/sh
echo 1 > /proc/sys/net/ipv4/ip_forward
iptables -t nat -A POSTROUTING -s 192.168.1.0/24 -j MASQUERADE 
iptables -A FORWARD -i p2p1 -o em1 -j ACCEPT
iptables -A FORWARD -o em1 -i p2p1 -j ACCEPT

exit 0;
