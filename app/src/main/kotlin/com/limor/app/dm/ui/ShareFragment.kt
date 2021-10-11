package com.limor.app.dm.ui

import android.content.*
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
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.limor.app.BuildConfig
import com.limor.app.common.Constants
import timber.log.Timber

class ShareFragment : Fragment() {

    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    private var _binding: FragmentShareDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareDialogBinding.inflate(inflater, container, false)
        setViews()
        generateLink()
        return binding.root
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
    }

    private fun setShortLink(shortLink: String) {
        binding.shareLink.text = shortLink
        setExternalShare(shortLink)

        binding.buttonCopyLink.setOnClickListener {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            ClipData.newPlainText("Limor", shortLink).also {
                cm.setPrimaryClip(it)
            }
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
        binding.recyclerExternal.adapter = AppsAdapter(requireContext(), apps)
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