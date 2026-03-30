package com.timecompany.mingleeapp

data class ChatSummary(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val receiverName: String = "",
    val receiverImageUrl: String = ""
)