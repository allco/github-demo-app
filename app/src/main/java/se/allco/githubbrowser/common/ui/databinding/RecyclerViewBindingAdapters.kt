package se.allco.githubbrowser.common.ui.databinding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import se.allco.githubbrowser.common.ui.recyclerview.DataBoundAdapter
import se.allco.githubbrowser.common.ui.recyclerview.DividerItemDecoration
import se.allco.githubbrowser.common.utils.dpToPx

@BindingAdapter("listItems")
fun setRecyclerViewListItems(recyclerView: RecyclerView, listItems: List<DataBoundAdapter.Item>?) {
    listItems?.let {
        when (val adapter = recyclerView.adapter) {
            null -> recyclerView.adapter = DataBoundAdapter(listItems)
            is DataBoundAdapter -> adapter.updateItems(listItems)
            else -> throw IllegalStateException("when the `:listItems` attribute is used no Adapters should be added to the RecyclerView")
        }
    }
}

@BindingAdapter("dividerSize")
fun setRecyclerViewSpacing(recyclerView: RecyclerView, dividerSizePx: Int?) {
    val size = dividerSizePx ?: return
    recyclerView.addItemDecoration(DividerItemDecoration(size))
}

@BindingAdapter("dividerSizeDp")
fun setRecyclerViewSpacingDp(recyclerView: RecyclerView, dividerSizeDp: Int?) {
    val sizeDp = dividerSizeDp ?: return
    setRecyclerViewSpacing(recyclerView, recyclerView.context.dpToPx(sizeDp.toFloat()))
}
