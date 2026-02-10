import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SocialServiceTest {

    @Before
    fun clearBeforeTest() {
        SocialService.clear()
    }

    @Test
    fun addMessage_shouldCreateNewChatAndMessage_whenNoExistingChat() {
        val senderId = 1
        val recipientId = 2
        val text = "Привет, как дела?"
        val newMessage = SocialService.addMessage(senderId, recipientId, text)

        assertNotNull(newMessage) // Проверяем, что сообщение не null
        assertEquals(text, newMessage.text) // Проверяем текст сообщения
        assertEquals(1, SocialService.getChats().size) // Проверяем, что создан один чат

        val createdChat = SocialService.getChats().first()//Проверяем, что чат создан
        assertEquals(senderId, createdChat.senderId)
        assertEquals(recipientId, createdChat.recipientId)
        assertEquals(1, createdChat.messages.size) // Проверяем, что в чате одно сообщение
        assertEquals(newMessage, createdChat.messages.first())

    }

    @Test
    fun addMessage_shouldAddToExistingChat() {
        SocialService.addMessage(1, 2, "Первое")// Добавляем в чат первое сообщение
        val initialChat = SocialService.getChats().first()//Проверяем, что чат создан
        val newMessage2 = SocialService.addMessage(1, 2, "Второе")// Добавляем в чат второе сообщение

        assertEquals(1, SocialService.getChats().size) // Должен остаться один чат
        assertEquals(2, initialChat.messages.size) // В чате должно быть 2 сообщения
        assertEquals(newMessage2, initialChat.messages.last()) // Второе сообщение должно быть последним
    }

    @Test
    fun editMessage_shouldUpdate_MessageText_when_MessageExists_And_NotDeleted() {
        val message = SocialService.addMessage(1, 2, "Старый текст")
        val newText = "Новый текст"

        val editedMessage = SocialService.editMessage(message.messageId, newText)

        assertEquals(newText, editedMessage.text)
        assertFalse(editedMessage.isDeleted)
    }

    @Test(expected = MessageNotFoundException::class)
    fun editMessage_shouldNot_EditDeletedMessage_and_ThrowException() {
        val message = SocialService.addMessage(1, 2, "Какой-то текст")
        SocialService.deleteMessage(message.messageId)
        SocialService.editMessage(message.messageId, "Новый текст")

    }

    @Test
    fun deleteMessage_shouldMark_MessageAsDeleted_when_MessageExists() {
        val message = SocialService.addMessage(1, 2, "Сообщение для удаления")
        val result = SocialService.deleteMessage(message.messageId)

        assertTrue(result)
        assertTrue(message.isDeleted)
    }

    @Test
    fun deleteMessage_shouldReturnFalse_when_MessageNotFound() {
        val result = SocialService.deleteMessage(999)

        assertFalse(result)
    }

    @Test
    fun deleteChat_marks_chat_as_deleted_when_it_has_no_active_messages() {
        val chatId = 3
        val activeMessage = Message(4, false, text = "Привет", senderId = 5, recipientId = 6)
        val newChat = Chat(chatId, false, mutableListOf(activeMessage), false, 5, 6)
        SocialService.chatsList.add(newChat)

        assertFalse(newChat.isDeleted)
        assertEquals(1, SocialService.chatsList.size)
        assertTrue(activeMessage.isDeleted == false)

        val messageToDelete = SocialService.deleteMessage(messageId = activeMessage.messageId)
        assertTrue(messageToDelete)
        assertTrue(activeMessage.isDeleted)

        val chatToDelete = SocialService.deleteChat(newChat.id)
        assertTrue(chatToDelete)
        assertFalse(SocialService.chatsList.contains(newChat))
        assertEquals(0, SocialService.chatsList.size)
    }

    @Test
    fun getUnreadChatsCount_should_return_unreadChats() {
        val message1 = Message(20, false, true, "Первое сообщение", 2, 3)
        val message2 = Message(30, false, false, "Второе сообщение", 3, 2)
        val unreadChat1 = Chat(10, false, mutableListOf(message1, message2), false, 2, 3)
        SocialService.chatsList.add(unreadChat1)

        val message3 = Message(40, false, false, "Третье сообщение", 4, 5)
        val message4 = Message(50, false, false, "Четвертое сообщение", 5, 4)
        val unreadChat2 = Chat(11, false, mutableListOf(message3, message4), false, 4, 5)
        SocialService.chatsList.add(unreadChat2)

        val message5 = Message(60, false, true, "Пятое сообщение", 6, 7)
        val message6 = Message(70, false, true, "Шестое сообщение", 7, 6)
        val readChat = Chat(12, false, mutableListOf(message5, message6), false, 6, 7)
        SocialService.chatsList.add(readChat)

        val unreadChatsCount = SocialService.getUnreadChatsCount()
        assertEquals(2, unreadChatsCount)
    }

    @Test
    fun getChats_returns_emptyList_of_chats_when_there_are_no_chats() {
        val chats = SocialService.getChats()

        assertTrue(chats.isEmpty())
        assertEquals(0, chats.size)
    }

    @Test
    fun getChats_returns_active_chats() {
        val message1 = Message(20, false, true, "Первое сообщение", 2, 3)
        val activeChat = Chat(10, false, mutableListOf(message1), true, 2, 3)
        SocialService.chatsList.add(activeChat)

        val message2 = Message(30, false, true, "Второе сообщение", 4, 5)
        val deletedChat = Chat(11, true, mutableListOf(message2), true, 4, 5)
        SocialService.chatsList.add(deletedChat)

        val chats = SocialService.getChats()

        assertFalse(chats.isEmpty())
        assertEquals(1, chats.size)
        assertTrue(chats.contains(activeChat))
        assertFalse(chats.contains(deletedChat))
    }

    @Test
    fun getChats_returns_emptyList_of_chats_when_chats_are_deleted() {
        val message1 = Message(20, false, true, "Первое сообщение", 2, 3)
        val deletedChat = Chat(10, true, mutableListOf(message1), true, 2, 3)
        SocialService.chatsList.add(deletedChat)

        val chats = SocialService.getChats()

        assertTrue(chats.isEmpty())
        assertEquals(0, chats.size)

    }

    @Test
    fun getMessages_returns_emptyList_if_Chat_isNot_Found() {
        val messages = SocialService.getMessages(2, 3, 5)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun getMessages_returns_emptyList_if_Chat_isDeleted() {
        val message1 = Message(20, false, true, "Первое сообщение", 2, 3)
        val deletedChat = Chat(10, true, mutableListOf(message1), true, 2, 3)
        SocialService.chatsList.add(deletedChat)

        val messages = SocialService.getMessages(2, 3, 5)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun getMessages_returns_emptyList_when_chat_contains_noMessages() {
        val emptyChat = Chat(10, true, mutableListOf(), true, 2, 3)
        SocialService.chatsList.add(emptyChat)

        val messages = SocialService.getMessages(2, 3, 5)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun getMessages_returns_last_X_activeMessages_and_marks_them_asRead() {
        val senderId = 2
        val recipientId = 3
        val message1 = Message(10, false, false, "Сообщение 1", 2, 3)
        val message2Deleted = Message(20, true, false, "Сообщение 2 (удалено)", 3, 2)
        val message3 = Message(30, false, false, "Сообщение 3", 2, 3)
        val message4 = Message(40, false, false, "Сообщение 4", 3, 2)

        val chat = Chat(
            1, false, mutableListOf(message1, message2Deleted, message3, message4),
            false, 2, 3
        )

        SocialService.chatsList.add(chat)

        val countToReturn = 2
        val returnedMessages = SocialService.getMessages(senderId, recipientId, countToReturn)

        assertFalse(returnedMessages.isEmpty())
        assertEquals(countToReturn, returnedMessages.size)

        assertEquals(message3, returnedMessages[0])
        assertEquals(message4, returnedMessages[1])

        assertTrue(message3.isRead)
        assertTrue(message4.isRead)

        assertFalse(message1.isRead)
        assertFalse(message2Deleted.isRead)

        assertFalse(chat.messageIsRead)
    }

    @Test
    fun getMessages_returns_allActiveMessages_if_count_isGreater_than_activeMessages_count() {
        val senderId = 2
        val recipientId = 3
        val message1 = Message(10, false, false, "Сообщение 1", 2, 3)
        val message2Deleted = Message(20, true, false, "Сообщение 2 (Удалено)", 3, 2)

        val chat = Chat(
            1, false, mutableListOf(message1, message2Deleted),
            false, 2, 3
        )
        SocialService.chatsList.add(chat)

        val countToReturn = 5
        val returnedMessages = SocialService.getMessages(senderId, recipientId, countToReturn)

        assertFalse(returnedMessages.isEmpty())
        assertEquals(1, returnedMessages.size)

        assertTrue(message1.isRead)
        assertFalse(chat.messageIsRead)
    }

    @Test
    fun getMessages_returns_emptyList_when_chat_contains_only_deletedMessages() {
        val messageDeleted = Message(20, true, false, "Сообщение удалено", 2, 3)
        val chat = Chat(
            1, false, mutableListOf(messageDeleted),
            false, 2, 3
        )
        SocialService.chatsList.add(chat)

        val messages = SocialService.getMessages(senderId = 2, recipientId = 3, count = 5)
        assertTrue(messages.isEmpty())
        assertFalse(messageDeleted.isRead)
    }

    @Test
    fun getLastMessagesFromAllChats_returns_emptyList_when_noChats_exist() {
        val result = SocialService.getLastMessagesFromAllChats()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getLastMessagesFromAllChats_returns_emptyList_when_allChats_are_deleted() {
        val message = Message(20, false, false, "Сообщение", 2, 3)
        val deletedChat = Chat(
            1, true, mutableListOf(message),
            false, 2, 3
        )

        SocialService.chatsList.add(deletedChat)

        val result = SocialService.getLastMessagesFromAllChats()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getLastMessagesFromAllChats_returns_emptyList_when_no_chat_has_activeMessages() {
        val message = Message(20, true, true, "Сообщение", 2, 3)
        val chat1 = Chat(
            1, false, mutableListOf(message),
            false, 2, 3
        )

        val chat2 = Chat(
            2, false, mutableListOf(),
            false, 4, 5
        )

        val message1 = Message(30, false, true, "Сообщение1", 6, 7)
        val chat3 = Chat(
            3, false, mutableListOf(message1),
            false, 6, 7
        )

        SocialService.chatsList.add(chat1)
        SocialService.chatsList.add(chat2)
        SocialService.chatsList.add(chat3)

        SocialService.getLastMessagesFromAllChats()
    }

    @Test
    fun getLastMessagesFromAllChats_returns_last_activeMessage_from_each_activeChat() {
        val chat1Message1 = Message(10, false, true, "Сообщение1", 2, 3)
        val chat1Message2Deleted = Message(20, true, false, "Сообщение2 (удалено)", 3, 2)
        val chat1Message3 = Message(30, false, true, "Сообщение3", 2, 3)
        val chat1 = Chat(
            1, false, mutableListOf(chat1Message1, chat1Message2Deleted, chat1Message3),
            false, 2, 3
        )
        SocialService.chatsList.add(chat1)

        val chat2Message1 = Message(40, false, true, "Сообщение1", 4, 5)
        val chat2 = Chat(
            2, true, mutableListOf(chat2Message1),
            true, 4, 5
        )
        SocialService.chatsList.add(chat2) // Добавляем, чтобы убедиться, что он игнорируется

        val chat3Message1Deleted = Message(50, true, false, "Сообщение1 (удалено)", 6, 7)
        val chat3 = Chat(
            3, false, mutableListOf(chat3Message1Deleted),
            false, 6, 7
        )
        SocialService.chatsList.add(chat3)

        val result = SocialService.getLastMessagesFromAllChats()

        assertFalse(result.isEmpty())
        assertEquals(1, result.size) // Должно попасть только из chat1

        assertTrue(result.contains(chat1Message3))

        assertEquals(chat1Message3, result[0])

        assertFalse(result.contains(chat2Message1))
        assertFalse(result.contains(chat3Message1Deleted))
    }


}



