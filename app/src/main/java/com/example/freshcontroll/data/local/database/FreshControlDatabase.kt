package com.example.freshcontroll.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.freshcontroll.data.local.dao.AuditDao
import com.example.freshcontroll.data.local.dao.CashDao
import com.example.freshcontroll.data.local.dao.ProductDao
import com.example.freshcontroll.data.local.dao.SaleDao
import com.example.freshcontroll.data.local.dao.StockMovementDao
import com.example.freshcontroll.data.local.dao.StoreDao
import com.example.freshcontroll.data.local.dao.UserDao
import com.example.freshcontroll.data.local.entity.AuditLogEntity
import com.example.freshcontroll.data.local.entity.CashRegisterCloseEntity
import com.example.freshcontroll.data.local.entity.ProductEntity
import com.example.freshcontroll.data.local.entity.SaleDetailEntity
import com.example.freshcontroll.data.local.entity.SaleEntity
import com.example.freshcontroll.data.local.entity.StockMovementEntity
import com.example.freshcontroll.data.local.entity.StoreEntity
import com.example.freshcontroll.data.local.entity.UserEntity

/**
 * Base de datos principal de FreshControl.
 * Nota: No incluimos @TypeConverters porque todos los campos de fecha están definidos como Long nativo.
 */
@Database(
    entities = [
        StoreEntity::class,
        UserEntity::class,
        ProductEntity::class,
        SaleEntity::class,
        SaleDetailEntity::class,
        StockMovementEntity::class,
        AuditLogEntity::class,
        CashRegisterCloseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FreshControlDatabase : RoomDatabase() {

    // Declaración de los 6 DAOs
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun userDao(): UserDao
    abstract fun auditDao(): AuditDao
    abstract fun cashDao(): CashDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun storeDao(): StoreDao

    /**
     * Patrón Singleton clásico para instanciar la base de datos.
     * Aunque Hilt gestionará la inyección de esta base de datos en la app real,
     * este companion object asegura que, a nivel interno de la clase, nunca existan
     * dos instancias conectadas al mismo archivo "freshcontrol_database".
     *
     * @Volatile asegura que el valor de INSTANCE sea visible inmediatamente para todos los hilos.
     * synchronized(this) evita que dos hilos creen la base de datos al mismo tiempo si entran simultáneamente.
     */
    companion object {
        @Volatile
        private var INSTANCE: FreshControlDatabase? = null

        fun getDatabase(context: Context): FreshControlDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FreshControlDatabase::class.java,
                    "freshcontrol_database"
                )
                    .fallbackToDestructiveMigration() // Útil en etapa de desarrollo inicial
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}