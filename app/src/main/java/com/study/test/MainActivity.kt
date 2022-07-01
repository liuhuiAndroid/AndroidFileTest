package com.study.test

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import org.apache.commons.io.FileUtils
import java.io.*

/**
 * https://developer.android.com/training/data-storage/shared/media?hl=zh-cn#direct-file-paths
 * https://juejin.cn/post/7012262477828194340
 */
class MainActivity : AppCompatActivity() {

    private val requestDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result?.data?.data?.let { uri ->
                    val resolver = applicationContext.contentResolver
                    resolver.openInputStream(uri).use { stream ->
                        val inStream: InputStream? = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inStream)
                        val path = saveBitmap("1", bitmap, this)
                        val randomAccessFile = RandomAccessFile(path, "r")
                        if (randomAccessFile != null) {
                            Log.i("TAG", "TAG")
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val intent = Intent(
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
                    )
                    requestDataLauncher.launch(intent)
                }
            }
    }

    fun saveBitmap(name: String?, bm: Bitmap, context: Context): String {
        val targetPath: String? =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val saveFile = File(targetPath, name)
        return try {
            val saveImgOut = FileOutputStream(saveFile)
            bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut)
            saveImgOut.flush()
            saveImgOut.close()
            saveFile.absolutePath
        } catch (ex: IOException) {
            ex.printStackTrace()
            ""
        }
    }
}