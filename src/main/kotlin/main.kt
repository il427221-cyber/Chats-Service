import java.lang.RuntimeException

data class Message(
    var messageId: Int,
    var isDeleted: Boolean,
    var isRead: Boolean = false,
    var text: String,
    val senderId: Int,
    val recipientId: Int
)

data class Chat(
    val id: Int,
    var isDeleted: Boolean,
    val messages: MutableList<Message>,
    var messageIsRead: Boolean,
    val senderId: Int,
    val recipientId: Int
)

class MessageNotFoundException(message: String) : RuntimeException()

object SocialService : ChatService {
    var messageList = mutableListOf<Message>()
    var chatsList = mutableListOf<Chat>()
    var nextChatId = 1
    var nextMessageId = 1

    fun clear() {
        messageList.clear()
        chatsList.clear()
        nextChatId = 1
        nextMessageId = 1
    }

    override fun addMessage(senderId: Int, recipientId: Int, text: String): Message {
        val newMessage = Message(
            messageId = nextMessageId++,
            isDeleted = false,
            isRead = false,
            text = text,
            senderId = senderId,
            recipientId = recipientId
        )
        val existingChat = chatsList.find {
            it.senderId == senderId && it.recipientId == recipientId
                    || it.senderId == recipientId && it.recipientId == senderId
        }

        if (existingChat != null) { // Если чат найден
            existingChat.messages.add(newMessage) // Добавляем сообщение в список сообщений чата
            existingChat.messageIsRead = true     // Устанавливаем флаг непрочитанных сообщений для чата
        } else { // Если чат не найден, создаём новый
            val newChat = Chat(
                id = nextChatId++,
                isDeleted = false,
                messages = mutableListOf(newMessage), // Новое сообщение - первое в чате
                messageIsRead = true, // Сразу помечаем как имеющий непрочитанные
                senderId = senderId, // Эти поля нужны для поиска чата в будущем
                recipientId = recipientId //
            )
            chatsList.add(newChat) // Добавляем новый чат в список всех чатов
        }

        return newMessage
    }

    override fun deleteMessage(messageId: Int): Boolean {
        val allMessages = chatsList.flatMap { it.messages }
        val targetMessage = allMessages.find { it.messageId == messageId }

        if (targetMessage != null) {
            targetMessage.isDeleted = true
            return true
        }
        return false
    }

    override fun editMessage(messageId: Int, newText: String): Message {
        val allMessages = chatsList.flatMap { it.messages }
        val targetMessage = allMessages.find { it.messageId == messageId }

        if (targetMessage != null && !targetMessage.isDeleted) {
            targetMessage.text = newText
            return targetMessage
        }
        throw MessageNotFoundException("Сообщение не найдено!")
    }

    override fun deleteChat(chatId: Int): Boolean {
        val chatToDelete = chatsList.find { it.id == chatId }

        if (chatToDelete != null) {
            val hasActiveMessages = chatToDelete.messages.any { !it.isDeleted }
            if (hasActiveMessages) {
                chatToDelete.isDeleted = true
                return true
            } else {
                chatsList.remove(chatToDelete)
                return true
            }
        }
        return false
    }

    override fun getChats(): List<Chat> {
        val existingChats = chatsList.filter { !it.isDeleted }
        return existingChats
    }

    override fun getMessages(senderId: Int, recipientId: Int, count: Int): List<Message> {
        val existingChat = chatsList.find {
            it.senderId == senderId && it.recipientId == recipientId
                    || it.senderId == recipientId && it.recipientId == senderId
        }

        if (existingChat == null || existingChat.isDeleted || existingChat.messages.isEmpty()) {
            return emptyList()
        }

        val activeMessages = existingChat.messages.filter { !it.isDeleted }
        val selectedMessages = activeMessages.takeLast(count)
        selectedMessages.forEach { message -> message.isRead = true }

        existingChat.messageIsRead = false

        return selectedMessages
    }

    override fun getUnreadChatsCount(): Int {
        val unreadChats = chatsList.filter { it.messages.any { !it.isRead } }
        return unreadChats.size
    }

    override fun getLastMessagesFromAllChats(): List<Message> {
        return chatsList.filter { !it.isDeleted }
            .mapNotNull { chat -> chat.messages.lastOrNull { !it.isDeleted } }
    }
}


fun main() {

}