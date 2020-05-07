package repositories

import entities.response.DraftEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import providers.storage.StorageDraftProvider
import javax.inject.Inject

class DataDraftsRepository @Inject constructor(
    //private val remoteProvider: RemoteDraftsProvider,
    private val storageProvider: StorageDraftProvider
) : DraftsRepository {

    ////*****************
    ////REMOTE
    ////*****************
    //override fun centres(): Single<CentresResponseEntity> {
    //    return remoteProvider.centres()
    //}
//
    //override fun createCentre(centre: CentreEntity): Single<CreateCentreResponseEntity> {
    //    return remoteProvider.createCentre(centre)
    //}
//
    //override fun centreTypes(): Single<CentreTypesResponseEntity> {
    //    return remoteProvider.centreTypes()
    //}
//
    //override fun deleteCentre(centreId: Int): Single<DeleteCentreResponseEntity> {
    //    return remoteProvider.deleteCentre(centreId)
    //}
//
    //override fun updateCentre(centre: CentreEntity): Single<CreateCentreResponseEntity> {
    //    return remoteProvider.updateCentre(centre)
    //}


    //*****************
    //STORAGE
    //*****************
    override fun insertRealmDraft(draftEntity: DraftEntity): Completable {
        return storageProvider.insertRealmDraft(draftEntity)
    }

    override fun loadRealmDrafts(): Flowable<List<DraftEntity>> {
        return storageProvider.loadRealmDrafts()
    }

    override fun deleteRealmDraft(draftEntity: DraftEntity): Completable {
        return storageProvider.deleteRealmDraft(draftEntity)
    }
}