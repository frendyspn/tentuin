package id.tentuin.student.core.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.repository.TestRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EntryPointInterface {
    fun testRepository(): TestRepository
    fun sessionDataStore(): SessionDataStore
}
