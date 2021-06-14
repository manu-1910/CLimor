package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.limor.app.R
import com.limor.app.scenes.main_new.fragments.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import io.github.hyuwah.draggableviewlib.DraggableListener
import io.github.hyuwah.draggableviewlib.DraggableView
import kotlinx.android.synthetic.main.fragment_home_new.*
import timber.log.Timber

class FragmentHomeNew : Fragment(), DraggableListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setUpDraggableView()
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        rvHome.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        rvHome.addItemDecoration(MarginItemDecoration(itemMargin))
        rvHome.adapter = HomeFeedAdapter((1..50).map { it.toString() }.toList())
    }

    private lateinit var someDraggableView: DraggableView<LinearLayout> // can be other View or ViewGroup

    private fun setUpDraggableView() {
        someDraggableView = DraggableView.Builder(draggableView)
            .setStickyMode(DraggableView.Mode.STICKY_X)
            .setListener(this)
            .build()
    }

    override fun onPositionChanged(view: View) {
        Timber.d("X: ${view.x}, Y: ${view.y}")
    }
}