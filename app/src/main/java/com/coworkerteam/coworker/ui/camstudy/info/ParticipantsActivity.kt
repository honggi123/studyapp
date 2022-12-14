package com.coworkerteam.coworker.ui.camstudy.info

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.ParticipantsResponse
import com.coworkerteam.coworker.databinding.ActivityParticipantsBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class ParticipantsActivity : BaseActivity<ActivityParticipantsBinding, ParticipantsViewModel>() {

    private val TAG = "ParticipantsActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_participants
    override val viewModel: ParticipantsViewModel by viewModel()

    private var mServiceCallback: Messenger? = null
    private var mClientCallback = Messenger(CallbackHandler(Looper.getMainLooper()))

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.camstudy_layout_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24_black) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        init()
        var intent = Intent(this, CamStudyService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
    }

    override fun onDestroy() {
        super.onDestroy()
        //binding 해제
        unbindService(mConnection)

        //서비스랑 메신저 연결 해제
        val connectMsg = Message.obtain(null, CamStudyService.MSG_CLIENT_DISCNNECT)
        connectMsg.replyTo = mClientCallback

        try {
            mServiceCallback!!.send(connectMsg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun init() {
        val txt_toobarTitle = findViewById<TextView>(R.id.camstudy_info_toolbar_title)
        val txt_peple = findViewById<TextView>(R.id.camstudy_info_toolbar_peple)

        txt_toobarTitle.text = "참여자 목록"

        //텍스트뷰 일부분 색상바꾸기
        val text = txt_peple.text.toString()
        txt_peple.text = setTextColor(text)
    }

    private fun setTextColor(text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        val colorBlueSpan = ForegroundColorSpan(Color.BLUE)
        builder.setSpan(colorBlueSpan, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    fun setting(it: ParticipantsResponse) {
        var newAdapter = ParticipantsAdapter(this, mServiceCallback,viewModel)
        newAdapter.datas = it.participants.toMutableList()
        newAdapter.isLeader = (it.participants[0].nickname == viewModel.getUserNickName()) && it.participants[0].isLeader
        viewDataBinding.participantsRv.adapter = newAdapter

        val txt_peple = findViewById<TextView>(R.id.camstudy_info_toolbar_peple)
        val peple = ""+it.participants.size + " / " + it.maxNum
        txt_peple.text = setTextColor(peple)
    }

    private var mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mServiceCallback = Messenger(service)

            setting(CamStudyService.participantsResponses!!)

            //서비스랑 연결
            val connectMsg = Message.obtain(null, CamStudyService.MSG_CLIENT_CONNECT)
            connectMsg.replyTo = mClientCallback

            try {
                mServiceCallback!!.send(connectMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceCallback = null
        }
    }

    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                CamStudyService.MSG_PARTICIPANTS_ITEM -> {
                    //캠스터디 아이템
                    setting(CamStudyService.participantsResponses!!)
                }
            }
        }
    }
}