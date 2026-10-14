package ai.qwenton.apex.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ai.qwenton.apex.data.QwentonDatabase
import ai.qwenton.apex.domain.QwentonRepository
import ai.qwenton.apex.network.WebSocketClient

class QwentonViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QwentonViewModel::class.java)) {
            val database = QwentonDatabase.getDatabase(context)
            val repository = QwentonRepository(
                conversationDao = database.conversationDao(),
                executionDao = database.executionDao()
            )
            val wsClient = WebSocketClient()
            return QwentonViewModel(repository, wsClient, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
