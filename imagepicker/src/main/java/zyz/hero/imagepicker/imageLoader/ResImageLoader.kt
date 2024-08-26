package zyz.hero.imagepicker.imageLoader

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import java.io.Serializable

/**
 * @author zouyongzhen
 * @date 2022/8/10 09:33
 */
abstract class ResImageLoader :Serializable {
    abstract fun load(context: Context, uri: Uri?, imageView: ImageView)
    open fun pauseRequests(context: Context){

    }
    open fun resumeRequests(context: Context){

    }
}