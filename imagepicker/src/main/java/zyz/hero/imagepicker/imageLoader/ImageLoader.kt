package zyz.hero.imagepicker.imageLoader

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import java.io.Serializable

/**
 * @author zouyongzhen
 * @date 2022/8/10 09:33
 */
interface ImageLoader :Serializable {
    fun load(context: Context, uri: Uri, imageView: ImageView)
}