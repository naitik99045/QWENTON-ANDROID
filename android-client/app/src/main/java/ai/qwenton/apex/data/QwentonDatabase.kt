package ai.qwenton.apex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MessageEntity::class, ExecutionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QwentonDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun executionDao(): ExecutionDao

    companion object {
        @Volatile
        private var INSTANCE: QwentonDatabase? = null

        fun getDatabase(context: Context): QwentonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QwentonDatabase::class.java,
                    "qwenton_apex.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
