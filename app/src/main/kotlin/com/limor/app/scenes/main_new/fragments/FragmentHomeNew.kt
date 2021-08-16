package com.limor.app.scenes.main_new.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_home_new.*
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject

class FragmentHomeNew : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val homeFeedViewModel: HomeFeedViewModel by viewModels { viewModelFactory }
    private val likePodcastViewModel: LikePodcastViewModel by viewModels { viewModelFactory }
    private val recastPodcastViewModel: RecastPodcastViewModel by viewModels { viewModelFactory }
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }
    private val podcastInteractionViewModel: PodcastInteractionViewModel by activityViewModels { viewModelFactory }

    lateinit var binding: FragmentHomeNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSwipeToRefresh()
        subscribeToViewModel()
        setOnClicks()
    }

    private fun setOnClicks() {
        binding.btnNotification.setOnClickListener {
           findNavController().navigate(R.id.navigation_notifications)
        }
    }

    private fun initSwipeToRefresh() {
        binding.swipeToRefresh.setColorSchemeResources(R.color.colorAccent)
        binding.swipeToRefresh.setOnRefreshListener {
            homeFeedViewModel.loadHomeFeed()
        }
    }

    private fun subscribeToViewModel() {
        homeFeedViewModel.homeFeedData.observe(viewLifecycleOwner) { casts ->
            binding.swipeToRefresh.isRefreshing = false
            setDataToRecyclerView(casts)
        }
        likePodcastViewModel.reload.observe(viewLifecycleOwner){
            homeFeedViewModel.loadHomeFeed()
        }
        recastPodcastViewModel.recastedResponse.observe(viewLifecycleOwner){
            homeFeedViewModel.loadHomeFeed()
        }
        recastPodcastViewModel.deleteRecastResponse.observe(viewLifecycleOwner){
            homeFeedViewModel.loadHomeFeed()
        }
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner){
            homeFeedViewModel.loadHomeFeed()
        }
        podcastInteractionViewModel.reload.observe(viewLifecycleOwner){
            if(it == true){
                homeFeedViewModel.loadHomeFeed()
            }
        }
    }

    private fun setDataToRecyclerView(list: List<CastUIModel>) {
        val adapter = binding.rvHome.adapter
        if (adapter != null) {
            (adapter as HomeFeedAdapter).submitList(list)
        } else {
            setUpRecyclerView(list)
        }
    }

    private fun setUpRecyclerView(list: List<CastUIModel>) {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.itemAnimator = null

        binding.rvHome.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.rvHome.addItemDecoration(MarginItemDecoration(itemMargin))
        val adapter = HomeFeedAdapter(
            onLikeClick = { castId, like ->
                likePodcastViewModel.likeCast(castId, like)
            },
            onCastClick = { cast ->
                openPlayer(cast)
            },
            onReCastClick = { castId, isRecasted ->
                if(isRecasted){
                    recastPodcastViewModel.reCast(castId)
                } else{
                    recastPodcastViewModel.deleteRecast(castId)
                }
            },
            onCommentsClick = {cast ->
                RootCommentsFragment.newInstance(cast).also { fragment ->
                    fragment.show(parentFragmentManager, fragment.requireTag())
                }
            },
            onShareClick = { cast ->
                sharePodcast(cast)
            },
            onReloadData = {
                    _, _ ->
                homeFeedViewModel.loadHomeFeed()
            }
        ).apply { submitList(list) }
        rvHome.adapter = adapter
    }

    var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK){
            val intent = result.data
            val podcastId = intent?.getIntExtra(Constants.SHARED_PODCAST_ID, -1) ?: -1
            if(podcastId != -1) {
                sharePodcastViewModel.share(podcastId)
            }
        }
    }

    val sharePodcast : (CastUIModel) -> Unit =  { cast ->

        val podcastLink = Constants.PODCAST_URL.format(cast.id)

        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(podcastLink)
            domainUriPrefix = Constants.LIMOR_DOMAIN_URL
            androidParameters(BuildConfig.APPLICATION_ID) {
                fallbackUrl = Uri.parse(podcastLink)
            }
            iosParameters(BuildConfig.IOS_BUNDLE_ID) {
            }
            socialMetaTagParameters {
                title = cast.title.toString()
                description = cast.caption.toString()
                cast.imageLinks?.large?.let {
                    imageUrl = Uri.parse(cast.imageLinks.large)
                }
            }
        }

        Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { (shortLink, flowChartLink) ->
            try{
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, cast.title)
                    putExtra(Intent.EXTRA_TEXT, "Hey, check out this podcast: $shortLink")
                    putExtra(Constants.SHARED_PODCAST_ID, cast.id)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                launcher.launch(shareIntent)
            } catch (e: ActivityNotFoundException){}

        }.addOnFailureListener {
            Timber.d("Failed in creating short dynamic link")
        }

    }

    private fun openPlayer(cast: CastUIModel) {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                cast.id
            )
        )
    }

}