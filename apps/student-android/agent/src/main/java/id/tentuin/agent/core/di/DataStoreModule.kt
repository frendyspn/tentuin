package id.tentuin.agent.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.tentuin.agent.core.datastore.SessionDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideSessionDataStore(@ApplicationContext ctx: Context): SessionDataStore =
        SessionDataStore(ctx)
}
