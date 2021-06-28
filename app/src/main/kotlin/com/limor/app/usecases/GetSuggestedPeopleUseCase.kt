package com.limor.app.usecases

import com.limor.app.SuggestedPeopleQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.uimodels.SuggestedPersonUIModel
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetSuggestedPeopleUseCase @Inject constructor(
    private val repository: GeneralInfoRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): Result<List<SuggestedPersonUIModel>> {
        return runCatching {
            withContext(dispatcherProvider.io) {
                repository.getSuggestedPeople()?.mapNotNull {
                    it.mapToUIModel()
                } ?: emptyList()
            }
        }
    }

    private fun SuggestedPeopleQuery.GetSuggestedUser.mapToUIModel(): SuggestedPersonUIModel? {
        require(
            username != null && last_name != null && first_name != null &&
                    images != null && blocked != null && followed != null && blocked_by != null &&
                    followed_by != null && following_count != null && followers_count != null &&
                    description != null && website != null && gender != null &&
                    date_of_birth != null && notifications_enabled != null && active != null &&
                    suspended != null && verified != null && autoplay_enabled != null &&
                    sharing_url != null
        ) {
            "Required argument is null, probably backend schema changed"
        }
        return SuggestedPersonUIModel(
            id, username, first_name, last_name, images.mapToUIModel(), blocked, followed,
            blocked_by, followed_by, following_count, followers_count, description, website, gender,
            date_of_birth.toLong().toLocalDate(), notifications_enabled, active, suspended,
            verified, autoplay_enabled, sharing_url
        )
    }

    private fun SuggestedPeopleQuery.Images.mapToUIModel(): SuggestedPersonUIModel.ImageLinks {
        require(
            small_url != null && medium_url != null &&
                    large_url != null && original_url != null
        ) {
            "Required argument is null, probably backend schema changed"
        }

        return SuggestedPersonUIModel.ImageLinks(small_url, medium_url, large_url, original_url)
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}