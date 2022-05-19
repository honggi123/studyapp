package com.coworkerteam.coworker.ui.yourday.moodPost

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MoodPostResponse
import com.coworkerteam.coworker.ui.yourday.YourdayViewModel
import com.coworkerteam.coworker.ui.yourday.moodPost.edit.EditMoodPostActivity
import com.coworkerteam.coworker.utils.DateFormatUtils
import com.google.gson.Gson
import kotlin.collections.ArrayList

class MoodPostAdapter (private val viewmodel : YourdayViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private lateinit var items_empathykind: ArrayList<View>

    private lateinit var items: ArrayList<MoodPostResponse.Result.MoodPost?>
    private var dialog_showing : Boolean = false
    lateinit var removeitem : MoodPostResponse.Result.MoodPost
    lateinit var context: Context
    val gson = Gson()
    // lateinit var itembinding : ItemMoodpostBinding
    var postPosition : Int = 0

    fun setdata(items: ArrayList<MoodPostResponse.Result.MoodPost?>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context
        return if (viewType == VIEW_TYPE_ITEM) {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_moodpost, parent, false)
            ItemViewHolder(view)
            /*
            var binding : ItemMoodpostBinding = ItemMoodpostBinding.inflate(LayoutInflater.from(parent.context), parent,false)
            itembinding = binding
            itembinding.adapter = this
            ItemViewHolder(binding)
            */
        } else {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is LoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items!![position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size
    }

    private fun showLoadingView(holder: LoadingViewHolder, position: Int) {}

    private fun populateItemRows(holder: ItemViewHolder, position: Int) {
        val item = items!![position]
        holder.dialog_empathy.visibility = View.GONE

        if (item!!.total_empathy == 0){
            holder.txt_totalempathynum.visibility = View.INVISIBLE
        }else{
            holder.txt_totalempathynum.visibility = View.VISIBLE
        }

        holder.btn_empathy.setOnClickListener(View.OnClickListener {
            if (item!!.is_empathy.toBoolean()){
                holder.dialog_empathy.visibility = View.GONE
                empathy(item!!.idx,item!!.my_empathy,position)
            }else{
                holder.dialog_empathy.visibility = View.VISIBLE
            }
        })

        if (item!!.is_empathy.toBoolean()){
            holder.btn_empathy.setText("공감했어요")
            holder.btn_empathy.setTextColor(context.resources.getColor(R.color.empathy_color))
        }else{
            holder.btn_empathy.setText("공감하기")
            holder.btn_empathy.setTextColor(context.resources.getColor(R.color.gray))
        }

        holder.txt_nickname.setText(item?.nickname)

        var diff_date = DateFormatUtils.daysToStringformat(item.create_date,item.create_date)
        holder.txt_create_date.setText(diff_date)
        holder.txt_contents.setText(item?.contents)

        if (viewmodel.getUserName().equals(item?.nickname)){
            holder.btn_remove.visibility = View.VISIBLE
            holder.btn_edt.visibility = View.VISIBLE
        }else{
            holder.btn_remove.visibility = View.INVISIBLE
            holder.btn_edt.visibility = View.INVISIBLE
        }

        holder.btn_remove.setOnClickListener(View.OnClickListener {
            item?.idx?.let { it1 -> viewmodel.deleteMoodPostdData(it1) }
            if (item != null) {
                removeitem = item
            }
        })

        holder.btn_edt.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, EditMoodPostActivity::class.java)
            intent.putExtra("idx", item.idx)
            intent.putExtra("mood", item.mood)
            intent.putExtra("create_date", diff_date)
            intent.putExtra("contents", item.contents)
            context.startActivity(intent)
        })

        var emotionname = "mood_emoticon"+item?.mood
        Glide.with(context).load(context.getResources().getIdentifier(emotionname,"drawable",context.getPackageName())
        ).into(holder.view_mood)

        holder.txt_totalempathynum.setText(item?.total_empathy.toString())

        var empathy_kinds = item?.empathy_kinds?.split(",")

        // 게시글 공감 이모티콘 모두 안보이게 한 후 게시물의 공감 종류에 따라 해당 이모티콘이 보이게 한다.
        for (i in 1..5){
            holder.layout_emoticon_kinds.get(i-1).visibility = View.GONE
        }
        if (item?.total_empathy != 0) {
            for (i in 1..empathy_kinds.size){
                holder.layout_emoticon_kinds.get(empathy_kinds[i-1].toInt()-1).visibility = View.VISIBLE
            }
        }

        holder.dialog_empathy_kinds1.setOnClickListener(View.OnClickListener {
            empathy(item!!.idx,1,position)
            holder.dialog_empathy.visibility = View.GONE
        })

        dialog_empathy_onclick(holder.dialog_empathy_kinds1,item!!.idx,1,position,holder)
        dialog_empathy_onclick(holder.dialog_empathy_kinds2,item!!.idx,2,position,holder)
        dialog_empathy_onclick(holder.dialog_empathy_kinds3,item!!.idx,3,position,holder)
        dialog_empathy_onclick(holder.dialog_empathy_kinds4,item!!.idx,4,position,holder)
        dialog_empathy_onclick(holder.dialog_empathy_kinds5,item!!.idx,5,position,holder)

    }

    fun dialog_empathy_onclick(view: View,idx : Int,mood : Int,position : Int,holder: ItemViewHolder){
        view.setOnClickListener(View.OnClickListener {
            empathy(idx,mood,position)
            holder.dialog_empathy.visibility = View.GONE
        })
    }


    fun empathy(postNum: Int,mood: Int,postPosition : Int){
        viewmodel.empathy(postNum,mood)
        this.postPosition = postPosition
    }

    fun adddata(datas: ArrayList<MoodPostResponse.Result.MoodPost>?) {
            if (datas != null) {
                items.addAll(datas)
            }
            notifyDataSetChanged()
        }
    }

    class ItemViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        var view_mood : ImageView
        var btn_empathy: TextView
        var dialog_empathy: ConstraintLayout
        var txt_nickname : TextView
        var txt_create_date : TextView
        var txt_contents : TextView
        var btn_edt : ImageView
        var btn_remove : ImageView
        var txt_totalempathynum : TextView
        var layout_emoticon_kinds : LinearLayout
        var layout_empathy_kinds : LinearLayout
        var dialog_empathy_kinds1 : ImageView
        var dialog_empathy_kinds2 : ImageView
        var dialog_empathy_kinds3 : ImageView
        var dialog_empathy_kinds4 : ImageView
        var dialog_empathy_kinds5 : ImageView

        init {
            layout_emoticon_kinds = itemView.findViewById(R.id.item_moodpost_empathy_kinds)
            layout_empathy_kinds = itemView.findViewById<LinearLayout>(R.id.dialog_empathy_kinds)

            view_mood = itemView.findViewById(R.id.item_mood_post_mood)
            btn_empathy = itemView.findViewById(R.id.txt_empathy)
            dialog_empathy = itemView.findViewById(R.id.dialog_empathy)
            txt_nickname = itemView.findViewById(R.id.item_mood_post_nickname)
            txt_create_date = itemView.findViewById(R.id.item_mood_post_date)
            txt_contents = itemView.findViewById(R.id.item_mood_post_content)
            btn_edt = itemView.findViewById(R.id.item_mood_post_btn_edit)
            btn_remove = itemView.findViewById(R.id.item_mood_post_btn_remove)
            txt_totalempathynum = itemView.findViewById(R.id.item_mood_post_total_empathynum)

            dialog_empathy_kinds1 = itemView.findViewById(R.id.dialog_empathy_kinds1)
            dialog_empathy_kinds2 = itemView.findViewById(R.id.dialog_empathy_kinds2)
            dialog_empathy_kinds3 = itemView.findViewById(R.id.dialog_empathy_kinds3)
            dialog_empathy_kinds4 = itemView.findViewById(R.id.dialog_empathy_kinds4)
            dialog_empathy_kinds5 = itemView.findViewById(R.id.dialog_empathy_kinds5)
            /*
           layout_emoticon_kinds = binding.itemMoodpostEmpathyKinds
           layout_empathy_kinds = binding.dialogEmpathyKinds

           view_mood = binding.itemMoodPostMood
           btn_empathy = binding.txtEmpathy
           dialog_empathy = binding.dialogEmpathy
           txt_nickname = binding.itemMoodPostNickname
           txt_create_date = binding.itemMoodPostDate
           txt_contents = binding.itemMoodPostContent
           btn_edt = binding.itemMoodPostBtnEdit
           btn_remove = binding.itemMoodPostBtnRemove
           txt_totalempathynum = binding.itemMoodPostTotalEmpathynum
            */

        }
    }

    private class LoadingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar

        init {
            progressBar = itemView.findViewById(R.id.progressBar)
        }
    }

