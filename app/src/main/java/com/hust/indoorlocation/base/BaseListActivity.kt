package com.hust.indoorlocation.base

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hust.indoorlocation.tools.ext.showSnackMsg
import com.yechaoa.yutilskt.LogUtil
import com.yechaoa.yutilskt.YUtils.hideLoading


/**
 * 视图绑定，ViewModel ,数据类
 */
abstract class BaseListActivity<VB : ViewBinding, LD : Any> :
    BaseActivity<VB>(){

    /**
     * 每页数据的个数
     */
    var mTotalCount = 20 //每次加载数量,为常量不变
    var mCurrentSize = 0 //当前加载数量，每次获取返回结果时改变 it=data.size，当it<mTotalCount表示数据已到达结尾
    var mCurrentPage = 0 //当前加载页数，刷新或获取新数据列表时变为 it=0,加载更多时 it++
    var mPosition = -1  //当前点击位置
    var mRecyclerView: RecyclerView? = null
    var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    var mFloatingActionBtn: FloatingActionButton? = null

    val mListAdapter: BaseQuickAdapter<LD, *> by lazy { attachAdapter() }

    /**
     * 是否是下拉刷新,适配器判断用
     */
    var isRefresh = true

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

    fun attachLayoutManager() = linearLayoutManager
    fun attachItemDecoration() = recyclerViewItemDecoration


    override fun onDestroy() {
        super.onDestroy()
        mCurrentSize = 0
        mCurrentPage = 0
    }

    abstract fun attachAdapter(): BaseQuickAdapter<LD, *>

    override fun initialize(saveInstanceState: Bundle?) {
        LogUtil.d("1321413")
        initListView()
        mListAdapter.apply {
            //开启加载动画
            animationEnable = true
            //加载动画为左侧划入
            setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn)
            //绑定视图
            //recyclerView=mRecyclerView
            //设置空布局,调用此方法前需要 recyclerView.adapter=MAdapter
            // setEmptyView(R.layout.layout_empty_view)

            //允许侧滑、拖动
            draggableModule.isSwipeEnabled = useDrag()
            draggableModule.isDragEnabled = useSwipe()
            //自动加载
            loadMoreModule.isAutoLoadMore = useAutoLoadMore()
            //加载更多
            loadMoreModule.setOnLoadMoreListener { onLoadMoreList() }

            //item点击，在fragment中完成，也可以在adapter中完成
            setOnItemClickListener(onItemClickListener)
            //item子view点击，收藏
            setOnItemChildClickListener(onItemChildClickListener)

        }

        //适配器绑定视图,不绑定不显示
        mRecyclerView?.apply {
            layoutManager = attachLayoutManager()
            adapter = mListAdapter
            itemAnimator = DefaultItemAnimator()
            //添加分割线
            attachItemDecoration()?.let { addItemDecoration(it) }
        }
        //下拉刷新
        mSwipeRefreshLayout?.apply {
            setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
            )
            setOnRefreshListener { onRefreshList() }
        }


        //滑动顶部
        mFloatingActionBtn?.setOnClickListener {
            scrollToTop()
        }
    }

    abstract fun initListView()
    open fun useDrag(): Boolean = true
    open fun useSwipe(): Boolean = true
    open fun useDecoration(): Boolean = true
    open fun useAutoLoadMore(): Boolean = false

    fun scrollToTop() {
        mRecyclerView?.run {
            if (linearLayoutManager.findFirstVisibleItemPosition() > 10) {
                scrollToPosition(0)
            } else {
                smoothScrollToPosition(0)
            }
        }
        showSnackMsg("scrollToTop ")
    }

    /**
     * ItemClickListener
     */
    val onItemClickListener = OnItemClickListener { adapter, view, position ->
        onItemClickEvent(adapter, view, position)
    }

    /**
     * ItemChildClickListener
     */
    val onItemChildClickListener = OnItemChildClickListener { adapter, view, position ->
        onItemChildClickEvent(adapter, view, position)
    }

    open fun onItemClickEvent(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

    }

    open fun onItemChildClickEvent(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {

    }

    /**
     * RefreshListener  下拉刷新页面
     */
    open fun onRefreshList() {
        mSwipeRefreshLayout?.postDelayed({
            isRefresh = true
            //是否打开加载更多
            //     mListAdapter.loadMoreModule.isEnableLoadMore=false
            mSwipeRefreshLayout?.isRefreshing = false
            refreshList()
            showSnackMsg("refresh page at : " + javaClass.simpleName)
        }, 1500)
    }


    open fun refreshList() { }
    open fun loadMoreList() { }

    /**
     * LoadMoreListener 上拉加载更多,在适配器处使用
     */
    open fun onLoadMoreList() {
        hideLoading()
        mRecyclerView?.postDelayed({
            isRefresh = false
            //刷新视图是否应显示刷新进度，关闭刷新loading
            mSwipeRefreshLayout?.isRefreshing = false

            if (mCurrentSize < mTotalCount) {
                //页面没满,已达结尾
                mListAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
                mCurrentPage++
                loadMoreList()
                showSnackMsg("Load more at : " + javaClass.simpleName)
            }
        }, 500)
    }

    fun showAtAdapter(it: MutableList<LD>) {
        mCurrentSize = it.size
        mListAdapter.apply {

            //如果是刷新数据，则替换adapter
            if (0 == mCurrentPage) {
                setList(it)
            } else {
                addData(it)
                // loadMoreModule.loadMoreComplete()
            }
            loadMoreModule.loadMoreEnd(gone = true)
            when {
                //加载数据小于返回总数，即到达结尾,不可再加载 （不显示没有更多数据）,true表示可以加载更多数据
                mCurrentSize < mTotalCount -> {
                    loadMoreModule.loadMoreEnd(false)
                }
                //加载数据大于返回总数，即一次性返回数据。到达结尾
                mCurrentSize > mTotalCount -> {
                    loadMoreModule.loadMoreEnd(false)
                }
                else -> {
                    loadMoreModule.loadMoreComplete()
                }
            }
        }
    }
}