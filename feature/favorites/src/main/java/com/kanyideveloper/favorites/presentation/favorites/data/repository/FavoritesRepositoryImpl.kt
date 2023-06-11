/*
 * Copyright 2022 Joel Kanyi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kanyideveloper.favorites.presentation.favorites.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.joelkanyi.shared.data.network.utils.Resource
import com.kanyideveloper.core.domain.FavoritesRepository
import com.kanyideveloper.core.model.Favorite
import com.kanyideveloper.core_database.dao.FavoritesDao
import com.kanyideveloper.core_database.model.FavoriteEntity
import com.kanyideveloper.favorites.presentation.favorites.data.mapper.toEntity
import com.kanyideveloper.favorites.presentation.favorites.data.mapper.toFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.UUID

class FavoritesRepositoryImpl(
    private val favoritesDao: FavoritesDao,
    private val databaseReference: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) : FavoritesRepository {
    override suspend fun insertFavorite(
        isSubscribed: Boolean,
        favorite: Favorite
    ): Resource<Boolean> {
        return if (isSubscribed) {
            saveFavoriteToRemoteDatasource(favorite)
        } else {
            favoritesDao.insertAFavorite(favorite.toEntity())
            Resource.Success(data = true)
        }
    }

    override suspend fun getFavorites(isSubscribed: Boolean): Resource<Flow<List<Favorite>>> {
        return if (isSubscribed) {
            getFavoritesFromRemoteDataSource()
        } else {
            Resource.Success(
                data = favoritesDao.getFavorites().map { favoritesEntity ->
                    favoritesEntity.map { it.toFavorite() }
                }
            )
        }
    }

    private suspend fun getFavoritesFromRemoteDataSource(): Resource<Flow<List<Favorite>>> {
        /**
         * Do offline caching
         */
        // first read from the local database
        val favorites = favoritesDao.getFavorites()

        return try {
            val newFavorites = withTimeoutOrNull(10000L) {
                // fetch from the remote database
                val favoritesRemote: MutableList<Favorite> = mutableListOf()
                val favs = databaseReference
                    .child("favorites")
                    .child(firebaseAuth.currentUser?.uid.toString())
                val auctionsListFromDb = favs.get().await()
                for (i in auctionsListFromDb.children) {
                    val result = i.getValue(Favorite::class.java)
                    favoritesRemote.add(result!!)
                }

                // clear the local database
                favoritesDao.deleteAllFavorites()

                // save the remote data to the local database
                favoritesRemote.forEach { onlineFavorite ->
                    favoritesDao.insertAFavorite(
                        FavoriteEntity(
                            id = onlineFavorite.id,
                            onlineMealId = onlineFavorite.onlineMealId,
                            localMealId = onlineFavorite.localMealId,
                            isOnline = onlineFavorite.online,
                            mealName = onlineFavorite.mealName,
                            mealImageUrl = onlineFavorite.mealImageUrl,
                            isFavorite = onlineFavorite.favorite
                        )
                    )
                }

                // read from the local database
                favoritesDao.getFavorites().map { favoriteEntities ->
                    favoriteEntities.map { favoriteEntity ->
                        favoriteEntity.toFavorite()
                    }
                }
            }

            if (newFavorites == null) {
                Resource.Error(
                    "Viewing offline data",
                    data = favorites.map {
                        it.map { favoriteEntity ->
                            favoriteEntity.toFavorite()
                        }
                    }
                )
            } else {
                Resource.Success(data = newFavorites)
            }
        } catch (e: Exception) {
            Resource.Error(
                e.localizedMessage ?: "Unknown error occurred",
                data = favorites.map {
                    it.map { favoriteEntity ->
                        favoriteEntity.toFavorite()
                    }
                }
            )
        }
    }

    override fun getASingleFavorite(id: Int): LiveData<Favorite?> {
        return favoritesDao.getAFavoriteById(id = id).map {
            it?.toFavorite()
        }
    }

    override fun isLocalFavorite(id: String): LiveData<Boolean> {
        return favoritesDao.localInFavorites(id = id)
    }

    override fun isOnlineFavorite(id: String): LiveData<Boolean> {
        return favoritesDao.onlineInFavorites(id = id)
    }

    override suspend fun deleteOneFavorite(favorite: Favorite, isSubscribed: Boolean) {
        if (isSubscribed) {
            deleteAFavoriteFromRemoteDatasource(
                mealId = if (favorite.online) {
                    favorite.onlineMealId ?: UUID.randomUUID().toString()
                } else {
                    favorite.localMealId.toString()
                },
                isOnlineMeal = favorite.online
            )
        } else {
            favoritesDao.deleteAFavorite(favorite.toEntity())
        }
    }

    override suspend fun deleteAllFavorites() {
        favoritesDao.deleteAllFavorites()
    }

    override suspend fun deleteALocalFavorite(localMealId: String, isSubscribed: Boolean) {
        if (isSubscribed) {
            deleteAFavoriteFromRemoteDatasource(
                mealId = localMealId,
                isOnlineMeal = false
            )
        } else {
            favoritesDao.deleteALocalFavorite(localMealId = localMealId)
        }
    }

    override suspend fun deleteAnOnlineFavorite(onlineMealId: String, isSubscribed: Boolean) {
        if (isSubscribed) {
            deleteAFavoriteFromRemoteDatasource(
                mealId = onlineMealId,
                isOnlineMeal = true
            )
        } else {
            favoritesDao.deleteAnOnlineFavorite(mealId = onlineMealId)
        }
    }

    private suspend fun saveFavoriteToRemoteDatasource(favorite: Favorite): Resource<Boolean> {
        return try {
            databaseReference
                .child("favorites")
                .child(firebaseAuth.currentUser?.uid.toString())
                .child(
                    if (favorite.online) {
                        favorite.onlineMealId ?: UUID.randomUUID().toString()
                    } else {
                        favorite.localMealId.toString()
                    }
                )
                .setValue(favorite).await()
            favoritesDao.insertAFavorite(favorite.toEntity())
            Resource.Success(data = true)
        } catch (e: Exception) {
            return Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    private suspend fun deleteAFavoriteFromRemoteDatasource(
        mealId: String,
        isOnlineMeal: Boolean
    ): Resource<Boolean> {
        return try {
            databaseReference
                .child("favorites")
                .child(firebaseAuth.currentUser?.uid.toString())
                .child(mealId)
                .removeValue()
                .await()

            if (isOnlineMeal) {
                favoritesDao.deleteAnOnlineFavorite(mealId = mealId)
            } else {
                favoritesDao.deleteALocalFavorite(localMealId = mealId)
            }

            Resource.Success(data = true)
        } catch (e: Exception) {
            Timber.e("Error deleting isFavorite: $e")
            return Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}
