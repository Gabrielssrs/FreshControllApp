package com.example.freshcontroll.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.freshcontroll.data.repository.AuditRepositoryImpl
import com.example.freshcontroll.data.repository.AuthRepositoryImpl
import com.example.freshcontroll.data.repository.CashRepositoryImpl
import com.example.freshcontroll.data.repository.EmployeeRepositoryImpl
import com.example.freshcontroll.data.repository.ProductRepositoryImpl
import com.example.freshcontroll.data.repository.SaleRepositoryImpl
import com.example.freshcontroll.domain.repository.AuditRepository
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.domain.repository.CashRepository
import com.example.freshcontroll.domain.repository.EmployeeRepository
import com.example.freshcontroll.domain.repository.ProductRepository
import com.example.freshcontroll.domain.repository.SaleRepository
import javax.inject.Singleton

/**
 * Módulo de Hilt encargado de enlazar las interfaces de los repositorios (Dominio)
 * con sus respectivas implementaciones concretas (Data).
 * Utiliza @Binds por eficiencia, ya que no requiere instanciar objetos manualmente.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindSaleRepository(
        impl: SaleRepositoryImpl
    ): SaleRepository

    @Binds
    @Singleton
    abstract fun bindEmployeeRepository(
        impl: EmployeeRepositoryImpl
    ): EmployeeRepository

    @Binds
    @Singleton
    abstract fun bindAuditRepository(
        impl: AuditRepositoryImpl
    ): AuditRepository

    @Binds
    @Singleton
    abstract fun bindCashRepository(
        impl: CashRepositoryImpl
    ): CashRepository
}