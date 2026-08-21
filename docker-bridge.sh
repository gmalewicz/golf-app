#!/bin/bash
# Bridges Windows-reachable TCP 2375 to the Ubuntu Docker unix socket for Testcontainers.
pkill -f "TCP-LISTEN:2375" 2>/dev/null
setsid socat TCP-LISTEN:2375,fork,reuseaddr,bind=0.0.0.0 UNIX-CONNECT:/var/run/docker.sock >/tmp/docker-bridge.log 2>&1 </dev/null &
disown
sleep 1
echo "LISTENING:"
ss -tlnp 2>/dev/null | grep 2375
echo "LOG:"
cat /tmp/docker-bridge.log 2>/dev/null
