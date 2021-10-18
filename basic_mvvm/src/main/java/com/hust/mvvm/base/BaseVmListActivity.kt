package com.hust.mvvm.base

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hust.mvvm.widgets.SpaceItemDecoration

/**
 * 视图绑定，ViewModel ,数据类
 */
abstract class BaseVmListActivity<VB : ViewBinding, VM : BaseViewModel, LD : Any> :
    BaseVmActivity<VB, VM>() ,IListView <LD>{

    /**
     * 每页数据的个数
     */
    override  var mTotalCount = 20 //每次加载数量,为常量不变
    override  var mCurrentSize = 0 //当前加载数量，每次获取返回结果时改变 it=data.size，当it<mTotalCount表示数据已到达结尾
    override  var mCurrentPage = 0 //当前加载页数，刷新或获取新数据列表时变为 it=0,加载更多时 it++
    override  var mPosition = -1  //当前点击位置

    /**
     * 是否是下拉刷新,适配器判断用
     */
    override  var isRefresh = true

    /**
     * LinearLayoutManager
     */
    protected val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(this)
    }

    /**
     * RecyclerView Divider 分割线
     */
    private val recyclerViewItemDecoration by lazy {
            SpaceItemDecoration(this)
    }

    override fun attachLayoutManager() =linearLayoutManager
    override fun attachItemDecoration() =recyclerViewItemDecoration


    override fun onDestroy() {
        super.onDestroy()
        mCurrentSize = 0
        mCurrentPage = 0
    }

    override  var mRecyclerView: RecyclerView? = null
    override  var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override  var mFloatingActionBtn: FloatingActionButton? = null

    override val mListAdapter: BaseQuickAdapter<LD, *> by lazy { attachAdapter() }

    abstract fun attachAdapter(): BaseQuickAdapter<LD, *>

    override fun initListener(){

    }

    override fun initView() {
        initListView()
    }

    override fun scrollToTop() {
        super.scrollToTop()
        showMsg("scrollToTop ")
    }

    /**
     * ItemClickListener
     */
    override val onItemClickListener = OnItemClickListener { adapter, view, position ->
        onItemClickEvent(adapter,view, position)
    }

    /**
     * ItemChildClickListener
     */
    override val onItemChildClickListener = OnItemChildClickListener { adapter, view, position ->
        onItemChildClickEvent(adapter,view, position)
    }

    open fun onItemClickEvent(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

    }

    open fun onItemChildClickEvent(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

    }

    override fun showAtAdapter(it :MutableList<LD>){
        hideLoading()
        super.showAtAdapter(it)
        if (mListAdapter.data.isEmpty()) {
            mLayoutStatusView?.showEmpty()
        } else {
            mLayoutStatusView?.showContent()
        }
    }
}