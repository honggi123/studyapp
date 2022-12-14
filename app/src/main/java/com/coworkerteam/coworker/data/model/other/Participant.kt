package com.coworkerteam.coworker.data.model.other

import android.content.Context
import android.util.Log
import org.webrtc.*

class Participant(context: Context, name: String){
    val TAG = "Participant"

    var idx: Int = -1


    var itemView: CamStudyItemView = CamStudyItemView(context,false)
    var myitemView: CamStudyItemView = CamStudyItemView(context,false)
    var itemViewScreen: CamStudyItemView = CamStudyItemView(context,true)

    var context : Context = context

    var timer: CamStudyTimer

    var mirrormode = true

    var peer: PeerConnection? = null
    var peerScreen: PeerConnection? = null

    var remoteVideoTrack: VideoTrack? = null
    var remoteAudioTrack: AudioTrack? = null

    var remoteVideoShareTrack: VideoTrack? = null
    var remoteAudioShareTrack: AudioTrack? = null

    //오디오와 비디오의 on/off 상태
    var isAudio: Boolean = true
    var isVideo: Boolean = true

    lateinit var videorender : VideoRenderer

    init {
        itemView.userNameView.text = name
        timer = CamStudyTimer(itemView.timerTextView, itemView.timerImageView)
    }

    fun settingDevice(isVideo: Boolean, isAudio: Boolean) {
        this.isVideo = isVideo
        this.isAudio = isAudio

        itemView.showProfileImage(isVideo)
        itemView.changAudioImage(isAudio)
    }

    //surfaceView에 받아온 비디오를 그리고, 받아온 오디오를 재생하는 함수
    fun startRender(remoteVideoTrack: VideoTrack?, remoteAudioTrack: AudioTrack?) {
        Log.d(TAG, " isvideo : $isVideo/ isaudio : $isAudio")
        itemView.showProfileImage(isVideo)
        itemView.changAudioImage(isAudio)

        this.remoteVideoTrack = remoteVideoTrack
        this.remoteAudioTrack = remoteAudioTrack

        remoteAudioTrack?.setEnabled(isAudio)
        remoteVideoTrack?.setEnabled(isVideo)
        videorender = VideoRenderer(itemView.surfaceView)
        remoteVideoTrack?.addRenderer(videorender)
        myitemView = itemView
    }


    //surfaceView에 받아온 비디오를 그리고, 받아온 오디오를 재생하는 함수
    fun startRenderScreen(remoteVideoTrack: VideoTrack?, remoteAudioTrack: AudioTrack?) {
        Log.d(TAG, " isvideo : $isVideo/ isaudio : $isAudio")
        itemViewScreen.showProfileImage(true)
        itemViewScreen.changAudioImage(true)

        this.remoteVideoShareTrack = remoteVideoTrack
        this.remoteAudioShareTrack = remoteAudioTrack

        remoteAudioShareTrack?.setEnabled(false)
        remoteVideoShareTrack?.setEnabled(true)

        videorender = VideoRenderer(itemViewScreen.surfaceView)
        remoteVideoShareTrack?.addRenderer(videorender)
    }

    //Audio를 on/off 하는 함수
    fun toggleAudio(status: String) {
        if (status == "off") {
            isAudio = false
            if (remoteAudioTrack != null) {
                remoteAudioTrack!!.setEnabled(false)
            }
        } else if (status == "on") {
            isAudio = true

            if (remoteAudioTrack != null) {
                remoteAudioTrack!!.setEnabled(true)
            }
        }

        itemView.changAudioImage(status)
    }

    //Video를 on/off 하는 함수
    fun toggleVideo(status: String) {
        if (status == "off") {
            isVideo = false

            if (remoteVideoTrack != null) {
                remoteVideoTrack!!.setEnabled(false)
            }
        } else if (status == "on") {
            isVideo = true

            if (remoteVideoTrack != null) {
                remoteVideoTrack!!.setEnabled(true)
            }
        }
        itemView.showProfileImage(status)
    }


    //캠스터디 종료할때, 진행중이던 타이머와 비디오를 멈추기 위한 함수
    fun stopCamStduy() {
        timer.endTimer()
        itemView.surfaceView.pauseVideo()
        itemView.surfaceView.release()

        if (remoteVideoTrack != null) {
            remoteVideoTrack!!.setEnabled(false)
            remoteVideoTrack!!.removeRenderer(videorender)
            remoteVideoTrack?.dispose()
            remoteVideoTrack = null
        }
        if(remoteAudioTrack != null){
            remoteAudioTrack?.setEnabled(false)
            remoteAudioTrack?.dispose()
            remoteAudioTrack = null
        }

        peer?.close()
        peer = null
    }


    //캠스터디 종료할때, 진행중이던 타이머와 비디오를 멈추기 위한 함수
    fun stopCamStduyScreen() {
        itemViewScreen.surfaceView.pauseVideo()


        if (remoteVideoShareTrack != null) {
            remoteVideoShareTrack!!.setEnabled(false)
            remoteVideoShareTrack!!.removeRenderer(videorender)
            remoteVideoShareTrack?.dispose()
            remoteVideoShareTrack = null
        }
        if(remoteAudioShareTrack != null){
            remoteAudioShareTrack?.setEnabled(false)
            remoteAudioShareTrack?.dispose()
            remoteAudioShareTrack = null
        }

        peerScreen?.close()
        peerScreen = null
    }

    fun horizontalflipcamera(){
        if(mirrormode){
            itemView.surfaceView.setMirror(false)
            mirrormode = false
        }else{
            itemView.surfaceView.setMirror(true)
            mirrormode = true
        }
    }

    fun changehighlight(status: Boolean){
        itemView.changeHighlight(status)
    }


}