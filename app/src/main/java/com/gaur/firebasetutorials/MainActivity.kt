package com.gaur.firebasetutorials

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.gaur.firebasetutorials.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val storage = FirebaseStorage.getInstance()

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    this.makeToast("permission granted")
                }
            }
        }

    private val fileAccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.data?.let {
                val inputStream = this.contentResolver.openInputStream(it)
                inputStream?.readBytes()?.let {
                    uploadFile(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUploadFilesOnFirebase.setOnClickListener {
            sdkIntOverO(this) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                fileAccess.launch(intent)
            }
        }


    }

    fun uploadFile(byteArray: ByteArray) {
        val storageRef = storage.reference
        val storageRef2 = storageRef.child("images/${Date().time}")
        storageRef2.putBytes(byteArray)
            .addOnSuccessListener {
                this.makeToast("success")
                // taking the public url
                storageRef2.downloadUrl.addOnSuccessListener {
                    Log.d("TAG", "uploadFile: $it")
                }

            }
            .addOnFailureListener {
                this.makeToast("failure")
            }
            .addOnCompleteListener {
                this.makeToast("complete")
            }

    }

    fun sdkIntOverO(context: Context, call: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                call.invoke()
            } else {
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }


}

fun Context.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
