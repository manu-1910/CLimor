package com.limor.app.extensions

import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

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