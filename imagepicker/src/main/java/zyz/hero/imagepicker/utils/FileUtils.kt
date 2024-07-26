package zyz.hero.imagepicker.utils

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import zyz.hero.imagepicker.ImagePicker
import zyz.hero.imagepicker.ResBean
import java.io.File
import java.io.FileOutputStream

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/7 5:52 下午
 */
internal class FileUtils {
    companion object {
        suspend fun uriToFile(
            activity: FragmentActivity,
            dataList: ArrayList<ResBean>,
        ) = withContext(Dispatchers.IO) {
            return@withContext dataList.map {
                async {
                    activity.contentResolver.openInputStream(it.uri!!).use { inputStream ->
                        val dir = File(ImagePicker.getTempDir(activity))
                        dir.mkdirs()
                        return@async File(ImagePicker.getTempDir(activity) + it.name).apply {
                            if (exists()) {
                                delete()
                            }
                            FileOutputStream(this).use { outStream ->
                                inputStream?.copyTo(outStream)
                            }
                        }
                    }
                }
            }.mapTo(arrayListOf()) {
                it.await()
            }
        }
    }
}