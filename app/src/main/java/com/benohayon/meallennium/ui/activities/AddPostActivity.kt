package com.benohayon.meallennium.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.benohayon.meallennium.R
import com.benohayon.meallennium.framework.interfaces.OnImageLoadedListener
import com.benohayon.meallennium.framework.managers.FirebaseManager
import com.benohayon.meallennium.framework.managers.PermissionManager
import com.benohayon.meallennium.framework.managers.UserManager
import com.benohayon.meallennium.framework.models.IMAGE_CAPTURE_REQUEST
import com.benohayon.meallennium.framework.models.PICK_IMAGE_FROM_GALLERY_REQUEST
import com.benohayon.meallennium.framework.models.Post
import com.benohayon.meallennium.framework.models.TAKEN_PICTURE_URL_KEY
import com.benohayon.meallennium.framework.utils.PopupManager
import com.benohayon.meallennium.ui.custom_views.TopActionBar
import com.benohayon.meallennium.ui.custom_views.styled_views.StyledEditText
import com.benohayon.meallennium.ui.custom_views.styled_views.StyledTextView
import com.nostra13.universalimageloader.core.ImageLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddPostActivity : AppCompatActivity() {

    private lateinit var topActionBar: TopActionBar
    private lateinit var postTitleEditText: StyledEditText
    private lateinit var postSummeryEditText: StyledEditText
    private lateinit var postDescriptionEditText: StyledEditText
    private lateinit var takePictureButton: StyledTextView
    private lateinit var pickFromGalleryButton: StyledTextView
    private lateinit var imageContainer: ImageView
    private lateinit var createButton: StyledTextView
    private lateinit var cancelButton: StyledTextView
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null

    private val imageLoader = ImageLoader.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        initUI()
        initTopActionBar()
    }

    private fun initUI() {
        topActionBar = findViewById(R.id.addPostActivityAppBar)
        postTitleEditText = findViewById(R.id.addPostActivityPostTitleEditText)
        postSummeryEditText = findViewById(R.id.addPostActivityPostSummeryEditText)
        postDescriptionEditText = findViewById(R.id.addPostActivityPostDescriptionEditText)
        takePictureButton = findViewById(R.id.addPostActivityTakePictureButton)
        pickFromGalleryButton = findViewById(R.id.addPostActivityPickFromGalleryButton)
        imageContainer = findViewById(R.id.addPostActivityImageContainer)
        createButton = findViewById(R.id.addPostActivityCreateButton)
        cancelButton = findViewById(R.id.addPostActivityCancelButton)
        progressBar = findViewById(R.id.addPostActivityProgressBar)

        cancelButton.setOnClickListener {
            PopupManager.showDiscardDataMessage(this) {
                finish()
            }
        }

        createButton.setOnClickListener {
            // create that post in Firebase server
            clearFocus()
            if (fieldsValidated()) {
                progressBar.visibility = View.VISIBLE
                val newPost = Post(postTitleEditText.text.toString(), postSummeryEditText.text.toString(), postDescriptionEditText.text.toString(), null, UserManager.getName(this))
                createPost(newPost, imageUri, onSuccess = {
                    setResult(RESULT_OK)
                    finish()
                }, onFail = {
                    progressBar.visibility = View.INVISIBLE
                    PopupManager.showInformationPopup(this, getString(R.string.alert_post_creation_failure_title), it)
                })
            }
        }

        takePictureButton.setOnClickListener {
            if (PermissionManager.hasCameraPermission(this) && PermissionManager.hasWriteStoragePermission(this))
                takePicture()
            else {
                val permissions = ArrayList<String>()
                if (PermissionManager.doesNotHaveWriteStoragePermission(this))
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                if (PermissionManager.doesNotHaveCameraPermission(this))
                    permissions.add(Manifest.permission.CAMERA)

                PermissionManager.requestPermissions(this, permissions.toArray(arrayOfNulls(permissions.size)))
            }
        }

        pickFromGalleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun clearFocus() {
        postTitleEditText.clearFocus()
        postSummeryEditText.clearFocus()
        postDescriptionEditText.clearFocus()
    }

    private fun openGallery() {
        val pickFromGalleryIntent = Intent()
        pickFromGalleryIntent.type = "image/*"
        pickFromGalleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(pickFromGalleryIntent, "Select Picture"), PICK_IMAGE_FROM_GALLERY_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun fieldsValidated(): Boolean {
        var validated = true
        if (postTitleEditText.text.isEmpty()) {
            val colorStateList: ColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
            postTitleEditText.backgroundTintList = colorStateList
            validated = false
        }

        if (postSummeryEditText.text.isEmpty()) {
            val colorStateList: ColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
            postSummeryEditText.backgroundTintList = colorStateList
            validated = false
        }

        if (postDescriptionEditText.text.isEmpty()) {
            val colorStateList: ColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red))
            postDescriptionEditText.backgroundTintList = colorStateList
            validated = false
        }

        return validated
    }


    private fun createPost(post: Post, imageUri: Uri?, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        FirebaseManager.addPost(this, post, imageUri, getPictureImage(post, imageUri), onSuccess = {
            onSuccess()
        }, onFail = {
            onFail(it)
        })
    }

    private fun getPictureImage(post: Post, imageUri: Uri?): String? {
        return imageUri?.let {
            "${post.title}.${getFileExtension(imageUri)}"
        }
    }

    private fun initTopActionBar() {
        topActionBar.centerText = getString(R.string.add_post_activity_top_centre_text)
        topActionBar.setLeftButtonResource(R.drawable.back_icon_white)
        topActionBar.setLeftButtonOnClickListener {
            PopupManager.showDiscardDataMessage(this) {
                finish()
            }
        }
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(TAKEN_PICTURE_URL_KEY, imageUri)
                .also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST)
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            imageContainer.background.clearColorFilter()
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val pictureName = getPictureName()
            File(pictureDirectory, pictureName)
            imageUri = data.data
            imageUri = getImageUri(this, imageBitmap)
            imageContainer.setImageBitmap(imageBitmap)
        }

        if (requestCode == PICK_IMAGE_FROM_GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data?.data
            loadImage(imageUri, imageContainer)
        }
    }

    private fun getPictureName(): String? {
        val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
        val timestamp = date.format(Date())
        return "mealennium$timestamp.jpg"
    }

    private fun getImageUri(context: Context, bitmapImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmapImage, "sample", null)
        return Uri.parse(path)
    }

    private fun loadImage(imageUri: Uri?, imageContainer: ImageView) {
        imageLoader.loadImage(imageUri.toString(), object: OnImageLoadedListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                imageContainer.setImageBitmap(loadedImage)
            }
        })
    }

    override fun onBackPressed() {
        PopupManager.showDiscardDataMessage(this) {
            finish()
        }
    }
}