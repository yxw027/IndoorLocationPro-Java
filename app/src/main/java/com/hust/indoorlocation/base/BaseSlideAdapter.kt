package com.hust.indoorlocation.base

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yechaoa.yutilskt.ToastUtil

abstract class BaseSlideAdapter<T,  VH : BaseViewHolder>(layoutResId: Int, data: MutableList<T>? = null) :
    BaseQuickAdapter<T, VH>(layoutResId,data),LoadMoreModule,DraggableModule{


    init {
        val status=1
        //设置头部和尾部
        //setHeaderView(mBinding.root)
        //setFooterView(mBinding.root)
        //设置空布局,调用此方法前需要 recyclerView.adapter=MAdapter
      //  setEmptyView(R.layout.empty_view)
        //开启加载动画,设置为 缩放显示
        animationEnable = true
        setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInLeft)
        initBaseSlideAdapter()


        //自动加载数据
        loadMoreModule.isAutoLoadMore=false
        loadMoreModule.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                ToastUtil.show("正在加载数据...")
                when(status){
                    //状态-》成功，失败，到结尾
                    1->loadMoreModule.loadMoreComplete()
                    2->loadMoreModule.loadMoreFail()
                    3->loadMoreModule.loadMoreEnd()
                }
            }
        })

    }


    private val itemChildClickListener=View.OnClickListener(){
        when(it.id){
            else-> ToastUtil.show("you click ${it.id} item ")
        }
    }

    open fun useDrag():Boolean =false
    open fun useSwipe():Boolean =false

    private fun initBaseSlideAdapter(){
        //允许侧滑、拖动
        if(useSwipe()){
            draggableModule.isSwipeEnabled=useSwipe()
            draggableModule.setOnItemDragListener(object : OnItemDragListener {
                //拖拽开始
                override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {

                }

                override fun onItemDragMoving(
                    source: RecyclerView.ViewHolder?,
                    from: Int,
                    target: RecyclerView.ViewHolder?,
                    to: Int,
                ) {

                }
                //拖拽结束
                override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {

                }
            })
        }
        if(useDrag()){
            draggableModule.isDragEnabled=useDrag()

            draggableModule.setOnItemSwipeListener(object : OnItemSwipeListener {
                override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {

                }

                override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {

                }

                override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {

                }

                override fun onItemSwipeMoving(
                    canvas: Canvas?,
                    viewHolder: RecyclerView.ViewHolder?,
                    dX: Float,
                    dY: Float,
                    isCurrentlyActive: Boolean,
                ) {

                }
            })
        }
    }
}