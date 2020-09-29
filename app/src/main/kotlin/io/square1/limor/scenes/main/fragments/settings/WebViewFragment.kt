package io.square1.limor.scenes.main.fragments.settings


import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_webview.*


class WebViewFragment : BaseFragment() {


    companion object {
        val TAG: String = WebViewFragment::class.java.simpleName
        fun newInstance() = WebViewFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_webview, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        configureWebView()

        //Load the url
        wvMore?.loadUrl(arguments?.getString(getString(R.string.webViewKey)))

    }


    private fun configureWebView(){
        // Get the web view settings instance
        val settings = wvMore?.settings

        // Enable java script in web view
        settings?.javaScriptEnabled = true

        // Enable and setup web view cache
        settings?.setAppCacheEnabled(true)
        settings?.cacheMode = WebSettings.LOAD_DEFAULT
        settings?.setAppCachePath(context?.cacheDir?.path)

        // Enable zooming in web view
        settings?.setSupportZoom(true)
        settings?.builtInZoomControls = true
        settings?.displayZoomControls = true

        // Zoom web view text
        settings?.textZoom = 125

        // Enable disable images in web view
        settings?.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings?.loadsImagesAutomatically = true


        // More web view settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings?.safeBrowsingEnabled = true  // api 26
        }
        //settings.pluginState = WebSettings.PluginState.ON
        settings?.useWideViewPort = true
        settings?.loadWithOverviewMode = true
        settings?.javaScriptCanOpenWindowsAutomatically = true
        settings?.mediaPlaybackRequiresUserGesture = false


        // More optional settings, you can enable it by yourself
        settings?.domStorageEnabled = true
        settings?.setSupportMultipleWindows(true)
        settings?.loadWithOverviewMode = true
        settings?.allowContentAccess = true
        settings?.setGeolocationEnabled(true)
        settings?.allowUniversalAccessFromFileURLs = true
        settings?.allowFileAccess = true

        // WebView settings
        wvMore?.fitsSystemWindows = true


        /*
            if SDK version is greater of 19 then activate hardware acceleration
            otherwise activate software acceleration
        */
        wvMore?.setLayerType(View.LAYER_TYPE_HARDWARE, null)


        // Set web view client
        wvMore?.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            }

            override fun onPageFinished(view: WebView, url: String) {
            }
        }


        // Set web view chrome client
        wvMore?.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView, newProgress: Int) {
            }
        }

    }


    override fun onResume() {
        super.onResume()
        view?.let {
            view!!.isFocusableInTouchMode = true
            view!!.requestFocus()
            view!!.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (wvMore.canGoBack()) {
                        // If web view have back history, then go to the web view back history
                        wvMore?.goBack()
                    } else {
                        // Ask the user to exit the app or stay in here
                        findNavController().popBackStack()
                    }
                    return@OnKeyListener true
                }
                false
            })
        }
    }

}
