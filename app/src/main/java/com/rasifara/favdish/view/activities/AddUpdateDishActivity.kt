package com.rasifara.favdish.view.activities

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.rasifara.favdish.R
import com.rasifara.favdish.application.FavDishApplication
import com.rasifara.favdish.databinding.ActivityAddUpdateDishBinding
import com.rasifara.favdish.databinding.DialogCustomImageSelectionBinding
import com.rasifara.favdish.databinding.DialogCustomListBinding
import com.rasifara.favdish.model.entities.FavDish
import com.rasifara.favdish.utils.Constants
import com.rasifara.favdish.view.adapters.CustomListAdapter
import com.rasifara.favdish.viewmodel.FavDishViewModel
import com.rasifara.favdish.viewmodel.FavDishViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddUpdateDishBinding
    private var mImagePath: String = ""
    private lateinit var mCustomListDialog: Dialog

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActionBar()
        binding.ivAddDishImage.setOnClickListener(this)

        binding.etType.setOnClickListener(this)
        binding.etCategory.setOnClickListener(this)
        binding.etCookingTime.setOnClickListener(this)
        binding.btnAddDish.setOnClickListener(this)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddDishActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_add_dish_image -> {
                    customImageSelectionDialog()
                    return
                }

                R.id.et_type -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                    return
                }

                R.id.et_category -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                    return
                }

                R.id.et_cooking_time -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(),
                        Constants.DISH_COOKING_TIME
                    )
                    return
                }

                R.id.btn_add_dish -> {
                    val title = binding.etTitle.text.toString().trim { it <= ' ' }
                    val type = binding.etType.text.toString().trim { it <= ' ' }
                    val category = binding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients = binding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        binding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        binding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {

                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            val favDishDetails: FavDish = FavDish(
                                mImagePath,
                                Constants.DISH_IMAGE_SOURCE_LOCAL,
                                title,
                                type,
                                category,
                                ingredients,
                                cookingTimeInMinutes,
                                cookingDirection,
                                false
                            )
                            mFavDishViewModel.insert(favDishDetails)
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                "resources.getString(R.string.msg_dish_added_successfully)",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.i("insert", "$favDishDetails success")
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val bindingDialog: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.tvCamera.setOnClickListener {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA_CODE)
                        } else {
                            showRationaleDialogForPermission()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationaleDialogForPermission()
                }

            }).onSameThread().check()
            dialog.dismiss()
        }

        bindingDialog.tvGallery.setOnClickListener {
            Dexter.withContext(this@AddUpdateDishActivity)
                .withPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(object : PermissionListener {
                    override fun onPermissionGranted(report: PermissionGrantedResponse?) {
                        val intentGallery =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intentGallery, GALLERY_CODE)
                    }

                    override fun onPermissionDenied(report: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@AddUpdateDishActivity,
                            "You don't have gallery permission",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        showRationaleDialogForPermission()
                    }

                }).onSameThread().check()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showRationaleDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permission required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                data?.extras?.let {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap

                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "Error loading image", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("Image Path", mImagePath)
                                }
                                return false
                            }

                        })
                        .into(binding.ivDishImage)

                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("Image Path", mImagePath)

                    binding.ivDishImage.setImageBitmap(thumbnail)
                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_edit
                        )
                    )
                }
            }
            if (requestCode == GALLERY_CODE) {
                data?.let {
                    val selectedImageUri = data.data
                    Glide.with(this@AddUpdateDishActivity)
                        .load(selectedImageUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                // log exception
                                Log.e("TAG", "Error loading image", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                val bitmap: Bitmap = resource.toBitmap()
                                mImagePath = saveImageToInternalStorage(bitmap)
                                Log.i("Image Path", mImagePath)
                                return false
                            }

                        }).into(binding.ivDishImage)

                    binding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_edit
                        )
                    )
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()

        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(this)

        val adapter = CustomListAdapter(this, itemsList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()

    }

    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                binding.etType.setText(item)
            }

            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                binding.etCategory.setText(item)
            }

            Constants.DISH_COOKING_TIME -> {
                mCustomListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
    }


    companion object {
        private const val CAMERA_CODE = 1
        private const val GALLERY_CODE = 2
        private const val IMAGE_DIRECTORY = "FavDishImages"

    }
}