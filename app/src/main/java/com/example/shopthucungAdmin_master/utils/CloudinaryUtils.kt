import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import android.net.Uri
import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

object CloudinaryUtils {
    private val cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", "dvkty2ewp",
        "api_key", "654133236111989",
        "api_secret", "xyMMddSRjMP34CYBOhKImpo1TBA"
    ))

    // Hàm upload ảnh lên Cloudinary
    suspend fun uploadToCloudinary(imageUri: Uri, context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val filePath = getPathFromUri(imageUri, context)
            if (filePath == null) {
                return@withContext null
            }

            val file = File(filePath)
            if (!file.exists()) {
                return@withContext null
            }

            val params = ObjectUtils.asMap("resource_type", "image")
            val uploadResult = cloudinary.uploader().upload(file, params)
            val secureUrl = uploadResult["secure_url"] as? String

            return@withContext secureUrl
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // Lấy đường dẫn file từ URI
    private fun getPathFromUri(uri: Uri, context: Context): String? {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}
