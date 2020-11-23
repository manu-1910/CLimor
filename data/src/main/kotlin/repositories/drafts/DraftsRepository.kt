package repositories.drafts

import entities.response.DraftEntity
import io.reactivex.Completable
import io.reactivex.Flowable

interface DraftsRepository {
    //fun centres(): Single<DraftResponseEntity>
    //fun createCentre(centre: CentreEntity): Single<CreateCentreResponseEntity>
    //fun centreTypes(): Single<CentreTypesResponseEntity>
    //fun deleteCentre(centreId : Int): Single<DeleteCentreResponseEntity>
    //fun updateCentre(centre: CentreEntity): Single<CreateCentreResponseEntity>

    fun insertRealmDraft(draftEntity: DraftEntity): Completable
    fun loadRealmDrafts(): Flowable<List<DraftEntity>>
    fun deleteRealmDraft(draftEntity: DraftEntity): Completable
    fun findByPrimaryKey(id : Int): Flowable<DraftEntity>
}