package com.hust.indoorlocation.base

import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yechaoa.yutilskt.ToastUtil


/**
 * @author ve
 * @date 2021/2/26
 * @desc 带有List的fragment接口
 */
interface IListView<LD : Any> {

    /**
     * 分页参数，每页数据的个数
     */
    var mTotalCount: Int //每次加载数量,为常量不变。应根据实际需要初始化
    var mCurrentSize: Int //当前加载数量，为bean返回结果的大小。即it=data.size，当it<mTotalCount表示数据已到达结尾
    var mCurrentPage: Int //当前加载页数，刷新页面数据时重置为0 ,加载更多时it++
    var mPosition: Int  //当前点击位置

    var mRecyclerView: RecyclerView?
    var mSwipeRefreshLayout: SwipeRefreshLayout?
    var mFloatingActionBtn: FloatingActionButton?
    val mListAdapter: BaseQuickAdapter<LD, *>


//    var linearLayoutManager: LinearLayoutManager
//    var recyclerViewItemDecoration: SpaceItemDecoration
//    var onRefreshListener: SwipeRefreshLayout.OnRefreshListener
//    val onLoadMoreListener: OnLoadMoreListener

    /**
     * 是否是下拉刷新,适配器判断用。如果是下拉刷新，则重置页面，否则页面++
     */
    var isRefresh: Boolean

    /**
     * LinearLayoutManager 布局管理器
     */
    fun attachLayoutManager(): RecyclerView.LayoutManager?

    /**
     * RecyclerView Divider 分割线
     */
    fun attachItemDecoration(): RecyclerView.ItemDecoration?

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
            ToastUtil.show("refresh page at : " + javaClass.simpleName)
        }, 1500)
    }


    open fun refreshList() { }
    open fun loadMoreList() { }

    /**
     * LoadMoreListener 上拉加载更多,在适配器处使用
     */
    open fun onLoadMoreList() {
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
                ToastUtil.show("Load more at : " + javaClass.simpleName)
            }
        }, 500)
    }


    open fun useDrag(): Boolean = true
    open fun useSwipe(): Boolean = true
    open fun useDecoration(): Boolean = true
    open fun useAutoLoadMore(): Boolean = false

    /**
     * 初始化ListView相关, 初始化 recyclerview，swipeRefreshView 相关数据，应该在initView之前调用
     */
    fun initListView()

    fun scrollToTop(){

    }

    /**
     * ItemClickListener
     */
    val onItemClickListener: OnItemClickListener

    /**
     * ItemChildClickListener
     */
    val onItemChildClickListener: OnItemChildClickListener


    open fun showAtAdapter(it: MutableList<LD>) {
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

