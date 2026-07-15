package com.example.freshcontroll.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.freshcontroll.data.local.dao.AuditDao
import com.example.freshcontroll.data.local.dao.CashDao
import com.example.freshcontroll.data.local.dao.ProductDao
import com.example.freshcontroll.data.local.dao.SaleDao
import com.example.freshcontroll.data.local.dao.StockMovementDao
import com.example.freshcontroll.data.local.dao.StoreDao
import com.example.freshcontroll.data.local.dao.UserDao
import com.example.freshcontroll.data.local.database.FreshControlDatabase
import javax.inject.Singleton

/**
 * Módulo de Hilt encargado de proveer las dependencias relacionadas con la base de datos local (Room).
 * Garantiza que toda la aplicación comparta la misma instancia de la base de datos y sus DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FreshControlDatabase {
        return FreshControlDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideProductDao(database: FreshControlDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideSaleDao(database: FreshControlDatabase): SaleDao = database.saleDao()

    @Provides
    @Singleton
    fun provideUserDao(database: FreshControlDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideAuditDao(database: FreshControlDatabase): AuditDao = database.auditDao()

    @Provides
    @Singleton
    fun provideCashDao(database: FreshControlDatabase): CashDao = database.cashDao()

    @Provides
    @Singleton
    fun provideStockMovementDao(database: FreshControlDatabase): StockMovementDao = database.stockMovementDao()

    @Provides
    @Singleton
    fun provideStoreDao(database: FreshControlDatabase): StoreDao = database.storeDao()
}