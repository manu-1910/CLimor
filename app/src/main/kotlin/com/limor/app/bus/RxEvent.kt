package com.limor.app.bus

import com.limor.app.uimodels.UIPodcast

// Usage - RxBus.publish(RxEvent.PodcastLiked(uiPodcast))

// Listen in viewModel (perhaps add this to an init{} block) - compositeDispose.add(RxBus.listen(RxEvent.PodcastLiked::class.java).subscribe {  })

class RxEvent {
    data class PodcastLiked(val podcast: UIPodcast)
}