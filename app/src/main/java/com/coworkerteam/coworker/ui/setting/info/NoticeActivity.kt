package com.coworkerteam.coworker.ui.setting.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import com.coworkerteam.coworker.R

//서비스 공지사항에 대한 웹뷰 액티비티
class NoticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        //툴바 세팅
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolber)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "공지사항"

        //웹뷰
        val webView = findViewById<WebView>(R.id.notice_webview)

        webView.settings.apply {
            javaScriptEnabled = true
        }

        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://www.studyday.co.kr/notice")
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when (item!!.itemId) {
            android.R.id.home -> {
                //뒤로가기 버튼 클릭시 액티비티 닫기
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}