package ai.qwenton.apex.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class ContactEntry(
    val name: String,
    val number: String
)

object ContactsHelper {

    /**
     * Search contacts matching the given name query.
     */
    fun findContacts(context: Context, nameQuery: String): List<ContactEntry> {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return emptyList()

        val results = mutableListOf<ContactEntry>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$nameQuery%")

        try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext() && results.size < 5) {
                    val name = cursor.getString(nameIndex) ?: ""
                    val number = cursor.getString(numberIndex) ?: ""
                    results.add(ContactEntry(name = name, number = number))
                }
            }
        } catch (e: Throwable) {
            // Graceful error handling if query fails
        }

        return results
    }

    /**
     * Initiate a phone call to the target phone number.
     * Uses ACTION_CALL if CALL_PHONE permission is granted, otherwise falls back to ACTION_DIAL.
     */
    fun initiateCall(context: Context, phoneNumber: String): Result<String> {
        val sanitized = phoneNumber.replace(" ", "").replace("-", "")
        if (sanitized.isBlank()) return Result.failure(Exception("Phone number is blank."))

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        return try {
            val intent = if (hasCallPermission) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$sanitized"))
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Result.success("Initiated call to $sanitized (${if (hasCallPermission) "Direct Call" else "Dialer"})")
        } catch (e: Throwable) {
            Result.failure(Exception("Failed to initiate call: ${e.localizedMessage}"))
        }
    }
}
