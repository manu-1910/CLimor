package com.limor.app.dm.ui

import android.content.*
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.limor.app.R
import com.limor.app.databinding.FragmentShareDialogBinding
import com.limor.app.uimodels.CastUIModel
import android.net.Uri
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.scenes.main.viewmodels.LikePodcastViewModel
import com.limor.app.scenes.main.viewmodels.RecastPodcastViewModel
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ShareFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }

    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    private var _binding: FragmentShareDialogBinding? = null
    private val binding get() = _binding!!
    private var mShortLink: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareDialogBinding.inflate(inflater, container, false)
        setViews()

        lifecycleScope.launch {
            generateLink()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModels()
    }

    private fun generateLink() {
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
                    imageUrl = Uri.parse(it)
                }
            }
        }

        Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { (shortLink, flowChartLink) ->
            setShortLink(shortLink.toString())

        }.addOnFailureListener {
            Timber.d("Failed in creating short dynamic link")
        }
    }

    private fun setViews() {
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_dm_message_copy_link)?.let {
            // 20 = original image width
            // 16 = original image height
            it.setBounds(
                0,
                0,
                (binding.buttonCopyLink.lineHeight * (20.0 / 16.0)).toInt(),
                binding.buttonCopyLink.lineHeight
            )

            SpannableStringBuilder(binding.buttonCopyLink.text).apply {
                insert(0, "- ")
                setSpan(ImageSpan(it, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }.also {
                binding.buttonCopyLink.text = it
            }
        }

        binding.textFakeSearch.setOnClickListener {
            showFullView(true)
        }

        binding.buttonBack.setOnClickListener {
            showFullView(false)
        }
    }

    private fun showFullView(show: Boolean) {
        binding.let {
            arrayOf(
                it.shareTitle,
                it.shareLink,
                it.buttonCopyLink,
                it.delimiter,
                it.layoutShareWithUsers,
                it.shareViaLabel,
                it.recyclerExternal
            ).forEach { view ->
                view.visibility = if (show) View.GONE else View.VISIBLE
            }

            it.layoutShareWithUsersFull.visibility = if (show) View.VISIBLE else View.GONE

            it.root.updateLayoutParams {
                height = if (show) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        (parentFragment as? ShareDialog)?.adjustHeight(show)
    }

    private fun setShortLink(shortLink: String) {
        mShortLink = shortLink
        binding.shareLink.text = shortLink

        lifecycleScope.launch {
            setExternalShare(shortLink)
        }

        binding.buttonCopyLink.isEnabled = true
        binding.buttonCopyLink.setOnClickListener {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            ClipData.newPlainText("Limor", shortLink).also {
                cm.setPrimaryClip(it)
                markAsShared()
            }
        }
    }

    private fun markAsShared() {
        sharePodcastViewModel.share(castId = cast.id)
    }

    private fun subscribeToViewModels() {
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner){
            ShareDialog.DismissEvent.dismiss()
        }
    }

    private fun setExternalShare(shortLink: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, cast.title)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message__with_format, shortLink))
            type = "text/plain"
        }

        val pm = requireContext().packageManager
        val apps = pm.queryIntentActivities(sendIntent, 0).sortedBy {
            it.activityInfo.name
        }
        binding.recyclerExternal.adapter = AppsAdapter(
            requireContext(),
            apps,
            onTap = {  ri ->
                openShareIntent(ri)
            }
        )
    }

    private fun openShareIntent(ri: ResolveInfo) {
        val shortLink = mShortLink ?: return

        val ai = ri.activityInfo
        val name = ComponentName(ai.applicationInfo.packageName, ai.name)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            component = name

            putExtra(Intent.EXTRA_SUBJECT, cast.title)
            putExtra(Intent.EXTRA_TEXT, context?.getString(R.string.share_message__with_format, shortLink))
        }

        context?.startActivity(sendIntent)
        markAsShared()
    }

    companion object {
        val TAG = ShareFragment::class.qualifiedName
        private const val KEY_PODCAST = "KEY_PODCAST"
        fun newInstance(cast: CastUIModel): ShareFragment {
            return ShareFragment().apply {
                arguments = bundleOf(KEY_PODCAST to cast)
            }
        }
    }
}