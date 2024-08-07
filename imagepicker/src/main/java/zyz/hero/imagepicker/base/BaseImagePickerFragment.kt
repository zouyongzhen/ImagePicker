package zyz.hero.imagepicker.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.coroutines.*
import zyz.hero.imagepicker.ResBean
import zyz.hero.imagepicker.PickConfig
import zyz.hero.imagepicker.R
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.sealeds.SelectType
import zyz.hero.imagepicker.ui.ImageAdapter
import zyz.hero.imagepicker.utils.ResUtils
import zyz.hero.imagepicker.utils.HelperFragment

/**
 * @author yongzhen_zou@163.com
 * @date 2021/9/6 8:59 下午
 */
abstract class BaseImagePickerFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private val pickConfig: PickConfig by lazy {
        arguments?.getSerializable("config") as PickConfig
    }
    var mediaType: SelectType? = null  //1：视频和图片、2：图片、3：视频
    var queryJob: Job? = null
    var mediaList = mutableListOf<ResBean>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mediaType = pickConfig.selectType
        return inflater.inflate(R.layout.fragment_imagepicker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 4)
        (recycler.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recycler.adapter = ImageAdapter(requireContext(), pickConfig) {
            takePhoto()
        }
        recycler.addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 停止滑动时，恢复加载
                        pickConfig.imageLoader?.resumeRequests(requireContext())
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING,
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        // 滑动时，暂停加载
                        pickConfig.imageLoader?.pauseRequests(requireContext())
                    }
                }
            }
        })
        initData()
    }

    private fun takePhoto() {
        HelperFragment.takePhoto(childFragmentManager) { data ->
            if (pickConfig.showCamara) {
                data?.let {
                    (recycler.adapter as ImageAdapter).apply {
                        items.add(1, ResBean(uri = it.uri, name = it.name, type = TYPE_IMG))
                        notifyItemInserted(1)
                    }
                }

            }
        }
    }

    private fun refreshData() {
        if (mediaList.isNotEmpty()) {
            if (pickConfig.showCamara) {
                mediaList.add(0, ResBean(isCamera = true))
            }
            (recycler.adapter as ImageAdapter).refreshItems(mediaList)
        }
    }

    private fun initData() {
        queryJob?.cancel()
        queryJob = lifecycleScope.launch {
            showLoading()
            mediaList.clear()
            withContext(Dispatchers.IO) {
                when (mediaType) {
                    is SelectType.All -> {
                        val images = async { ResUtils.getImageData(requireContext()) }
                        val videos = async { ResUtils.getVideoData(requireContext()) }
                        mediaList.addAll(images.await())
                        mediaList.addAll(videos.await())
                    }

                    is SelectType.Image -> {
                        val images = async { ResUtils.getImageData(requireContext()) }
                        mediaList.addAll(images.await())

                    }

                    is SelectType.Video -> {
                        val videos = async { ResUtils.getVideoData(requireContext()) }
                        mediaList.addAll(videos.await())

                    }

                    else -> {
                        val images = async { ResUtils.getImageData(requireContext()) }
                        mediaList.addAll(images.await())
                    }
                }
                mediaList.sortByDescending { it.date }
            }
            hideLoading()
            refreshData()
        }
    }


    fun complete() = (recycler.adapter as ImageAdapter).selectedData

    fun init(pickConfig: PickConfig): BaseImagePickerFragment {
        return this.apply {
            arguments = Bundle().apply {
                putSerializable("config", pickConfig)
            }
        }
    }

    override fun onDestroy() {
        queryJob?.cancel()
        super.onDestroy()
    }

    abstract fun hideLoading()

    abstract fun showLoading()
}