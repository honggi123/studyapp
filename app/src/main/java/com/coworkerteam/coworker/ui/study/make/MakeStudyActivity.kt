package com.coworkerteam.coworker.ui.study.make

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityMakeStudyBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.unity.UnityActivity
import com.coworkerteam.coworker.utils.PatternUtils
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class MakeStudyActivity : BaseActivity<ActivityMakeStudyBinding, MakeStudyViewModel>() {

    private val TAG = "MakeStudyActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_make_study
    override val viewModel: MakeStudyViewModel by viewModel()

    lateinit var mDialogView: View

    var realpath: String? = null
    var fileName: String? = null

    var imageUrl: String? = null
    var studyType: String? = null
    var isStudyName = false
    var isPassword = false
    var isStudyNum = false
    var categorys = ArrayList<String>()
    var isIntroduce = false
    var showStudyDescription = false
    var builder: AlertDialog? = null

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var uri = result.data!!.data
                imageUrl = uri.toString()
                if (uri != null) {
                    realpath = getPathFromUri(uri)
                    Log.d(TAG, realpath.toString())

                    fileName = getFileName(realpath!!)
                }
                Glide.with(this).load(uri)
                    .into(mDialogView.findViewById(R.id.dialog_select_image_selet_image))
            }
        }

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.make_study_toolbar)

        setSupportActionBar(main_toolbar) // ????????? ??????????????? ????????? ??????
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // ???????????? ?????? ??? ?????? ?????????
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // ????????? ????????? ??????
        supportActionBar?.title = "????????? ?????????"

        viewDataBinding.activity = this
    }

    override fun initDataBinding() {
        viewModel.MakeStudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //????????? ????????? ??????
            when {
                it.isSuccessful -> {
                    //????????? ?????? ???????????? ??????
                    viewModel.getEnterCamstduyData(it.body()!!.result.studyIdx,it.body()!!.result.pw)
                }
                it.code() == 400 -> {
                    //???????????? ????????? ??? ???????????? ?????? ?????? ex. ?????? ?????? ???????????? ?????? ??????????????? ?????????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    loding.dismissDialog()

                    //400?????? ????????? ????????? ????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this,errorMessage.getString("message"),Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    loding.dismissDialog()

                    moveLogin()
                }
            }
        })

        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            loding.dismissDialog()
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG,"make_study")

                    /*
                    var intent = Intent(this, EnterCamstudyActivity::class.java)
                    intent.putExtra("studyInfo", it.body()!!)

                    startActivity(intent)
                    finish()
                    */
                                       var intent = Intent(this, UnityActivity::class.java)
                                       intent.putExtra("studyInfo", it.body()!!)
                                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                       intent.setAction(Intent.ACTION_MAIN);
                                       intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                       Log.d(TAG,"studyInfo : "+it.body().toString())
                                       startActivity(intent)
                                       finish()
                }
                it.code() == 400 -> {
                    //???????????? ????????? ??? ???????????? ?????? ?????? ex. ?????? ?????? ???????????? ?????? ??????????????? ?????????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ????????? ????????? ??????????????? ?????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this,"???????????? ?????? ????????? ??? ????????????. ?????? ?????? ??????????????????.",Toast.LENGTH_SHORT).show()

                    finish()
                }
                it.code() == 403 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -4 ->{
                            //?????? ???????????? ?????? ?????? ?????? ??? ?????? ????????? ??? ?????? ??????
                            Toast.makeText(this,"?????? ???????????? ??????????????????. ????????? ??? ????????????.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        -5 ->{
                            //???????????? ???????????? ?????? ??????
                            Toast.makeText(this,"?????? ???????????? ???????????? ????????????. ?????? ????????? ??? ????????????.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        -12 ->{
                            //??????????????? ?????? ??????
                            finish()
                        }
                    }
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -2 ->{
                            //???????????? ?????? ????????? ??????
                            moveLogin()
                        }
                        -3 ->{
                            //???????????? ?????? ???????????? ??????
                            Toast.makeText(this,"????????? ???????????? ?????? ??????????????????.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun checkStudyTypeRadio() {
        val checkedId = viewDataBinding.makeStudyType.checkedRadioButtonId

        if (checkedId == R.id.make_study_radio_open) {
            studyType = "open"
            Log.d(TAG, "open")
        } else if (checkedId == R.id.make_study_radio_group) {
            studyType = "group"
            Log.d(TAG, "group")
        } else {
            studyType = null
        }
    }

    fun clickCategoryButton(v: View) {
        val view = v as TextView

        //???????????? ??????
        val categoryName = view.text.toString()

        firebaseLog.addLog(TAG,"select_category")

        if (view.isSelected) {
            //?????????????????? ?????????????????? ????????????
            view.setSelected(false)
            categorys.remove(categoryName)
        } else {
            //???????????? ????????? ?????????????????? ????????????
            if (categorys.size >= 3) {
                //??????????????? 3??? ?????? ?????? ???????????? ?????????
                Toast.makeText(this, "??????????????? ?????? 3????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
            } else {
                //??????????????? 3??? ????????????
                view.setSelected(true)
                categorys.add(categoryName)
            }
        }
    }

    fun onCheckedChangedPassword(checked: Boolean) {
        if(checked){
            viewDataBinding.makeStudyEdtPw.visibility = View.VISIBLE
        }else{
            viewDataBinding.makeStudyEdtPw.visibility = View.GONE
        }
    }

    fun changTextStudyName(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheStudyName(s.toString())

        if (result.isNotError){
            isStudyName = true
            viewDataBinding.makeStudyEdtName.isErrorEnabled = false
            viewDataBinding.makeStudyEdtName.error = null
        } else {
            isStudyName = false
            viewDataBinding.makeStudyEdtName.error = result.ErrorMessge
        }
    }

    fun changTextStudyPassword(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheStudyPassword(s.toString())

        if (result.isNotError) {
            isPassword = true
            viewDataBinding.makeStudyEdtPw.isErrorEnabled = false
            viewDataBinding.makeStudyEdtPw.error = null
        } else {
            isPassword = false
            viewDataBinding.makeStudyEdtPw.error = result.ErrorMessge
        }
    }

    fun changTextStudyNum(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheStudyNum(s.toString())

        if (result.isNotError) {
            isStudyNum = true
            viewDataBinding.makeStudyEdtNum.isErrorEnabled = false
            viewDataBinding.makeStudyEdtNum.error = null
        } else {
            isStudyNum = false
            viewDataBinding.makeStudyEdtNum.error = result.ErrorMessge
        }
    }

    fun changTextIntroduce(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheDescript(s.toString())

        if (result.isNotError) {
            isIntroduce = true
            viewDataBinding.makeStudyEdtIntroduce.isErrorEnabled = false
            viewDataBinding.makeStudyEdtIntroduce.error = null
        } else {
            isIntroduce = false
            viewDataBinding.makeStudyEdtIntroduce.error = result.ErrorMessge
        }
    }

    fun showImageDialog() {
        val view = viewDataBinding.makeStudyImg
        val baseImages: List<String> = listOf(
            "https://coworker-study.s3.ap-northeast-2.amazonaws.com/basicImage1.jpg",
            "https://coworker-study.s3.ap-northeast-2.amazonaws.com/basicImage2.jpg",
            "https://coworker-study.s3.ap-northeast-2.amazonaws.com/basicImage3.jpg"
        )

        mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_image, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        val builder = mBuilder.show()
        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val finish = mDialogView.findViewById<ImageView>(R.id.dialog_select_image_finish)
        val btn_import =
            mDialogView.findViewById<Button>(R.id.dialog_select_image_btn_image_Import)
        val btn_pick = mDialogView.findViewById<Button>(R.id.dialog_select_image_btn_pick_ok)

        val selectImage =
            mDialogView.findViewById<ImageView>(R.id.dialog_select_image_selet_image)

        val baseImage1 = mDialogView.findViewById<ImageView>(R.id.dialog_select_image_basic_one)
        val baseImage2 = mDialogView.findViewById<ImageView>(R.id.dialog_select_image_basic_two)
        val baseImage3 =
            mDialogView.findViewById<ImageView>(R.id.dialog_select_image_basic_three)

        Glide.with(this).load(baseImages[0]).into(baseImage1)
        Glide.with(this).load(baseImages[1]).into(baseImage2)
        Glide.with(this).load(baseImages[2]).into(baseImage3)

        baseImage1.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(baseImages[0]).into(selectImage)
            imageUrl = baseImages[0]

            firebaseLog.addLog(TAG,"select_base_image")
        })

        baseImage2.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(baseImages[1]).into(selectImage)
            imageUrl = baseImages[1]

            firebaseLog.addLog(TAG,"select_base_image")
        })

        baseImage3.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(baseImages[2]).into(selectImage)
            imageUrl = baseImages[2]

            firebaseLog.addLog(TAG,"select_base_image")
        })

        btn_import.setOnClickListener(View.OnClickListener {
            val permissionListener:PermissionListener = object:PermissionListener{
                override fun onPermissionGranted() {
                    //?????? ????????? ????????? ??????
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = MediaStore.Images.Media.CONTENT_TYPE
                    intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    startForResult.launch(intent)

                    firebaseLog.addLog(TAG,"select_image")
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    // ?????? ????????? ??????  ??? ??????
                    Toast.makeText(this@MakeStudyActivity,"????????? ???????????? ????????? ????????? ????????? ??? ????????????.",Toast.LENGTH_SHORT).show()
                }
            }
            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("?????? ????????? ?????? ????????? ???????????? ???????????? ????????? ??? ????????????. ?????? ????????? [??????] > [??????] ?????? ??????????????????.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        })

        btn_pick.setOnClickListener(View.OnClickListener {
            Glide.with(this).load(imageUrl).into(view)
            builder.dismiss()
        })

        finish.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
    }

    fun makeStudy() {
        if (imageUrl == null) {
            Toast.makeText(this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else if (studyType == null) {
            Toast.makeText(this, "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else if (!isStudyName) {
            Toast.makeText(this, "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else if (viewDataBinding.makeStudyCheckPw.isChecked && !isPassword) {
            Toast.makeText(this, "????????? ??????????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else if (!isStudyNum) {
            Toast.makeText(this, "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else if (categorys.size == 0) {
            Toast.makeText(this, "????????? ??????????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else if (!isIntroduce) {
            Toast.makeText(this, "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        } else {
            var haveimg = true
            loding.showDialog(this)

            if (fileName != null ) {
                var file = File(realpath)
                if(file != null){
                    uploadWithTransferUtilty(fileName!!,file)
                    imageUrl = getString(R.string.s3_coworker_study_url) + fileName
                }else{
                    Toast.makeText(this, "????????? ???????????? ???????????? ???????????????. ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                    haveimg = false
                }
            }
            if(haveimg){
                var categorys = categorys.joinToString("|")
                var password =
                    if (viewDataBinding.makeStudyCheckPw.isChecked) viewDataBinding.makeStudyEdtPw.editText?.text.toString() else null

                viewModel.setMakeStudyData(
                    studyType!!,
                    viewDataBinding.makeStudyEdtName.editText?.text.toString(),
                    categorys,
                    imageUrl!!,
                    password,
                    viewDataBinding.makeStudyEdtNum.editText?.text.toString().toInt(),
                    viewDataBinding.makeStudyEdtIntroduce.editText?.text.toString()
                )
            }
        }
    }

    fun uploadWithTransferUtilty(fileName: String, file: File) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials(
                getString(R.string.s3_accesskey_id),
                getString(R.string.s3_accesskey_secret)
            ) // IAM ???????????? ?????? ??? ??????

        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))

        val transferUtility = TransferUtility.builder().s3Client(s3Client)
            .context(getApplicationContext()).build()
        TransferNetworkLossHandler.getInstance(getApplicationContext())

        val uploadObserver = transferUtility.upload(
            "coworker-study",
            fileName,
            file
        ) // (bucket api, file??????, file??????)


        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    // Handle a completed upload
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
                Log.d("MYTAG", "UPLOAD - - ID: \$id, percent done = \$done")
            }

            override fun onError(id: Int, ex: Exception) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: \$id - - EX:$ex")
            }
        })
    }

    // ????????? uri??? ?????? ????????? ????????? ????????? gps return
    // ???????????? ??????
    open fun getPathFromUri(uri: Uri?): String? {
        val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToNext()
        val path: String = cursor.getString(cursor.getColumnIndex("_data"))
        cursor.close()
        return path
    }

    fun getFileName(path: String): String {
        val timestamp = System.currentTimeMillis().toString()

        var extension = path.lastIndexOf(".")

        var result = timestamp + path.substring(extension, path.length)

        return result
    }


    fun showStudyDescription(){
        // ????????? ?????? ???????????????
        if(showStudyDescription == false){
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.dialog_studydescription, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            builder = mBuilder?.create()
            builder?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            builder?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            val btn_cancle =
                mDialogView.findViewById<ImageView>(R.id.dialog_studydescripion_btncancle)

            // ??????????????? ??????
            btn_cancle.setOnClickListener(View.OnClickListener {
                builder?.dismiss()
                showStudyDescription = false
            })
        }
        showStudyDescription = false

        if(showStudyDescription == true){
            builder?.dismiss()
        }else{
            builder?.show()
            showStudyDescription = true
        }
    }

}