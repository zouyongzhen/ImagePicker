package zyz.hero.imagepicker.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import zyz.hero.imagepicker.ResBean
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.TYPE_VIDEO

/**
 * @author yongzhen_zou@163.com
 * @date 2021/12/8 7:46 下午
 */
object ResUtils {
    fun getImageData(context: Context): MutableList<ResBean> {
        val dataList = mutableListOf<ResBean>()
        val imageCursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.ImageColumns.DATE_ADDED} desc"
        )
        val imageIdList = mutableListOf<Long>()
        imageCursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val imageId = it.getLong(idColumn)
                imageIdList.add(imageId)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
                dataList.add(
                    ResBean(
                        uri = imageUri,
                        thumbnailUri = null, //后面更新这个字段
                        name = it.getString(displayNameColumn),
                        type = TYPE_IMG,
                        mimeType = it.getString(mimeTypeColumn),
                        date = it.getLong(dateAddedColumn)
                    )
                )
            }
            val thumbnails = getThumbnails(context, imageIdList)
            // 更新 dataList 中的缩略图 URI
            dataList.forEachIndexed { index, resBean ->
                resBean.thumbnailUri = thumbnails[imageIdList[index]]
            }
        }
        return dataList
    }

    fun getVideoData(context: Context): MutableList<ResBean> {
        val dataList = mutableListOf<ResBean>()
        val videoCursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Video.VideoColumns.DATE_ADDED} desc"
        )
        val videoIdList = mutableListOf<Long>()
        videoCursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val videoId = it.getLong(idColumn)
                videoIdList.add(videoId)
                val videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)
                dataList.add(
                    ResBean(
                        uri = videoUri,
                        thumbnailUri = null,//后面更新这个字段
                        name = it.getString(displayNameColumn),
                        type = TYPE_VIDEO,
                        duration = it.getLong(durationColumn),
                        mimeType = it.getString(mimeTypeColumn),
                        date = it.getLong(dateAddedColumn)
                    )
                )
                // 批量查询缩略图
                val thumbnails = getVideoThumbnails(context, videoIdList)
                // 使用 videoIdList 的索引来更新 dataList 中的缩略图 URI
                dataList.forEachIndexed { index, videoResBean ->
                    videoResBean.thumbnailUri = thumbnails[videoIdList[index]]
                }
            }
        }
        return dataList
    }

    private fun getThumbnails(context: Context, imageIdList: List<Long>): Map<Long, Uri> {
        val thumbnails = mutableMapOf<Long, Uri>()

        val thumbnailCursor = context.contentResolver.query(
            MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID),
            "${MediaStore.Images.Thumbnails.IMAGE_ID} IN (${imageIdList.joinToString(",")})",
            null,
            null
        )

        thumbnailCursor?.use {
            val thumbIdColumn = it.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID)
            val imageIdColumn = it.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID)

            while (it.moveToNext()) {
                val thumbId = it.getLong(thumbIdColumn)
                val imageId = it.getLong(imageIdColumn)
                val thumbnailUri = ContentUris.withAppendedId(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, thumbId)
                thumbnails[imageId] = thumbnailUri
            }
        }

        return thumbnails
    }
    private fun getVideoThumbnails(context: Context, videoIdList: List<Long>): Map<Long, Uri> {
        val thumbnails = mutableMapOf<Long, Uri>()

        val thumbnailCursor = context.contentResolver.query(
            MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Thumbnails._ID, MediaStore.Video.Thumbnails.VIDEO_ID),
            "${MediaStore.Video.Thumbnails.VIDEO_ID} IN (${videoIdList.joinToString(",")})",
            null,
            null
        )

        thumbnailCursor?.use {
            val thumbIdColumn = it.getColumnIndexOrThrow(MediaStore.Video.Thumbnails._ID)
            val videoIdColumn = it.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.VIDEO_ID)

            while (it.moveToNext()) {
                val thumbId = it.getLong(thumbIdColumn)
                val videoId = it.getLong(videoIdColumn)
                val thumbnailUri = ContentUris.withAppendedId(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbId)
                thumbnails[videoId] = thumbnailUri
            }
        }

        return thumbnails
    }
}