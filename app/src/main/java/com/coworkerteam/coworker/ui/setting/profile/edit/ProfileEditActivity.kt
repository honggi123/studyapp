package com.coworkerteam.coworker.ui.setting.profile.edit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.databinding.ActivityProfileEditBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.utils.PatternUtils
import com.google.android.material.textfield.TextInputLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding, ProfileEditViewModel>() {

    val TAG = "ProfileEditActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_profile_edit
    override val viewModel: ProfileEditViewModel by viewModel()

    lateinit var profileManageResponse: ProfileManageResponse.Result.Profile
    var categorys = ArrayList<String>()

    var realpath: String? = null
    var fileName: String? = null
    var nickname_check = ""
    var is_edit = false

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var uri = result.data!!.data
                var haveimg = true
                profileManageResponse.img = uri.toString()
                is_edit = true

                if(uri != null){
                    realpath = getPathFromUri(uri)
                    Log.d(TAG, realpath.toString())
                }else{
                    Toast.makeText(this,"????????? ???????????? ???????????????. ?????? ?????? ????????????.", Toast.LENGTH_SHORT).show()
                    haveimg = false
                }

                if(haveimg == true){
                    if (realpath != null) {
                        Glide.with(this).load(uri)
                            .into(findViewById(R.id.my_profile_edit_img))
                        Log.d(TAG, realpath.toString())
                        fileName = getFileName(realpath!!)
                    }else{
                        Toast.makeText(this,"????????? ???????????? ???????????????. ?????? ?????? ????????????.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun initStartView() {
        setSupportActionBar(viewDataBinding.myProfileEditToolbar) // ????????? ??????????????? ????????? ??????
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // ???????????? ?????? ??? ?????? ?????????
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // ????????? ????????? ??????
        supportActionBar?.title = "????????? ??????"

        profileManageResponse =
            intent.getSerializableExtra("profile") as ProfileManageResponse.Result.Profile
        setLoginImage(viewDataBinding.myProfileLoginImg,profileManageResponse.loginType)

        viewDataBinding.activitiy = this

        init()
    }

    override fun initDataBinding() {
        viewModel.ProfileEditResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    finish()
                }
                it.code() == 400 -> {
                    //API ???????????? ????????? ??? ???????????? ?????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ????????? ????????? ????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this,"????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 -> {
                    //?????? ???????????? ???????????? ?????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403?????? ????????? ????????? ????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this,"?????? ???????????? ???????????? ???????????? ????????? ??? ????????????. ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

        viewModel.NicknameCheckResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    is_edit = true

                    //????????? ??? ?????? ????????? ????????? EditText??? Error?????? ?????????????????? ??????
                    viewDataBinding.myProfileEditNickname.error = null
                    viewDataBinding.myProfileEditNickname.isErrorEnabled = false

                    //????????? ?????? ??????????????? ?????? ??????
                    viewDataBinding.myProfileEditNickname.helperText = it.body()!!.message

                    //????????? ?????? ??????????????? ?????? ??????????????? ??? ??????
                    nickname_check = it.body()!!.isUse

                }
                it.code() == 400 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -1 -> {
                            //API ???????????? ????????? ??? ???????????? ?????? ??????
                            Toast.makeText(this,"????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                        }
                        -10 -> {
                            //???????????? ???????????? ????????? ??? ?????? ??????
                            viewDataBinding.myProfileEditNickname.error = errorMessage.getString("message")
                            nickname_check = errorMessage.getString("isUse")
                        }
                    }

                }
                it.code() == 403 -> {
                    //?????? ???????????? ???????????? ?????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403?????? ????????? ????????? ????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this,"?????? ???????????? ???????????? ???????????? ????????? ??? ????????????. ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun init() {
        
        //??? ?????? ???????????? ?????????, ????????? ?????? ??? ??????
        Glide.with(this)
            .load(profileManageResponse.img)
            .into(viewDataBinding.myProfileEditImg)

        viewDataBinding.myProfileEditNickname.editText?.setText(profileManageResponse.nickname)
        viewDataBinding.myProfileEmail.text = profileManageResponse.email
        var study = profileManageResponse

        
        //?????? ?????? ?????????????????? ???????????? UI ?????? ???, ???????????? ArrayList??? ??? ??????
        if (study.category.contains("????????????")) {
            viewDataBinding.myProfileEditTxtTest.isSelected = true
            categorys.add("????????????")
        }

        if (study.category.contains("??????")) {
            viewDataBinding.myProfileEditTxtSat.isSelected = true
            categorys.add("??????")
        }

        if (study.category.contains("??????")) {
            viewDataBinding.myProfileEditTxtEmp.isSelected = true
            categorys.add("??????")
        }

        if (study.category.contains("??????")) {
            viewDataBinding.myProfileEditTxtLaug.isSelected = true
            categorys.add("??????")
        }

        if (study.category.contains("?????????")) {
            viewDataBinding.myProfileEditTxtCre.isSelected = true
            categorys.add("?????????")
        }

        if (study.category.contains("??????/??????")) {
            viewDataBinding.myProfileEditTxtOff.isSelected = true
            categorys.add("??????/??????")
        }

        if (study.category.contains("??????")) {
            viewDataBinding.myProfileEditTxtTran.isSelected = true
            categorys.add("??????")
        }

        if (study.category.contains("????????????")) {
            viewDataBinding.myProfileEditTxtSelf.isSelected = true
            categorys.add("????????????")
        }

        if (study.category.contains("??????")) {
            viewDataBinding.myProfileEditTxtOther.isSelected = true
            categorys.add("??????")
        }

    }

    fun changTextNickname(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheNickName(s.toString(),viewModel.getNickName())
        Log.d(TAG,s.toString())

        if (result.isNotError) {
            viewDataBinding.myProfileEditNickname.isErrorEnabled = false
            viewDataBinding.myProfileEditNickname.error = null
            viewDataBinding.myProfileEditBtnNicknameCheck.isEnabled = true
        } else {
            viewDataBinding.myProfileEditNickname.error = result.ErrorMessge
            viewDataBinding.myProfileEditBtnNicknameCheck.isEnabled = false
        }
    }

    fun setLoginImage(v: View, loginType: String) {
        val imageView = v as CircleImageView

        if (loginType.equals("google")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.google_icon)
        } else if (loginType.equals("kakao")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.kakao_icon)
        } else if (loginType.equals("naver")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.naver_icon)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.profile_edit_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_modify_ok -> {
                when {
                    is_edit -> {
                        when (nickname_check) {
                            "true", "" -> {
                                firebaseLog.addLog(TAG,"edit_profile")
                                if(fileName!=null ) {
                                    var file = File(realpath)
                                    if(file != null){
                                        uploadWithTransferUtilty(fileName!!, file, this)
                                    }else{
                                        Toast.makeText(this,"????????? ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                                    }
                                }else{
                                    viewModel.setProfileEditData(viewDataBinding.myProfileEditNickname.editText?.text.toString(),categorys.joinToString("|"),profileManageResponse.img)
                                }
                            }
                            else -> {
                                Toast.makeText(this, "????????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun clickCategoryButton(v: View) {
        //???????????? ?????? ?????? ?????????
        firebaseLog.addLog(TAG,"select_category")

        val view = v as TextView
        //???????????? ??????
        val categoryName = view.text.toString()

        if (view.isSelected) {
            //?????????????????? ?????????????????? ????????????
            view.setSelected(false)
            categorys.remove(categoryName)

            //??????????????? ?????????????????? ????????? ???????????? ??? ??????
            is_edit = true

        } else {
            //???????????? ????????? ?????????????????? ????????????
            if (categorys.size >= 3) {
                //??????????????? 3??? ?????? ?????? ???????????? ?????????
                Toast.makeText(this, "??????????????? ?????? 3????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
            } else {
                //??????????????? 3??? ????????????
                view.setSelected(true)
                categorys.add(categoryName)

                //??????????????? ?????????????????? ????????? ???????????? ??? ??????
                is_edit = true

            }
        }
    }

    fun clickNicknameCheck(){
        //????????? ?????? ??????
        viewModel.getNicknameCheckData(viewDataBinding.myProfileEditNickname.editText?.text.toString())

        firebaseLog.addLog(TAG,"check_nickname")
    }

    fun startImagePick(){
        //????????? ?????? ????????? ???????????? ???????????? ?????????
        val permissionListener: PermissionListener = object: PermissionListener {
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
                Toast.makeText(this@ProfileEditActivity,"????????? ???????????? ????????? ????????? ????????? ??? ????????????.",Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("?????? ????????? ?????? ????????? ???????????? ???????????? ????????? ??? ????????????. ?????? ????????? [??????] > [??????] ?????? ??????????????????.")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }

    fun uploadWithTransferUtilty(fileName: String, file: File, content: Context) {
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
            "coworker-profile",
            fileName,
            file
        ) // (bucket api, file??????, file??????)


        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    // Handle a completed upload
                    if(fileName != null){
                        profileManageResponse.img = getString(R.string.s3_coworker_study_url) + fileName
                    }
                    viewModel.setProfileEditData(viewDataBinding.myProfileEditNickname.editText?.text.toString(),categorys.joinToString("|"),profileManageResponse.img)
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
}