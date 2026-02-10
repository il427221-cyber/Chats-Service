interface ChatService {
    fun getChats(): List<Chat>
    fun deleteChat(chatId: Int): Boolean
    fun getMessages(senderId: Int, recipientId: Int, count: Int): List<Message>
    fun getUnreadChatsCount(): Int
    fun addMessage(senderId: Int, recipientId: Int, text: String): Message
    fun deleteMessage(messageId: Int): Boolean
    fun editMessage(messageId: Int, newText: String): Message
    fun getLastMessagesFromAllChats(): List<Message>
}