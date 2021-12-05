package com.limor.app.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.limor.app.R

/**
 * @see [RequestListener]
 */
fun <T> RequestBuilder<T>.addListener(
    onReady: (
        resource: T,
        model: Any?,
        target: Target<T>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ) -> Boolean,
    onError: (
        error: GlideException?,
        model: Any?,
        target: Target<T>?,
        isFirstResource: Boolean
    ) -> Boolean = { _, _, _, _ -> false }
) = addListener(
    object : RequestListener<T> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<T>?,
            isFirstResource: Boolean
        ): Boolean {
            return onError(e, model, target, isFirstResource)
        }

        override fun onResourceReady(
            resource: T,
            model: Any?,
            target: Target<T>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            return onReady(resource, model, target, dataSource, isFirstResource)
        }
    }
)

/**
 * @see [RequestListener]
 */
fun <T> RequestBuilder<T>.listener(
    onReady: (
        resource: T,
        model: Any?,
        target: Target<T>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ) -> Boolean,
    onError: (
        error: GlideException?,
        model: Any?,
        target: Target<T>?,
        isFirstResource: Boolean
    ) -> Boolean = { _, _, _, _ -> false }
) = listener(
    object : RequestListener<T> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<T>?,
            isFirstResource: Boolean
        ): Boolean {
            return onError(e, model, target, isFirstResource)
        }

        override fun onResourceReady(
            resource: T,
            model: Any?,
            target: Target<T>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            return onReady(resource, model, target, dataSource, isFirstResource)
        }
    }
)

fun <T : ImageView> T.loadCircleImage(url: String?) {
    if (url == null) {
        setImageResource(R.drawable.ic_podcast_listening)
        return
    }
    loadCircleImage(url, true);
}

fun <T : ImageView> T.loadCircleImage(url: String, usePlaceholder: Boolean) {
    var options = Glide.with(this)
        .load(url)
        .signature(ObjectKey(url))

    if (usePlaceholder) {
        options = options.error(R.drawable.ic_podcast_listening)
            .placeholder(R.drawable.ic_podcast_listening)
    }

    options.circleCrop()
        .into(this)
}

fun ImageView.loadImage(url: String) {
    Glide.with(this)
        .load(url)
        .signature(ObjectKey(url))
        .into(this)
}