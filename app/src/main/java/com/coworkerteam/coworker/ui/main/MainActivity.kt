package com.coworkerteam.coworker.ui.main

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.data.model.other.IsGoalSucess
import com.coworkerteam.coworker.databinding.ActivityMainBinding
import com.coworkerteam.coworker.ui.base.NavigationActivity
import com.coworkerteam.coworker.ui.dialog.PasswordDialog
import com.coworkerteam.coworker.ui.dialog.SuccessPostDialog
import com.coworkerteam.coworker.ui.study.make.MakeStudyActivity
import com.coworkerteam.coworker.ui.todolist.TodoListActivity
import com.coworkerteam.coworker.unity.UnityActivity
import com.coworkerteam.coworker.utils.PatternUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.gson.Gson
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat

class MainActivity : NavigationActivity<ActivityMainBinding, MainViewModel>()
     {
    val TAG = "MainActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_main
    override val viewModel: MainViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    var NewStudyShowOpen: Boolean = true
    var RecommendStudyShowOpen: Boolean = true
    var setData: Boolean = false
    var showStudyDescription = false
    lateinit var appUpdateManager : AppUpdateManager

    lateinit var pagingMainMyStudyAdapter: MainMyStudyPagingAdapter

    val passwordDialog = PasswordDialog()
    var builder: AlertDialog? = null

    var notShowSucessPost = true

    private val REQ_CODE_VERSION_UPDATE = 500

     var gson = Gson()
      var goalIsSuccess : IsGoalSucess? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var con = this
    }


    override fun initStartView() {
        super.initStartView()

        loding.showDialog(this)

        viewDataBinding.activitiy = this

        //?????? ??????
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolber)

        setSupportActionBar(main_toolbar) // ????????? ??????????????? ????????? ??????
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // ???????????? ?????? ??? ?????? ?????????
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // ????????? ????????? ??????
        supportActionBar?.title = getString(R.string.app_name)

        //????????? + ???????????? ?????? ??????
        val mainToolbarMakeStudy = findViewById<ImageView>(R.id.main_toolbar_makeStudy)

        mainToolbarMakeStudy.visibility = View.VISIBLE
        mainToolbarMakeStudy.setOnClickListener(
            View.OnClickListener {
                var intent = Intent(this, MakeStudyActivity::class.java)
                startActivity(intent)
            }
        )

        //???????????? ????????? Adapter ??????
        pagingMainMyStudyAdapter = MainMyStudyPagingAdapter(viewModel)
        viewDataBinding.mainMystudyRecylerView.adapter = pagingMainMyStudyAdapter

        pagingMainMyStudyAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && pagingMainMyStudyAdapter.itemCount < 1) {
                viewDataBinding.textView37.visibility = View.VISIBLE
            } else {
                viewDataBinding.textView37.visibility = View.GONE
            }
        }

        //???????????? ??????????????? ok?????? ?????? ??????
        passwordDialog.onClickOKButton = { i: Int, s: String? ->
            viewModel.getEnterCamstduyData(i, s)
            firebaseLog.addLog(TAG, "check_study_password")
        }

        //?????? ??? ?????? ??????????????? ???????????????
        var kick = intent.getBooleanExtra("KickFromLeader",false)
        if(kick){
            MaterialAlertDialogBuilder(this@MainActivity)
                .setMessage("??????????????? ?????????????????????.")
                .setPositiveButton("??????", DialogInterface.OnClickListener { dialog, which ->
                }).show()
        }


        // ?????? ?????? ?????? ?????? ??? successpost ????????????
        if(viewModel.getIsGoalSucess()!= null) {
            goalIsSuccess = gson.fromJson(viewModel.getIsGoalSucess(), IsGoalSucess::class.java)

            if (goalIsSuccess!!.goalIsSuccess) {
                goalIsSuccess!!.goalPostIsWrite?.let {
                    showSuccessPostDialog(
                        goalIsSuccess!!.goalSuccesstime,
                        it
                    )
                }
                // ?????????????????? ????????? ??? ?????? ?????? ?????? ?????? ??????????????? false ??? ???????????? ???????????? ?????????????????? ???????????? ???????????? ??????
                viewModel.setGoalIsSuccess(
                    goalIsSuccess = false,
                    goalIsSuccess!!.goalSuccesstime,
                    goalPostIsWrite = false
                )
            }
        }
        init()
    }

    override fun onResume() {
        super.onResume()
        checkdate()
        var studyinfo = intent.getStringExtra("studyinfo")

        if(studyinfo != null){
            // '/'??? ???????????? ???????????? ?????????.
            var str_arr = studyinfo.split("https://www.studyday.co.kr/link")
            Log.d(TAG,"str_arr[1]"+ str_arr[1])

            var uri = Uri.parse(str_arr[1])
            var studyidx = Uri.parse(str_arr[1]).getQueryParameter("idx")
            var pwd = Uri.parse(studyidx).getQueryParameter("pwd")
            studyidx = studyidx?.split("?pwd")?.get(0)
            pwd = pwd!!.replace(" ", "")

            if(!pwd.equals("null")){
                //??????, ????????? ?????? ????????? ??????????????? ???????????? ???????????? ???????????? ?????? ???????????? ?????? dialog??? ??????
                studyidx?.let { passwordDialog.showDialog(this, studyidx.toInt()) }
            }else {
                if (studyidx != null) {
                    passwordDialog.onClickOKButton(studyidx.toInt(), null)
                }
            }
            intent.removeExtra("studyinfo")
        }
    }


    override fun initDataBinding() {
        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    /*
                    // (???????????? ?????? ???) ???????????? ?????? ???????????? ?????? ??????
                    var intent = Intent(this, EnterCamstudyActivity::class.java)
                    intent.putExtra("studyInfo", it.body()!!)
                    Log.d(TAG,"STUDYINFO"+it.body())
                    passwordDialog.dismissDialog()
                    startActivity(intent)
                    */

                    passwordDialog.dismissDialog()
                    var intent = Intent(this, UnityActivity::class.java)
                   intent.putExtra("studyInfo", it.body()!!)
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_MAIN);
                   intent.addCategory(Intent.CATEGORY_LAUNCHER);
                   Log.d(TAG,"studyInfo : "+it.body().toString())
                    startActivity(intent)

                }

                it.code() == 400 -> {
                    //???????????? ????????? ??? ???????????? ?????? ?????? ex. ?????? ?????? ???????????? ?????? ??????????????? ?????????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ????????? ????????? ??????????????? ?????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this, "???????????? ????????? ??? ????????????. ?????? ?????? ??????????????????.", Toast.LENGTH_SHORT)
                        .show()
                }

                it.code() == 403 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -4 -> {
                            //?????? ???????????? ?????? ?????? ?????? ??? ?????? ????????? ??? ?????? ??????
                            Toast.makeText(this, "?????? ???????????? ??????????????????. ????????? ??? ????????????.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        -5 -> {
                            //???????????? ???????????? ?????? ??????
                            Toast.makeText(
                                this,
                                "?????? ???????????? ???????????? ????????????. ?????? ????????? ??? ????????????.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        -12 -> {
                            //??????????????? ?????? ??????
                            passwordDialog.setErrorMessage(errorMessage.getString("message"))
                        }
                    }
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //???????????? ?????? ????????? ??????
                            moveLogin()
                        }
                        -3 -> {
                            //???????????? ?????? ???????????? ??????
                            Toast.makeText(this, "????????? ???????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        })

        viewModel.MainResponseLiveData.observe(this, androidx.lifecycle.Observer {
            loding.dismissDialog()
            when {
                it.isSuccessful -> {
                    viewDataBinding.mainResponse = it.body()!!

                    //??????????????? ?????? ??????
                    setNavigaionProfileImage(it.body()!!.result[0].profile.img)
                    setNavigaionLoginImage(it.body()!!.result[0].profile.loginType)
                    setNavigaionNickname(it.body()!!.result[0].profile.nickname)

                    viewDataBinding.draworInfo = DrawerBottomInfo(
                        it.body()!!.result[0].achieveTimeRate,
                        it.body()!!.result[0].achieveTodoRate,
                        it.body()!!.result[0].dream.dday,
                        it.body()!!.result[0].dream.ddayName
                    )

                    //????????????
                    // var myStudyAdepter = MainTodolistAdapter(this, viewModel)
                    // myStudyAdepter.datas = it.body()!!.result[0].todo.toMutableList()
                    // viewDataBinding.mainTodolistRecylerView.adapter = myStudyAdepter

                    setData = true
                    NewStudy_init()
                    Recommend_init()
                }
                it.code() == 400 -> {
                    //???????????? ????????? ??? ???????????? ?????? ?????? ex. ?????? ?????? ???????????? ?????? ??????????????? ?????????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ????????? ?????? ????????? ??????????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this, "?????? ???????????? ???????????? ???????????????. ?????? ?????? ??????????????????.", Toast.LENGTH_SHORT)
                        .show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))
                    moveLogin()
                }
            }
        })

        viewModel.EditGoalResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG, "edit_goal")

                    viewDataBinding.mainResponse!!.result[0].dream = it.body()!!.result.dream
                    viewDataBinding.mainResponse!!.result[0].achieveTimeRate =
                        it.body()!!.result.achieveTimeRate
                    viewDataBinding.mainResponse!!.result[0].aimTime = it.body()!!.result.aimTime
                    viewDataBinding.mainResponse = viewDataBinding.mainResponse
                }
                it.code() == 400 -> {
                    //???????????? ????????? ??? ???????????? ?????? ?????? ex. ?????? ?????? ???????????? ?????? ??????????????? ?????????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ????????? ?????? ????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this, "?????? ????????? ??????????????????. ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

        viewModel.MainRankResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    viewDataBinding.mainRankResponse = it.body()

                    val nothaverank = findViewById<ConstraintLayout>(R.id.notHaveMyRanking)

                    if(it.body()?.result?.myRank == null){
                        nothaverank.visibility = View.VISIBLE
                    }else{
                        nothaverank.visibility = View.GONE
                    }

                    firebaseLog.addLog(TAG, "edit_goal")
                    Log.d(TAG,"rank : " + it.body()?.result)
                }
                it.code() == 400 -> {

                    //400?????? ????????? ?????? ????????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this, "?????? ????????? ??????????????????. ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })


        viewModel.GoalSuccessResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                }
                it.code() == 400 -> {
                    //400?????? ????????? ????????? ????????? ????????? ???????????? ex) 20??? ??????, ?????????????????? ????????? ??? ?????? ??????. ??????????????? ??????
                    Toast.makeText(this, "?????? ????????? ??????????????????. ????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //???????????? ?????? ????????? ??????
                            moveLogin()
                        }
                        -10 -> {
                            //???????????? ?????? ???????????? ??????
                            Toast.makeText(this, "?????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                it.code() == 500 -> {
                    // ???????????? ????????? ????????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, "????????? ????????? ??????????????????. ?????? ??????????????????.")
                    moveLogin()
                }
            }
        })

        /*
        viewModel.CheckTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG, "check_todolist")

                    //??????????????? ????????? ?????? ?????? ????????? ??????
                    viewDataBinding.draworInfo!!.achieveTodoRate =
                        it.body()!!.result.achieveTodoRate
                    viewDataBinding.draworInfo = viewDataBinding.draworInfo
                }
                it.code() == 400 -> {
                    //???????????? ????????? ??? ???????????? ?????? ?????? ex. ?????? ?????? ???????????? ?????? ??????????????? ?????????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400?????? ????????? ??????????????? ?????? ???????????? ??????, ??????????????? ????????????.
                    Toast.makeText(this, "??????????????? ????????? ??????????????????. ?????? ?????? ??????????????????.", Toast.LENGTH_SHORT)
                        .show()
                }
                it.code() == 404 -> {
                    //???????????? ?????? ????????? ??????
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //???????????? ?????? ????????? ??????
                            moveLogin()
                        }
                        -7 -> {
                            //??????????????? ?????? ??? ?????? ????????? ???????????? ?????? ??????
                            Toast.makeText(this, "?????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
         */

        viewModel.MyStudyPagingData.observe(this, androidx.lifecycle.Observer {
            pagingMainMyStudyAdapter.submitData(lifecycle, it)
        })
    }

    override fun initAfterBinding() {
        viewModel.getMainData()
    }

    override fun onRestart() {
        super.onRestart()
        initAfterBinding()
        pagingMainMyStudyAdapter.refresh()
    }

    fun init() {
        //?????????
        val data = arrayOf("???????????????", "???????????????")

        val adapter = ArrayAdapter(this, R.layout.spinner_item_selected, data)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        val spinner_new = findViewById<Spinner>(R.id.main_spinner_new_study)
        val spinner_dcommend = findViewById<Spinner>(R.id.main_spinner_dcommend)
        val spinner_rankingcategory = findViewById<Spinner>(R.id.main_spinner_ranking)

        spinner_new.adapter = adapter
        spinner_new.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent.getItemAtPosition(position).toString() == "???????????????") {
                    NewStudyShowOpen = true
                    if (setData) {
                        NewStudy_init()
                    }
                } else {
                    NewStudyShowOpen = false
                    if (setData) {
                        NewStudy_init()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        spinner_dcommend.adapter = adapter
        spinner_dcommend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent.getItemAtPosition(position).toString() == "???????????????") {
                    RecommendStudyShowOpen = true
                    if (setData) {
                        Recommend_init()
                    }
                } else {
                    RecommendStudyShowOpen = false
                    if (setData) {
                        Recommend_init()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val rankingdata = arrayOf("??????", "?????? 1??????","?????? 1??????")

        val rankingadapter = ArrayAdapter(this, R.layout.spinner_item_selected, rankingdata)
        rankingadapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinner_rankingcategory.adapter = rankingadapter
        spinner_rankingcategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent.getItemAtPosition(position).toString() == "??????") {
                    viewModel.getRankData("yesterday")
                } else if(parent.getItemAtPosition(position).toString() == "?????? 1??????"){
                    viewModel.getRankData("week")
                }else{
                    viewModel.getRankData("month")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    fun showPopupMenu(v: View) {
        var popup = PopupMenu(this, v)
        var con = this
        con.menuInflater?.inflate(R.menu.main_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.goal_setting -> {
                    Log.d(TAG, "goal_setting")
                    val mDialogView =
                        LayoutInflater.from(con).inflate(R.layout.dialog_goal, null)
                    val mBuilder = AlertDialog.Builder(con).setView(mDialogView)
                    val builder = mBuilder.show()

                    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val editTime =
                        mDialogView.findViewById<TextInputLayout>(R.id.dialog_goal_edit_goal_time)
                    val editGoal =
                        mDialogView.findViewById<TextInputLayout>(R.id.dialog_goal_edit_goal)
                    val txt_delete = mDialogView.findViewById<TextView>(R.id.dialog_goal_delete)
                    var isCheckGoal = true

                    if (viewDataBinding.mainResponse!!.result[0].aimTime != null && viewDataBinding.mainResponse!!.result[0].dream.goal != null) {
                        editTime.editText?.setText(viewDataBinding.mainResponse!!.result[0].aimTime)
                        editGoal.editText?.setText(viewDataBinding.mainResponse!!.result[0].dream.goal)
                        txt_delete.visibility = View.VISIBLE
                    }

                    val changTextGoal: (CharSequence?, Int, Int, Int) -> Unit =
                        { charSequence: CharSequence?, i: Int, i1: Int, i2: Int ->
                            val result = PatternUtils.matcheGoal(charSequence.toString())
                            Log.d(TAG, charSequence.toString())

                            if (result.isNotError) {
                                editGoal.isErrorEnabled = false
                                editGoal.error = null
                                isCheckGoal = true
                            } else {
                                editGoal.error = result.ErrorMessge
                                isCheckGoal = false
                            }
                        }

                    editGoal.editText?.addTextChangedListener(onTextChanged = changTextGoal)

                    editTime.editText?.setOnClickListener(View.OnClickListener {
                        MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(0)
                            .setMinute(0)
                            .build()
                            .apply {
                                addOnPositiveButtonClickListener {
                                    editTime.editText?.setText(
                                        onTimeSelected(
                                            this.hour,
                                            this.minute
                                        )
                                    )
                                }
                            }.show(
                                supportFragmentManager,
                                MaterialTimePicker::class.java.canonicalName
                            )
                    })

                    txt_delete.setOnClickListener(View.OnClickListener {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("?????? ??????")
                            .setMessage("????????? ????????? ?????????????????????????")
                            .setNegativeButton(
                                "??????",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
                                })
                            .setPositiveButton(
                                "??????",
                                DialogInterface.OnClickListener { dialog, which ->
                                    viewModel.setGoalCamstduyData(
                                        null,
                                        null,
                                        viewDataBinding.mainResponse!!.result[0].dream.ddayDate,
                                        viewDataBinding.mainResponse!!.result[0].dream.ddayName
                                    )
                                    dialog.dismiss()
                                    builder.dismiss()
                                }).show()
                    })

                    val btn_cancle =
                        mDialogView.findViewById<Button>(R.id.dialog_goal_btn_cancle)
                    val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_goal_btn_ok)

                    btn_cancle.setOnClickListener(View.OnClickListener {
                        builder.dismiss()
                    })

                    btn_ok.setOnClickListener(View.OnClickListener {
                        if ((editTime.editText?.text.isNullOrEmpty() && editGoal.editText?.text.isNullOrEmpty()) || (!editTime.editText?.text.isNullOrEmpty() && !editGoal.editText?.text.isNullOrEmpty())) {
                            if (isCheckGoal) {
                                val aimTime =
                                    changTime(editTime.editText?.text.toString()).toString()
                                val goal = editGoal.editText?.text.toString()

                                viewDataBinding.mainResponse!!.result[0].aimTime =
                                    editTime.editText?.text.toString()
                                viewDataBinding.mainResponse!!.result[0].dream.goal = goal

                                viewModel.setGoalCamstduyData(
                                    aimTime,
                                    goal,
                                    viewDataBinding.mainResponse!!.result[0].dream.ddayDate,
                                    viewDataBinding.mainResponse!!.result[0].dream.ddayName
                                )
                                builder.dismiss()
                            } else {
                                Toast.makeText(this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "?????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                R.id.ddat_setting -> {
                    Log.d(TAG, "ddat_setting")
                    val mDialogView =
                        LayoutInflater.from(con).inflate(R.layout.dialog_dday, null)
                    val mBuilder = AlertDialog.Builder(con).setView(mDialogView)
                    val builder = mBuilder.show()

                    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val editDate =
                        mDialogView.findViewById<TextInputLayout>(R.id.dialog_dday_edit_day)
                    val editDdayName =
                        mDialogView.findViewById<TextInputLayout>(R.id.dialog_dday_edit_name)
                    val txt_delete = mDialogView.findViewById<TextView>(R.id.dialog_dday_delete)
                    var isCheckDDay = true

                    if (viewDataBinding.mainResponse!!.result[0].dream.ddayDate != null && viewDataBinding.mainResponse!!.result[0].dream.ddayName != null) {
                        editDate.editText?.setText(viewDataBinding.mainResponse!!.result[0].dream.ddayDate)
                        editDdayName.editText?.setText(viewDataBinding.mainResponse!!.result[0].dream.ddayName)
                        txt_delete.visibility = View.VISIBLE
                    }

                    val changTextDDay: (CharSequence?, Int, Int, Int) -> Unit =
                        { charSequence: CharSequence?, i: Int, i1: Int, i2: Int ->
                            val result = PatternUtils.matcheDDay(charSequence.toString())
                            Log.d(TAG, charSequence.toString())

                            if (result.isNotError) {
                                editDdayName.isErrorEnabled = false
                                editDdayName.error = null
                                isCheckDDay = true
                            } else {
                                editDdayName.error = result.ErrorMessge
                                isCheckDDay = false
                            }
                        }

                    editDdayName.editText?.addTextChangedListener(onTextChanged = changTextDDay)

                    editDate.editText?.setOnClickListener(View.OnClickListener {
                        MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select date")
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build()
                            .apply {
                                addOnPositiveButtonClickListener {
                                    var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                                    editDate.editText?.setText(simpleDateFormat.format(it))
                                }
                            }.show(
                                supportFragmentManager,
                                MaterialDatePicker::class.java.canonicalName
                            )
                    })

                    txt_delete.setOnClickListener(View.OnClickListener {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("????????? ??????")
                            .setMessage("????????? ???????????? ?????????????????????????")
                            .setNegativeButton(
                                "??????",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
                                })
                            .setPositiveButton(
                                "??????",
                                DialogInterface.OnClickListener { dialog, which ->
                                    viewModel.setGoalCamstduyData(
                                        changTime(viewDataBinding.mainResponse!!.result[0].aimTime),
                                        viewDataBinding.mainResponse!!.result[0].dream.goal,
                                        null,
                                        null
                                    )
                                    dialog.dismiss()
                                    builder.dismiss()
                                }).show()
                    })

                    val btn_cancle =
                        mDialogView.findViewById<Button>(R.id.dialog_goal_btn_cancle)
                    val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_goal_btn_ok)

                    btn_cancle.setOnClickListener(View.OnClickListener {
                        builder.dismiss()
                    })

                    btn_ok.setOnClickListener(View.OnClickListener {

                        if ((editDate.editText?.text.isNullOrEmpty() && editDdayName.editText?.text.isNullOrEmpty()) || (!editDate.editText?.text.isNullOrEmpty() && !editDdayName.editText?.text.isNullOrEmpty())) {
                            if (isCheckDDay) {
                                val dday = editDate.editText?.text.toString()
                                val ddayName = editDdayName.editText?.text.toString()

                                viewDataBinding.mainResponse!!.result[0].dream.ddayDate = dday
                                viewDataBinding.mainResponse!!.result[0].dream.ddayName = ddayName

                                viewModel.setGoalCamstduyData(
                                    changTime(viewDataBinding.mainResponse!!.result[0].aimTime),
                                    viewDataBinding.mainResponse!!.result[0].dream.goal,
                                    dday,
                                    ddayName
                                )
                                builder.dismiss()
                            } else {
                                Toast.makeText(this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "?????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            false
        }
        popup.show()

    }

    private fun onTimeSelected(hour: Int, minute: Int): String {
        val hourAsText = if (hour < 10) "0$hour" else hour
        val minuteAsText = if (minute < 10) "0$minute" else minute

        return "$hourAsText:$minuteAsText"
    }

    fun NewStudy_init() {
        //?????????
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.main_newstudy_recylerView)
        var newAdapter = MainOtherStudyAdapter(this, passwordDialog)

        if (NewStudyShowOpen) {
            newAdapter.datas = viewDataBinding.mainResponse!!.result[0].newOpenStudy.toMutableList()
            viewDataBinding.isNewStudy =
                if (viewDataBinding.mainResponse!!.result[0].newOpenStudy.size < 1) true else false
        } else {
            newAdapter.datas =
                viewDataBinding.mainResponse!!.result[0].newGroupStudy.toMutableList()
            viewDataBinding.isNewStudy =
                if (viewDataBinding.mainResponse!!.result[0].newGroupStudy.size < 1) true else false
        }
        recyclerNewStudy.adapter = newAdapter
    }


    fun Recommend_init() {
        //??????
        var recyclerRecommendStudy: RecyclerView =
            findViewById(R.id.main_recommend_study_recylerView)
        var recommendAdapter: MainOtherStudyAdapter = MainOtherStudyAdapter(this, passwordDialog)

        if (RecommendStudyShowOpen) {
            recommendAdapter.datas =
                viewDataBinding.mainResponse!!.result[0].openRecommend.toMutableList()
            viewDataBinding.isRecommendStudy =
                if (viewDataBinding.mainResponse!!.result[0].openRecommend.size < 1) true else false
        } else {
            recommendAdapter.datas =
                viewDataBinding.mainResponse!!.result[0].groupRecommend.toMutableList()
            viewDataBinding.isRecommendStudy =
                if (viewDataBinding.mainResponse!!.result[0].groupRecommend.size < 1) true else false
        }
        recyclerRecommendStudy.adapter = recommendAdapter
    }

    fun changTime(goalTime: String?): String? {
        if (goalTime != null) {
            val time = goalTime.split(":")
            val hour = time[0].toInt() * 60 * 60
            val minute = time[1].toInt() * 60

            return (hour + minute).toString()
        }
        return null
    }

    fun moveTodolist() {
        var intent = Intent(this, TodoListActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun showStudyDescription(){
        Log.d(TAG,"showStudyDescription : "+showStudyDescription)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_VERSION_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    // ?????? ????????????
    fun updaterequest() {
        appUpdateManager  = AppUpdateManagerFactory.create(this)
        appUpdateManager!!.let {
            it.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // or AppUpdateType.FLEXIBLE
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, // or AppUpdateType.FLEXIBLE
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
        }
    }*/

     fun showSuccessPostDialog(goalTime: Int, iswrite:Boolean){
         if (iswrite){
             // ?????? ???????????? ???????????? ??????????????????
             return
         }
         var dialog = SuccessPostDialog.Builder(this)
             .setGoalTime(goalTime)
             .show()
     }

     fun shareSuccessPost(contents: String){
        viewModel.setSuccessPostData(contents)
     }


}