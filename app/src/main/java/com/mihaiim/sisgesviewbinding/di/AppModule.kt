package com.mihaiim.sisgesviewbinding.di

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.mihaiim.sisgesviewbinding.R
import com.mihaiim.sisgesviewbinding.data.repository.*
import com.mihaiim.sisgesviewbinding.domain.repository.*
import com.mihaiim.sisgesviewbinding.others.Constants.SHARED_PREFERENCES_NAME
import com.mihaiim.sisgesviewbinding.others.UriTypeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseFirestore() = Firebase.firestore

    @Singleton
    @Provides
    @Named("administration")
    fun provideAdministrationCollection(
        firestore: FirebaseFirestore,
    ) = firestore.collection("administration")

    @Singleton
    @Provides
    @Named("products")
    fun provideProductsCollection(
        firestore: FirebaseFirestore,
    ) = firestore.collection("products")

    @Singleton
    @Provides
    @Named("storage")
    fun provideStorageCollection(
        firestore: FirebaseFirestore,
    ) = firestore.collection("storage")

    @Singleton
    @Provides
    @Named("storage_positions")
    fun provideStoragePositionsCollection(
        firestore: FirebaseFirestore,
    ) = firestore.collection("storage_positions")

    @Singleton
    @Provides
    @Named("users")
    fun provideUsersCollection(
        firestore: FirebaseFirestore,
    ) = firestore.collection("users")

    @Singleton
    @Provides
    fun provideAdministrationRepository(
        @Named("administration") collectionRef: CollectionReference,
        storageRepository: StorageRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AdministrationRepository = AdministrationRepositoryImpl(
        collectionRef,
        storageRepository,
        ioDispatcher,
    )

    @Singleton
    @Provides
    fun provideProductRepository(
        @Named("products") collectionRef: CollectionReference,
        storageRepository: StorageRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): ProductRepository = ProductRepositoryImpl(collectionRef, storageRepository, ioDispatcher)

    @Singleton
    @Provides
    fun provideStorageRepository(
        @Named("storage") collectionRef: CollectionReference,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): StorageRepository = StorageRepositoryImpl(collectionRef, ioDispatcher)

    @Singleton
    @Provides
    fun provideStoragePositionRepository(
        @Named("storage_positions") collectionRef: CollectionReference,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): StoragePositionRepository = StoragePositionRepositoryImpl(collectionRef, ioDispatcher)

    @Singleton
    @Provides
    fun provideUserRepository(
        auth: FirebaseAuth,
        @Named("users") collectionRef: CollectionReference,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): UserRepository = UserRepositoryImpl(auth, collectionRef, ioDispatcher)

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context,
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.ic_default_profile_picture)
            .error(R.drawable.ic_default_profile_picture)
    )

    @Singleton
    @Provides
    fun provideGsonInstance() =
        GsonBuilder().registerTypeAdapter(Uri::class.java, UriTypeAdapter()).create()

    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context,
    ) = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
}