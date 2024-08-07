package zyz.hero.imagepicker.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import zyz.hero.imagepicker.ImagePicker
import zyz.hero.imagepicker.PickConfig
import zyz.hero.imagepicker.R
import zyz.hero.imagepicker.ResBean
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.TYPE_VIDEO
import zyz.hero.imagepicker.ext.visible
import zyz.hero.imagepicker.sealeds.SelectType

/**
 * @author yongzhen_zou@163.com
 * @date 2021/8/29 1:34 上午
 */
class ImageAdapter(var context: Context, var pickConfig: PickConfig, val takePhoto: () -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_CAMARA = 0
        const val TYPE_RESOURCE = 1
    }

    var items = arrayListOf<ResBean>()
    var selectedData = arrayListOf<ResBean>()
    override fun getItemCount(): Int = items.size
    override fun getItemViewType(position: Int): Int {
        return if (items[position].isCamera) TYPE_CAMARA else TYPE_RESOURCE
    }

    fun concatItems(newItems: MutableList<ResBean>?) {
        if (newItems != null && newItems.size > 0) {
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    fun refreshItems(newItems: MutableList<ResBean>?) {
        if (newItems != null) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CAMARA) CameraHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_image_picker_camera, parent, false)
        ) else ImageHolder(
            LayoutInflater.from(context).inflate(R.layout.item_image_picker_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_CAMARA) {
            (holder as? CameraHolder)?.also {
                holder.itemView.setOnClickListener {
                    takePhoto.invoke()
                }
            }
        } else {
            (holder as? ImageHolder)?.apply {
                val imageBean = items[position]
                select.visible = when (pickConfig.selectType) {
                    is SelectType.Image -> pickConfig.maxImageCount > 1
                    is SelectType.Video -> pickConfig.maxVideoCount > 1
                    else -> true
                }
                durationLayout.visible = imageBean.type == TYPE_VIDEO
                if (imageBean.type == TYPE_VIDEO) {
                    val minutes = imageBean.duration / 1000 / 60
                    val seconds = imageBean.duration / 1000 % 60
                    durationText.text = "${minutes}:${if (seconds >= 10) seconds else "0$seconds"}"
                }
                loadRes(context, imageBean, image)
                if (imageBean.select) {
                    select.text = (selectedData.indexOf(imageBean) + 1).toString()
                    select.setBackgroundResource(R.drawable.image_picker_shape_select)
                    image.alpha = 0.6f
                } else {
                    image.alpha = 0.9f
                    select.text = null
                    select.setBackgroundResource(R.drawable.image_picker_shape_unselect)
                }
                select.setOnClickListener {
                    if (imageBean.select) {
                        imageBean.select = false
                        selectedData.remove(imageBean)
                        notifyItemChanged(position)
                        selectedData.filter { it.select }.forEach {
                            notifyItemChanged(items.indexOf(it))
                        }
                    } else {
                        if (imageBean.type == TYPE_IMG) {
                            if (selectedData.filter { it.type == TYPE_IMG }.size < pickConfig.maxImageCount) {
                                handleSelect(this, imageBean)
                            } else {
                                ImagePicker.log("Select up to ${pickConfig.maxImageCount} pictures")
                            }
                        } else {
                            if (selectedData.filter { it.type == TYPE_VIDEO }.size < pickConfig.maxVideoCount) {
                                handleSelect(this, imageBean)
                            } else {
                                ImagePicker.log("Select up to ${pickConfig.maxVideoCount} videos")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadRes(context: Context, resBean: ResBean, imageView: ImageView) {
        pickConfig.imageLoader?.run {
            load(context, getThumbUri(resBean), imageView)
        }
    }

    private fun getThumbUri(resBean: ResBean): Uri? {
        return (resBean.thumbnailUri) ?: resBean.uri
    }

    private fun handleSelect(holder: ImageHolder, imageBean: ResBean) {
        imageBean.select = true
        selectedData.add(imageBean)
        holder.select.text = (selectedData.indexOf(imageBean) + 1).toString()
        holder.select.setBackgroundResource(R.drawable.image_picker_shape_select)
        holder.image.alpha = 0.6f
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
        val durationLayout = itemView.findViewById<ConstraintLayout>(R.id.durationLayout)
        val durationText = itemView.findViewById<TextView>(R.id.durationText)
        val select = itemView.findViewById<TextView>(R.id.select)
    }

    class CameraHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}