package kim.jeonghyeon.androidlibrary.extension

import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kim.jeonghyeon.androidlibrary.ui.binder.recyclerview.BasePagedListAdapter
import kim.jeonghyeon.androidlibrary.ui.binder.recyclerview.BaseRecyclerViewAdapter
import kim.jeonghyeon.androidlibrary.ui.binder.recyclerview.DiffComparable

fun <VM : DiffComparable<VM>> RecyclerView.bindDiffComparable(
    itemList: List<VM>?,
    layoutId: Int,
    lifecycleOwner: LifecycleOwner?
) {
    if (itemList == null) return

    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context)
    }

    @Suppress("UNCHECKED_CAST")
    val adapter = adapter as? BaseRecyclerViewAdapter<VM>
        ?: (object : BaseRecyclerViewAdapter<VM>(lifecycleOwner, getDiffUtil()) {
            override fun getItemLayoutId(position: Int) = layoutId
        }.also { adapter = it })

    adapter.submitList(itemList)
}

fun <VM : Any> RecyclerView.bindData(
    itemList: List<VM>?,
    layoutId: Int,
    lifecycleOwner: LifecycleOwner?
) {
    if (itemList == null) return

    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context)
    }

    @Suppress("UNCHECKED_CAST")
    val adapter = adapter as? BaseRecyclerViewAdapter<VM>
        ?: (object : BaseRecyclerViewAdapter<VM>(lifecycleOwner) {
            override fun getItemLayoutId(position: Int) = layoutId
        }.also { adapter = it })

    adapter.submitList(itemList)
}

fun <VM : DiffComparable<VM>> RecyclerView.bindDiffComparable(
    itemList: PagedList<VM>?,
    layoutId: Int,
    lifecycleOwner: LifecycleOwner? = null
) {
    if (itemList == null) return

    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context)
    }

    @Suppress("UNCHECKED_CAST")
    val adapter = adapter as? BasePagedListAdapter<VM>
        ?: (object : BasePagedListAdapter<VM>(lifecycleOwner, getDiffUtil()) {
            override fun getItemLayoutId(position: Int) = layoutId
        }.also { adapter = it })

    adapter.submitList(itemList)
}

fun <VM : Any> RecyclerView.bindData(
    itemList: PagedList<VM>?,
    layoutId: Int,
    lifecycleOwner: LifecycleOwner? = null
) {
    if (itemList == null) return

    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context)
    }

    @Suppress("UNCHECKED_CAST")
    val adapter = adapter as? BasePagedListAdapter<VM>
        ?: (object : BasePagedListAdapter<VM>(lifecycleOwner) {
            override fun getItemLayoutId(position: Int) = layoutId
        }.also { adapter = it })

    adapter.submitList(itemList)
}

private fun <VM : DiffComparable<VM>> getDiffUtil(): DiffUtil.ItemCallback<VM> =
    object : DiffUtil.ItemCallback<VM>() {
        override fun areItemsTheSame(oldItem: VM, newItem: VM): Boolean =
            oldItem.areItemsTheSame(newItem)

        override fun areContentsTheSame(oldItem: VM, newItem: VM): Boolean =
            oldItem.areContentsTheSame(newItem)
    }