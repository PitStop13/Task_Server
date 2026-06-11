package com.taskserver.app.data.model

import java.util.UUID

data class TaskCommand(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val order: Int,
    val isCustom: Boolean = true
)

// Comandi predefiniti per home lab Ubuntu/Docker
data class PredefinedCommand(
    val label: String,
    val command: String,
    val category: String,
    val requiresParam: Boolean = false,
    val paramHint: String = ""
)

val PREDEFINED_COMMANDS: List<PredefinedCommand> = listOf(
    // Docker
    PredefinedCommand("Docker PS", "docker ps", "Docker"),
    PredefinedCommand("Docker Images", "docker images", "Docker"),
    PredefinedCommand("Docker Pull", "docker pull IMAGE", "Docker", true, "IMAGE (es. nginx:latest)"),
    PredefinedCommand("Compose Up -d", "docker compose up -d", "Docker"),
    PredefinedCommand("Compose Down", "docker compose down", "Docker"),
    PredefinedCommand("Compose Restart", "docker compose restart", "Docker"),
    PredefinedCommand("Compose Logs", "docker compose logs -f --tail=100", "Docker"),
    PredefinedCommand("Container Logs", "docker logs -f --tail=50 CONTAINER", "Docker", true, "CONTAINER"),
    PredefinedCommand("Prune System", "docker system prune -af", "Docker"),
    // System
    PredefinedCommand("APT Update", "sudo apt update", "System"),
    PredefinedCommand("APT Upgrade", "sudo DEBIAN_FRONTEND=noninteractive apt upgrade -y", "System"),
    PredefinedCommand("APT Update+Upgrade", "sudo apt update && DEBIAN_FRONTEND=noninteractive apt upgrade -y", "System"),
    PredefinedCommand("APT Autoremove", "sudo apt autoremove -y", "System"),
    PredefinedCommand("Systemctl Restart", "sudo systemctl restart SERVICE", "System", true, "SERVICE"),
    PredefinedCommand("Systemctl Status", "sudo systemctl status SERVICE", "System", true, "SERVICE"),
    PredefinedCommand("Systemctl Start", "sudo systemctl start SERVICE", "System", true, "SERVICE"),
    PredefinedCommand("Systemctl Stop", "sudo systemctl stop SERVICE", "System", true, "SERVICE"),
    PredefinedCommand("Reboot", "sudo reboot", "System"),
    // Monitoring
    PredefinedCommand("Disk Usage", "df -h", "Monitoring"),
    PredefinedCommand("Memory Usage", "free -h", "Monitoring"),
    PredefinedCommand("CPU Top (1 ciclo)", "top -bn1 | head -30", "Monitoring"),
    PredefinedCommand("Processi attivi", "ps aux --sort=-%cpu | head -20", "Monitoring"),
    PredefinedCommand("Uptime", "uptime", "Monitoring"),
    PredefinedCommand("Network Stats", "ss -tuln", "Monitoring"),
    PredefinedCommand("Journal Errors", "sudo journalctl -p err --since '1 hour ago'", "Monitoring"),
    // Files
    PredefinedCommand("List Files", "ls -lah", "Files"),
    PredefinedCommand("Disk Usage Dir", "du -sh -- *", "Files"),
    PredefinedCommand("Find Large Files", "find / -xdev -size +100M -exec ls -lh {} \\;", "Files"),
    // Network
    PredefinedCommand("Ping Google", "ping -c 4 8.8.8.8", "Network"),
    PredefinedCommand("IP Address", "ip addr show", "Network"),
    PredefinedCommand("Netstat", "netstat -tlnp", "Network"),
)
