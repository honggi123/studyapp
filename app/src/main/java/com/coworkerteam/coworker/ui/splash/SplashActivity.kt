package com.coworkerteam.coworker.ui.splash

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.widget.Toast

import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivitySplashBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryActivity
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import com.github.ybq.android.spinkit.style.Wave
import com.google.android.play.core.appupdate.AppUpdateManager

import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.logging.Logger

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    private val TAG = "SplashActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashViewModel by viewModel()

    private val DEFAULT_PATH = "studyday://main";
    var studyinfo : String? = null

    lateinit var appUpdateManager : AppUpdateManager

    override fun initStartView() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager?.let {
            it.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    val mDialogView =
                        LayoutInflater.from(this).inflate(R.layout.dialog_updatecheck, null)
                    val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
                    val builder = mBuilder.show()

                    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_btn_ok)
                    builder.setCancelable(false)

                    btn_ok.setOnClickListener(View.OnClickListener {
                        val ownGooglePlayLink = "market://details?id=com.coworkerteam.coworker"
                        val ownWebLink =
                            "https://play.google.com/store/apps/details?id=com.coworkerteam.coworker"
                        finishAffinity()
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ownGooglePlayLink)))
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ownWebLink)))
                        }
                        //moveTaskToBack(true); // ???????????? ?????????????????? ??????
                       // finishAndRemoveTask(); // ???????????? ?????? + ????????? ??????????????? ?????????
                        System.exit(0)
                    })
                }else{
                    //??? ??????????????? ???????????? ???????????? ?????? ???????????? ????????? ?????????
                    if (!viewModel.getRefreshToken().isNullOrEmpty()) {
                        viewModel.getAutoLoginData()
                    } else {
                        //????????? ????????? ?????? ?????????, ????????????????????? ????????? ??????
                       var intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                       finish()
                    }
                }
            }

        }

        var intent : Intent = getIntent();
        Log.d(TAG,"data : " + intent.getDataString())
        if (intent.getDataString()?.startsWith(DEFAULT_PATH+"?studylink=") == true) {
            var param : String = intent.getDataString()!!.replace(DEFAULT_PATH, "");
            studyinfo = param
        }

        //?????????????????? ?????? ????????? ??????
        viewDataBinding.spinKit.setIndeterminateDrawable(Wave())
    }



    override fun initDataBinding() {
        viewModel.AutoLoginResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //??????????????? ??????????????? ???????????? ??????
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG,"auto_login")

                    //??????????????? ????????????????????? ?????? ??????
                    if (it.body()!!.result.isInterest) {
                        //???????????? ??????
                        moveMain()
                    } else {
                        //???????????? ???????????? ??????
                        moveCategory()
                    }
                }
                else -> {
                    //400?????? ?????? -> ?????????????????? ????????? ????????? ??????(??????)
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ?????? ??? ????????? ???????????? ??????
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    override fun initAfterBinding() {
       // inAppUpdate = InAppUpdate(this)
        Log.d(TAG,"initAfterBinding")

    }

    //?????????????????? ???????????? ?????????
    private fun moveMain() {
        var intent = Intent(this, MainActivity::class.java)
        if (studyinfo!=null){
            intent.putExtra("studyinfo",studyinfo)
        }
        startActivity(intent)
        finish()
    }

    //??????????????? ???????????? ?????????
    private fun moveCategory() {
        var intent = Intent(this, CategoryActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
    }



}


