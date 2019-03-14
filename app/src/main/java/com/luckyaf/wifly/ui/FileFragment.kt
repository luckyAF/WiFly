package com.luckyaf.wifly.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.luckyaf.kommon.base.BaseFragment
import com.luckyaf.kommon.extension.addAllTo
import com.luckyaf.kommon.manager.AppExecutors
import com.luckyaf.kommon.widget.adapter.CommonRecyclerAdapter
import com.luckyaf.kommon.widget.adapter.CommonRecyclerHolder
import com.luckyaf.wifly.R
import com.luckyaf.wifly.constant.Constants
import com.luckyaf.wifly.model.FileModel
import com.luckyaf.wifly.utils.FileUtils
import kotlinx.android.synthetic.main.fragment_file.*
import android.support.v7.widget.LinearLayoutManager
import com.luckyaf.kommon.component.RxBus
import com.luckyaf.kommon.component.SmartJump
import com.luckyaf.kommon.extension.DEBUG
import com.luckyaf.kommon.extension.clickWithTrigger
import com.luckyaf.kommon.widget.dialog.Alert
import java.io.File
import com.luckyaf.wifly.utils.UriUtils
import io.reactivex.disposables.Disposable


/**
 * 类描述： 文件列表页
 * @author Created by luckyAF on 2019-02-15
 *
 */
class FileFragment : BaseFragment() {
    private val mAppExecutors = AppExecutors()

    companion object {
        fun newInstance() = FileFragment()
    }

    private val fileList = ArrayList<FileModel>()
    private lateinit var fileAdapter:CommonRecyclerAdapter<FileModel>

    private val smartJump by lazy {
        SmartJump.from(this)
    }

    private var disposable:Disposable?=null
    override fun getLayoutId() = R.layout.fragment_file
    override fun initData(bundle: Bundle?) {
    }

    override fun initView(savedInstanceState: Bundle?, contentView: View) {
        fileAdapter = object :CommonRecyclerAdapter<FileModel>(mActivity,fileList){
            override fun getItemLayoutId(viewType: Int) = R.layout.item_file_model
            override fun bindData(holder: CommonRecyclerHolder, data: FileModel, position: Int) {
                holder.setText(R.id.txtFileName,data.name)
                holder.setText(R.id.txtFileDetail,"${data.getFileSize()}  上次修改:${data.getLastModifiedTime()}")
                holder.setOnItemClickListener {
                    FileUtils.openFile(mActivity,data.path)
                }
                holder.setOnItemLongClickListener {
                    data.name.DEBUG()
                    deleteFile(data.path)
                    true
                }
            }
        }

        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = fileAdapter

        swipeRefreshLayout.setOnRefreshListener {
            loadFileList()
        }

        fabAdd.clickWithTrigger {
            addFile()
        }

    }

    override fun start() {
        initDir()
        loadFileList()
        disposable = RxBus.toFlowable().subscribe {
            loadFileList()
        }

    }

    private fun initDir(){
        val dir = File(Constants.serverDir)
        if(!dir.exists()){
            dir.mkdirs()
        }
    }

    private fun addFile(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "file/*"
        smartJump.startForResult(intent,object :SmartJump.Callback{
            override fun onActivityResult(resultCode: Int, data: Intent?) {
                val uri = data?.data
                uri?.let {
                    val oldFile = UriUtils.uri2File(it)
                    if(null != oldFile){
                        FileUtils.copyFile(oldFile,"${Constants.serverDir}/${oldFile.name}")

                    }
                }
            }
        })
    }



    private fun deleteFile(path:String){
        Alert.normal(mActivity,"警告","确认删除该文件?"){type, _ ->
            if(type == Alert.CONFIRM){
                FileUtils.deleteFile(File(path))
                loadFileList()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        disposable?.dispose()
    }

    private fun loadFileList() {
        mAppExecutors.runOnIoThread {
            fileList.clear()
            val files = FileUtils.getFilesByDir(Constants.serverDir)
            files?.addAllTo(fileList)
            mAppExecutors.runOnMainThread {
                fileAdapter.updateData(fileList)
                //刷新完成
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

}