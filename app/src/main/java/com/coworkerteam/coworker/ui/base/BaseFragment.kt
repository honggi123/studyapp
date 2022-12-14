package com.coworkerteam.coworker.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.utils.FirebaseAnalyticsUtils
import org.koin.android.ext.android.get

open abstract class BaseFragment<T : ViewDataBinding, R : BaseViewModel> : Fragment() {

    lateinit var viewDataBinding: T
    abstract val layoutResourceID: Int

    // viewModel로 쓰일 변수
    abstract val viewModel: R

    //lateinit var mActivity: BaseActivity
    lateinit var mRootView: View

    //로그를 심는 Utils 클래스
    val firebaseLog: FirebaseAnalyticsUtils = get()

    //레이아웃을 띄운 직후 호출 - 뷰나 액티비티의 속성등을 초기화
    abstract fun initStartView()

    // 두번째로 호출. 데이터 바인딩 및 rxjava 설정
    abstract fun initDataBinding()

    // 바인딩 이후 해야할 것을 구현
    abstract fun initAfterBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, layoutResourceID, container, false)
        mRootView = viewDataBinding.root
        return mRootView
    }

    override fun onStart() {
        super.onStart()

        initStartView()
        initDataBinding()
        initAfterBinding()
    }

    fun moveLogin(){
        Toast.makeText(activity,"다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()

        val loginIntent = Intent(activity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
    }
}