package id.tentuin.student.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.tentuin.student.core.datastore.PrefsDataStore
import id.tentuin.student.core.datastore.SessionDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context): SessionDataStore =
        SessionDataStore(context)

    @Provides
    @Singleton
    fun providePrefsDataStore(@ApplicationContext context: Context): PrefsDataStore =
        PrefsDataStore(context)
}
