package io.square1.limor.di.modules

import dagger.Binds
import dagger.Module
import repositories.drafts.DataDraftsRepository
import repositories.drafts.DraftsRepository
import repositories.auth.AuthRepository
import repositories.auth.DataAuthRepository
import repositories.comment.CommentRepository
import repositories.comment.DataCommentRepository
import repositories.podcast.DataPodcastRepository
import repositories.podcast.PodcastRepository
import repositories.search.DataSearchRepository
import repositories.search.SearchRepository
import repositories.user.DataUserRepository
import repositories.user.UserRepository


@Module
abstract class DataModule {
    /**
     * This companion object annotated as a module is necessary in order to provide dependencies
     * statically in case the wrapping module is an abstract class (to use binding)
     */
    @Module
    companion object {
        /**
        @Provides
        @JvmStatic
        fun provideSomething(): Something {
        return InstanceOfSomething
        }*/
    }

    @Binds
    abstract fun bindAuthRepository(authRepository: DataAuthRepository): AuthRepository

    @Binds
    abstract fun bindDraftsRepository(draftsRepository: DataDraftsRepository): DraftsRepository

    @Binds
    abstract fun bindUserRepository(userRepository: DataUserRepository): UserRepository

    @Binds
    abstract fun bindPodcastRepository(podcastRepository: DataPodcastRepository): PodcastRepository

    @Binds
    abstract fun bindSearchRepository(searchRepository: DataSearchRepository): SearchRepository

    @Binds
    abstract fun bindCommentRepository(commentRepository: DataCommentRepository): CommentRepository


}