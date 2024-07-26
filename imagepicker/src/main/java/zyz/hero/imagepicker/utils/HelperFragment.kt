package zyz.hero.imagepicker.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import zyz.hero.imagepicker.ResBean
import zyz.hero.imagepicker.TYPE_IMG
import zyz.hero.imagepicker.ui.ImagePickerActivity
import java.io.File


internal class HelperFragment : Fragment() {

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        try {
            if (requestCode == REQUEST_PERMISSION_CODE) {
                onPermissionResult?.invoke(grantResults.all { it == 0 })
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        } finally {
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        }

    }

    var onResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
    var captureResult: ((ResBean?) -> Unit)? = null
    var onPermissionResult: ((havePermission: Boolean) -> Unit)? = null
    var mFragmentManager: FragmentManager? = null

    companion object {
        fun startActivityForResult(
            fragmentManager: FragmentManager?,
            destination: Class<out AppCompatActivity>? = ImagePickerActivity::class.java,
            params: Bundle = Bundle(),
            onResult: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> },
        ) {
            fragmentManager ?: return kotlin.run {
                Log.e(TAG, "fragmentManager can not be null")
            }
            val tempFragment = HelperFragment()
            fragmentManager.beginTransaction().add(tempFragment, TAG + "_startActivityForResult")
                .commitNow()
            tempFragment.onResult = onResult
            tempFragment.mFragmentManager = fragmentManager
            tempFragment.startForResult(
                Intent(
                    tempFragment.activity,
                    destination
                ).putExtras(params)
            )
        }

        fun requestPermission(
            fragmentManager: FragmentManager?,
            vararg permissions: String,
            onPermissionResult: (havePermission: Boolean) -> Unit,
        ) {
            if (permissions.isEmpty()) {
                onPermissionResult(true)
                return
            }
            fragmentManager ?: return kotlin.run {
                Log.e(TAG, "fragmentManager can not be null")
            }
            val tempFragment = HelperFragment()
            fragmentManager.beginTransaction().add(tempFragment, TAG + "_requestPermission")
                .commitNow()
            tempFragment.onPermissionResult = onPermissionResult
            tempFragment.mFragmentManager = fragmentManager
            tempFragment.requestPermissions(permissions, REQUEST_PERMISSION_CODE)
        }

        fun takePhoto(
            fragmentManager: FragmentManager?,
            captureResult: (ResBean?) -> Unit = { },
        ) {
            fragmentManager ?: return kotlin.run {
                Log.e(TAG, "fragmentManager can not be null")
            }
            val tempFragment = HelperFragment()
            fragmentManager.beginTransaction().add(tempFragment, TAG + "_takePhoto").commitNow()
            tempFragment.captureResult = captureResult
            tempFragment.mFragmentManager = fragmentManager
            tempFragment.takePhoto()
        }

        private const val TAG = "TempFragment"
        private const val REQUEST_PERMISSION_CODE = 501
    }

    private val startForResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            onResult?.invoke(result.resultCode, result.data)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
        }
    }

    private fun startForResult(intent: Intent) {
        startForResultLauncher.launch(intent)
    }

    private var imageBean: ResBean? = null
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                captureResult?.invoke(imageBean)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mFragmentManager?.beginTransaction()?.remove(this)?.commitNow()
            }
        }
    }

    private fun takePhoto() {
        val externalFilesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return
        val fileDir = File(externalFilesDir.path)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        val mFilePath = fileDir.absolutePath + "/" + fileName;
        //
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        } else {
            values.put(MediaStore.Images.Media.DATA, mFilePath);
        }
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
        val uri =
            requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
        uri?.let {
            imageBean = ResBean(uri, fileName, TYPE_IMG)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
            takePhotoLauncher.launch(intent)
        }
    }

}