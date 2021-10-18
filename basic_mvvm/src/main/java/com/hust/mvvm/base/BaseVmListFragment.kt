package com.hust.mvvm.base

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hust.mvvm.widgets.SpaceItemDecoration

/**
 * 视图绑定，ViewModel ,数据类
 */
abstract class BaseVmListFragment<VB : ViewBinding, VM : BaseViewModel, LD : Any> :
    BaseVmFragment<VB, VM>(), IListView<LD> {

    override var mTotalCount = 20 //每次加载数量,为常量不变
    override var mCurrentSize = 0 //当前加载数量，每次获取返回结果时改变 it=data.size，当it<mTotalCount表示数据已到达结尾
    override var mCurrentPage = 0 //当前加载页数，刷新或获取新数据列表时变为 it=0,加载更多时 it++
    override var mPosition = -1  //当前点击位置

    override var isRefresh = true

    /**
     * LinearLayoutManager
     */
    val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(activity)
    }
    /**
     * RecyclerView Divider
     */
    val recyclerViewItemDecoration by lazy {
        activity?.let {
            SpaceItemDecoration(it)
        }
    }
    override fun attachLayoutManager() =linearLayoutManager
    override fun attachItemDecoration() =recyclerViewItemDecoration


    override fun onDestroy() {
        super.onDestroy()
        mCurrentSize = 0
        mCurrentPage = 0
    }

    override var mRecyclerView: RecyclerView? = null
    override var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override var mFloatingActionBtn: FloatingActionButton? = null
    override val mListAdapter: BaseQuickAdapter<LD, *> by lazy { attachAdapter() }

    abstract fun attachAdapter(): BaseQuickAdapter<LD, *>

    override fun initView() {
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

    override fun scrollToTop() {
        super.scrollToTop()
        mRecyclerView?.run {
            if (linearLayoutManager.findFirstVisibleItemPosition() > 10) {
                scrollToPosition(0)
            } else {
                smoothScrollToPosition(0)
            }
        }
        showMsg("scrollToTop ")
    }

    /**
     * ItemClickListener
     */
    override val onItemClickListener = OnItemClickListener { adapter, view, position ->
        onItemClickEvent(mListAdapter.data, view, position)
    }

    /**
     * ItemChildClickListener
     */
    override val onItemChildClickListener = OnItemChildClickListener { adapter, view, position ->
        onItemChildClickEvent(mListAdapter.data, view, position)
    }

    open fun onItemClickEvent(datas: MutableList<LD>, view: View, position: Int) {

    }

    open fun onItemChildClickEvent(datas: MutableList<LD>, view: View, position: Int) {

    }

    override fun showAtAdapter(it: MutableList<LD>) {
        hideLoading()
        super.showAtAdapter(it)
        if (mListAdapter.data.isEmpty()) {
            mLayoutStatusView?.showEmpty()
        } else {
            mLayoutStatusView?.showContent()
        }
    }
}