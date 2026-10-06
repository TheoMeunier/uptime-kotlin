package tmenier.fr.common.enums.notifications

enum class NotificationChannelsEnum {
    DISCORD,
    MAIL,
    TEAMS,
    SLACK,
    WEBHOOK,

    // Stored by ordinal in notifications_channels.type: append new channels, never reorder.
    GOTIFY,
}
