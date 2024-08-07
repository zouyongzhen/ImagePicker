package zyz.hero.imagepicker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import zyz.hero.imagepicker.ext.pickResource
import zyz.hero.imagepicker.imageLoader.ImageLoader
import zyz.hero.imagepicker.sealeds.SelectType
import zyz.hero.imagepicker.ui.dialog.SimpleLoadingDialog

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 5:44 下午
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loadingDialog = SimpleLoadingDialog()
        findViewById<Button>(R.id.select).setOnClickListener { view ->
            pickResource {
                setSelectType(SelectType.Image)
                setMaxImageCount(6)
                setMaxVideoCount(9)
                setImageLoader(object :ImageLoader{
                    override fun load(context: Context, uri: Uri?, imageView: ImageView) {
                        Glide.with(context).load(uri).override(300).into(imageView)
                    }

                    override fun pauseRequests(context: Context) {
                        Glide.with(context).pauseRequests()
                    }

                    override fun resumeRequests(context: Context) {
                        Glide.with(context).resumeRequests()
                    }
                })
            }.asPath(showLoading = {
                loadingDialog.show(supportFragmentManager,null)
            }, hideLoading = {
                loadingDialog.dismiss()
            }){

            }.start(this)
        }
    }
}