package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Carpet
import com.example.data.model.DisplayStand
import com.example.data.model.SyncLogEntry
import com.example.data.model.TakeDownOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Carpet::class,
        DisplayStand::class,
        TakeDownOrder::class,
        SyncLogEntry::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carpetDao(): CarpetDao
    abstract fun displayStandDao(): DisplayStandDao
    abstract fun takeDownOrderDao(): TakeDownOrderDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dywany_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val standDao = database.displayStandDao()
            val carpetDao = database.carpetDao()
            val orderDao = database.takeDownOrderDao()

            val stand1Id = standDao.insert(
                DisplayStand(
                    code = "ST-A1",
                    name = "Stojak Główny A - Rozmiar 160×230",
                    section = "Alejka 14 (Dywany Duże)",
                    totalSlots = 30, // 30 pałąków (po 2 dywany każdy = 60 miejsc)
                    maxDimensions = "160×230 cm",
                    notes = "Główny ekspozytor przesuwny przy wejściu do działu"
                )
            )

            val stand2Id = standDao.insert(
                DisplayStand(
                    code = "ST-B2",
                    name = "Stojak Średni B - Rozmiar 120×170",
                    section = "Alejka 15 (Dywany Nowoczesne)",
                    totalSlots = 24, // 24 pałąki (po 2 dywany każdy = 48 miejsc)
                    maxDimensions = "120×170 cm",
                    notes = "Ekspozycja kolekcji Shaggy i Geometrycznych"
                )
            )

            val stand3Id = standDao.insert(
                DisplayStand(
                    code = "ST-C1",
                    name = "Stojak Klasyczny C - Wełna i Tradycja",
                    section = "Alejka 16 (Dywany Wełniane)",
                    totalSlots = 20, // 20 pałąków (po 2 dywany każdy = 40 miejsc)
                    maxDimensions = "200×300 cm",
                    notes = "Kolekcje Agnella Rubin i Isfahan"
                )
            )

            val carpets = listOf(
                // STOJAK 1 - PAŁĄK 1 (Dywan A i Dywan B)
                Carpet(
                    name = "Dywany Agnella Rubin Rubinowy Klasyk",
                    ean = "5901234110012",
                    lmCode = "82345001",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Wełna nowozelandzka",
                    patternStyle = "Klasyczny",
                    regularPrice = 799.00,
                    discountPrice = 649.00,
                    stockQuantity = 4,
                    standId = stand1Id,
                    standSlot = 1,
                    slotSide = "A",
                    isReserved = false,
                    notes = "Pałąk 1, Strona A: Tradycyjny perski medalion wełniany"
                ),
                Carpet(
                    name = "Dywany Agnella Isfahan Złoty Beż",
                    ean = "5901234110029",
                    lmCode = "82345002",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Wełna",
                    patternStyle = "Klasyczny",
                    regularPrice = 1299.00,
                    discountPrice = null,
                    stockQuantity = 2,
                    standId = stand1Id,
                    standSlot = 1,
                    slotSide = "B",
                    isReserved = true,
                    reservedFor = "Marek Kowalski",
                    reservedPhone = "+48 601 234 567",
                    notes = "Pałąk 1, Strona B: Klient odbiera w sobotę"
                ),

                // STOJAK 1 - PAŁĄK 2 (Dywan A i Dywan B)
                Carpet(
                    name = "Dywany Rabbit Super Soft Beige",
                    ean = "5901234110036",
                    lmCode = "82345003",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Poliester Mikrofibra",
                    patternStyle = "Shaggy",
                    regularPrice = 349.00,
                    discountPrice = 279.00,
                    stockQuantity = 8,
                    standId = stand1Id,
                    standSlot = 2,
                    slotSide = "A",
                    isReserved = false,
                    notes = "Pałąk 2, Strona A: Miękkie runo imitacja królika"
                ),
                Carpet(
                    name = "Dywany Vista Geometryczny Szary/Złoty",
                    ean = "5901234110043",
                    lmCode = "82345004",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Polipropylen Heatset Frise",
                    patternStyle = "Geometryczny",
                    regularPrice = 219.00,
                    discountPrice = 179.00,
                    stockQuantity = 6,
                    standId = stand1Id,
                    standSlot = 2,
                    slotSide = "B",
                    isReserved = false,
                    notes = "Pałąk 2, Strona B: Geometryczny ze złotą przecierką"
                ),

                // STOJAK 1 - PAŁĄK 3 (Dywan A i Dywan B)
                Carpet(
                    name = "Dywany Boho Natural Juta Pleciony",
                    ean = "5901234110050",
                    lmCode = "82345005",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Juta naturalna",
                    patternStyle = "Boho",
                    regularPrice = 299.00,
                    discountPrice = null,
                    stockQuantity = 5,
                    standId = stand1Id,
                    standSlot = 3,
                    slotSide = "A",
                    isReserved = false,
                    notes = "Pałąk 3, Strona A: Płaskotkany splot roślinny"
                ),
                Carpet(
                    name = "Dywany Berber Kremowy Romb Shaggy",
                    ean = "5901234110135",
                    lmCode = "82345013",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Polipropylen Shaggy",
                    patternStyle = "Boho",
                    regularPrice = 379.00,
                    discountPrice = 299.00,
                    stockQuantity = 6,
                    standId = stand1Id,
                    standSlot = 3,
                    slotSide = "B",
                    isReserved = false,
                    notes = "Pałąk 3, Strona B: Wzór marokański w romby"
                ),

                // STOJAK 2 - PAŁĄK 1 (Dywan A i Dywan B)
                Carpet(
                    name = "Dywany Scandinavia Szary Melanż",
                    ean = "5901234110104",
                    lmCode = "82345010",
                    widthCm = 120,
                    lengthCm = 170,
                    material = "Polipropylen płaskotkany",
                    patternStyle = "Nowoczesny",
                    regularPrice = 139.00,
                    discountPrice = 99.00,
                    stockQuantity = 10,
                    standId = stand2Id,
                    standSlot = 1,
                    slotSide = "A",
                    isReserved = false,
                    notes = "Stojak B2, Pałąk 1, Strona A"
                ),
                Carpet(
                    name = "Dywany Kids Safari Zwierzątka",
                    ean = "5901234110074",
                    lmCode = "82345007",
                    widthCm = 120,
                    lengthCm = 170,
                    material = "Poliamid z podkładem filcowym",
                    patternStyle = "Dziecięcy",
                    regularPrice = 149.00,
                    discountPrice = null,
                    stockQuantity = 7,
                    standId = stand2Id,
                    standSlot = 1,
                    slotSide = "B",
                    isReserved = false,
                    notes = "Stojak B2, Pałąk 1, Strona B: Bezpieczny dla alergików"
                ),

                // STOJAK 2 - PAŁĄK 2 (Dywan A)
                Carpet(
                    name = "Dywany Palermo Nowoczesny Marmur",
                    ean = "5901234110159",
                    lmCode = "82345015",
                    widthCm = 120,
                    lengthCm = 170,
                    material = "Polipropylen Heatset z połyskiem",
                    patternStyle = "Nowoczesny",
                    regularPrice = 329.00,
                    discountPrice = 279.00,
                    stockQuantity = 5,
                    standId = stand2Id,
                    standSlot = 2,
                    slotSide = "A",
                    isReserved = false,
                    notes = "Stojak B2, Pałąk 2, Strona A: Połyskujący marmur"
                )
            )

            carpetDao.insertAll(carpets)

            orderDao.insert(
                TakeDownOrder(
                    carpetId = 1,
                    carpetName = "Dywany Agnella Rubin Rubinowy Klasyk",
                    standCode = "ST-A1",
                    standSlot = 1,
                    customerName = "Anna Nowak",
                    customerPhone = "+48 502 999 111",
                    reason = "Klient chce obejrzeć w świetle dziennym i sprawdzić spód",
                    status = TakeDownOrder.STATUS_PENDING,
                    priority = TakeDownOrder.PRIORITY_URGENT,
                    assignedStaff = "Dział Obsługi Klienta"
                )
            )
        }
    }
}
