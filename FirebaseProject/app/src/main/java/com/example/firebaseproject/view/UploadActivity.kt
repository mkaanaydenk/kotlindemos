package com.example.firebaseproject.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebaseproject.databinding.ActivityUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()
        auth= Firebase.auth
        firestore= Firebase.firestore
        storage= Firebase.storage
        binding.button2.visibility= View.VISIBLE
    }

    fun shareButton(view: View) {

        val uuid= UUID.randomUUID()
        val imageName= "$uuid.jpg"
        val reference= storage.reference
        val imageReference= reference.child("images/").child(imageName)

        if (selectedUri!=null){

            binding.button2.visibility= View.INVISIBLE

            imageReference.putFile(selectedUri!!).addOnSuccessListener {

                val downloadImageUrlReference= storage.reference.child("images").child(imageName)
                downloadImageUrlReference.downloadUrl.addOnSuccessListener {

                    val downloadUrl= it.toString()
                    if (auth.currentUser!=null){

                        val postMap= HashMap<String, Any>()
                        postMap.put("downloadUrl",downloadUrl)
                        postMap.put("userEmail",auth.currentUser!!.email!!)
                        postMap.put("comment",binding.commentText.text.toString())
                        postMap.put("date",Timestamp.now())

                        firestore.collection("Posts").add(postMap).addOnSuccessListener {

                            Toast.makeText(applicationContext,"Post paylaşıldı bebeğim!",Toast.LENGTH_LONG).show()
                            finish()

                        }.addOnFailureListener {

                            Toast.makeText(this@UploadActivity,"Bir hata oluştu.",Toast.LENGTH_LONG).show()

                        }

                    }

                }

            }.addOnFailureListener {

                Toast.makeText(this@UploadActivity,"Bir hata oluştu",Toast.LENGTH_LONG).show()

            }

        }

    }

    fun selectImageClicked(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {

                    Snackbar.make(
                        view,
                        "Galeri için izin vermeniz gerekir.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin ver", View.OnClickListener {

                        //req permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                    }).show()

                } else {
                    //req permission
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {
                //go to gallery
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        } else {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {

                    Snackbar.make(
                        view,
                        "Galeri için izin vermeniz gerekir.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin ver", View.OnClickListener {

                        //req permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

                    }).show()

                } else {
                    //req permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {
                //go to gallery
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        }


    }

    fun registerLauncher() {

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == RESULT_OK) {

                    val resultIntent = result.data
                    if (resultIntent != null) {

                        selectedUri= resultIntent.data
                        selectedUri?.let {

                            binding.selectImage.setImageURI(it)

                        }

                    }

                }

            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

                if (result) {

                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                } else {

                    Toast.makeText(
                        this@UploadActivity,
                        "Galeriye gitmek için izin vermeniz gerekir!",
                        Toast.LENGTH_LONG
                    ).show()

                }

            }

    }

}