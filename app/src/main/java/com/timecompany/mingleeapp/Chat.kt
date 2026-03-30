package com.timecompany.mingleeapp

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,          // null olabilir
    val lastTimestamp: Long = 0,
    val receiverName: String? = null,         // null olabilir
    val receiverImageUrl: String? = null      // null olabilir
)