package com.limor.app.dm.ui

import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Rect
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
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.common.BaseFragment
import com.limor.app.common.Constants
import com.limor.app.dm.LeanUser
import com.limor.app.dm.SessionsViewModel
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.showKeyboard
import com.limor.app.scenes.main.viewmodels.SharePodcastViewModel
import kotlinx.android.synthetic.main.fragment_share_dialog.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ShareFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val sharePodcastViewModel: SharePodcastViewModel by viewModels { viewModelFactory }
    private val chat: SessionsViewModel by viewModels { viewModelFactory }

    private var onShared: ((shareResult: ShareResult) -> Unit)? = null

    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    private var _binding: FragmentShareDialogBinding? = null
    private val binding get() = _binding!!
    private var mShortLink: String? = null
    private var shareableUsers = listOf<LeanUser>()

    private lateinit var quickShareAdapter: QuickShareAdapter
    private lateinit var fullShareAdapter: FullShareAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentShareDialogBinding.inflate(inflater, container, false);
        _binding = binding

        adjustUI()

        createAdapters()
        setViews()

        lifecycleScope.launch {
            generateLink()
        }

        return binding.root
    }

    private fun adjustUI() {
        activity?.let {
            val rect = Rect()
            it.window.decorView.getWindowVisibleDisplayFrame(rect)

            binding.root.measure(
                View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.UNSPECIFIED
            )

            if (binding.root.measuredHeight > rect.height() * 0.96) {
                // the UI height is bigger than what can be presented on the screen, will decrease
                // the UI paddings and/or margins

                binding.root.updatePadding(
                    top = (binding.root.paddingTop * 0.5).toInt()
                )

                listOf(
                    binding.textFakeSearch,
                    binding.delimiter,
                    binding.buttonCopyLink,
                    binding.buttonShareWithMessage,
                    binding.shareViaLabel
                ).map { adjustTopMargin(it, 0.5) }
            }
        }
    }

    private fun adjustTopMargin(view: View, ratio: Double) {
        view.updateLayoutParams<LinearLayout.LayoutParams> {
            topMargin = (topMargin * ratio).toInt()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModels()
    }

    fun setOnSharedListener(onSharedListener: ((shareResult: ShareResult) -> Unit)?) {
        onShared = onSharedListener
    }

    private fun createAdapters() {
        quickShareAdapter = QuickShareAdapter(
            context = requireContext(),
            allUsers = listOf(),
            onTap = { onUserTapped() },
            onMore = { showFullView(true) }
        )
        fullShareAdapter = FullShareAdapter(
            context = requireContext(),
            allUsers = mutableListOf(),
            onTap = { onUserTapped() }
        )
    }

    private fun onUserTapped() {
        val canShare = shareableUsers.isNotEmpty() && shareableUsers.any {
            it.selected
        }
        binding.buttonShareWithMessage.isEnabled = canShare
        binding.buttonShareSeparately.isEnabled = canShare

        quickShareAdapter.notifyDataSetChanged()
        fullShareAdapter.notifyDataSetChanged()
    }

    private fun generateLink(retryCount: Int = 1) {
        if (retryCount > 5) {
            // TODO?
            ShareDialog.DismissEvent.dismiss()
            return
        }

        val podcastLink = Constants.PODCAST_URL.format(cast.id)
        if (BuildConfig.DEBUG) {
            println("Getting dynamic link...")
        }
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

        if (BuildConfig.DEBUG) {
            println("Got dynamic link to $dynamicLink")
        }

        Firebase.dynamicLinks.shortLinkAsync {
            longLink = dynamicLink.uri
        }.addOnSuccessListener { (shortLink, _) ->
            if (BuildConfig.DEBUG) {
                print("converted dynamic link to short -> $shortLink")
            }
            setShortLink(shortLink.toString())

        }.addOnFailureListener {
            if (BuildConfig.DEBUG) {
                Timber.d("Failed in creating short dynamic link")
            }
            binding.root.postDelayed({
                if (BuildConfig.DEBUG) {
                    println("Retrying dynamic link...")
                }
                generateLink(retryCount + 1)
            }, linkGenerationRetryIntervalMillis)
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
                setSpan(
                    ImageSpan(it, ImageSpan.ALIGN_BOTTOM),
                    0,
                    1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }.also {
                binding.buttonCopyLink.text = it
            }
        }

        binding.textFakeSearch.setOnClickListener {
            showFullView(true)
            binding.layoutShareWithUsersFull.post {
                binding.editSearch.apply {
                    requestFocus()
                    showKeyboard()
                }
            }
        }

        binding.buttonBack.setOnClickListener {
            binding.editSearch.apply {
                clearFocus()
                hideKeyboard()
            }
            showFullView(false)
        }

        binding.recyclerQuickUsers.adapter = quickShareAdapter
        binding.recyclerFullUsers.adapter = fullShareAdapter

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                fullShareAdapter.filter(s.toString())
                val canClear = s.isNotEmpty()
                binding.searchIcon.visibility = if (canClear) View.GONE else View.VISIBLE
                binding.searchClear.visibility = if (canClear) View.VISIBLE else View.GONE
            }
        })

        binding.searchClear.setOnClickListener {
            binding.editSearch.setText("")
            fullShareAdapter.filter("")
        }

        binding.buttonShareWithMessage.setOnClickListener {
            shareSelected()
        }
        binding.buttonShareSeparately.setOnClickListener {
            shareSelected()
        }
    }

    private fun shareSelected() {
        val selected = shareableUsers.filter { it.selected }
        lifecycleScope.launch {
            val start = System.currentTimeMillis()
            val shared = chat.shareAsDirectMessage(selected, mShortLink)
            val end = System.currentTimeMillis()
            if (BuildConfig.DEBUG) {
                println("Shared successfully -> $shared for ${end - start}ms")
            }
            markAsShared(selected.size)
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
                height =
                    if (show) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
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
            val cm =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            ClipData.newPlainText("Limor", shortLink).also {
                cm.setPrimaryClip(it)
                markAsShared(1)
            }
        }
    }

    private fun markAsShared(newSharesCount: Int) {
        onShared?.invoke(
            ShareResult(
                hasShared = true,
                newSharesCount = newSharesCount,
                shareUrl = mShortLink
            )
        )
        sharePodcastViewModel.share(castId = cast.id, shareCount = newSharesCount)
    }

    private fun subscribeToViewModels() {
        sharePodcastViewModel.sharedResponse.observe(viewLifecycleOwner) {
            if (BuildConfig.DEBUG) {
                println("Agora Shared response -> $it")
            }
            ShareDialog.DismissEvent.dismiss()
        }
        chat.sessions.observe(viewLifecycleOwner) {
            if (BuildConfig.DEBUG) {
                println("Got sessions -> $it")
            }
            setUsers(it.map(LeanUser::fromSession))
        }
    }

    private fun setUsers(leanUsers: List<LeanUser>) {
        shareableUsers = leanUsers

        quickShareAdapter.setAllLeanUsers(leanUsers)
        fullShareAdapter.setAllLeanUsers(leanUsers)
    }

    private fun getShareIntent(shortLink: String, useType: Boolean): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, cast.title)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message__with_format, shortLink))
            if (useType) {
                type = "text/plain"
            }
        }
    }

    private fun getActivities(sendIntent: Intent, matchAll: Boolean): List<ResolveInfo> {
        val pm = requireContext().packageManager
        return pm.queryIntentActivities(sendIntent, if (matchAll) PackageManager.MATCH_ALL else 0).sortedBy {
            it.activityInfo.name
        }
    }

    private fun setExternalShare(shortLink: String) {
        val a = getActivities(getShareIntent(shortLink, true), true)
        val b = getActivities(getShareIntent(shortLink, true), false)

        val apps = listOf(a, b,).first { a.isNotEmpty() }

        // binding.shareViaLabel.text = "a ${a.size}, b ${b.size}, c ${c.size}, d ${d.size}"
        binding.recyclerExternal.adapter = AppsAdapter(
            requireContext(),
            apps,
            onTap = { ri ->
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
            putExtra(
                Intent.EXTRA_TEXT,
                context?.getString(R.string.share_message__with_format, shortLink)
            )
        }

        context?.startActivity(sendIntent)
        markAsShared(1)
    }

    companion object {
        val TAG = ShareFragment::class.qualifiedName
        private const val KEY_PODCAST = "KEY_PODCAST"
        private const val linkGenerationRetryIntervalMillis = 500L

        fun newInstance(cast: CastUIModel): ShareFragment {
            return ShareFragment().apply {
                arguments = bundleOf(KEY_PODCAST to cast)
            }
        }
    }

}