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
    version = 2,
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
                    totalSlots = 30,
                    maxDimensions = "160×230 cm",
                    notes = "Przednia ekspozycja przy wejściu do działu"
                )
            )

            val stand2Id = standDao.insert(
                DisplayStand(
                    code = "ST-B2",
                    name = "Stojak Średni B - Rozmiar 120×170",
                    section = "Alejka 15 (Dywany Nowoczesne)",
                    totalSlots = 24,
                    maxDimensions = "120×170 cm",
                    notes = "Ekspozycja kolekcji Shaggy i Geometrycznych"
                )
            )

            val stand3Id = standDao.insert(
                DisplayStand(
                    code = "ST-C1",
                    name = "Stojak Klasyczny C - Wełna i Tradycja",
                    section = "Alejka 16 (Dywany Wełniane)",
                    totalSlots = 20,
                    maxDimensions = "200×300 cm",
                    notes = "Kolekcje Agnella Rubin i Isfahan"
                )
            )

            val stand4Id = standDao.insert(
                DisplayStand(
                    code = "WIESZAK-PROMOCJE",
                    name = "Wieszak Strefa Okazji",
                    section = "Czoło alejki 14/15",
                    totalSlots = 16,
                    maxDimensions = "160×230 cm",
                    notes = "Wyprzedaże końcówek serii z obniżoną ceną"
                )
            )

            val carpets = listOf(
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
                    warehouseContainer = null,
                    warehouseSlot = null,
                    isReserved = false,
                    notes = "Tradycyjny perski medalion, wysoka gęstość tkania 500 000 pkt/m2"
                ),
                Carpet(
                    name = "Dywany Agnella Isfahan Złoty Beż",
                    ean = "5901234110029",
                    lmCode = "82345002",
                    widthCm = 200,
                    lengthCm = 300,
                    material = "100% Wełna",
                    patternStyle = "Klasyczny",
                    regularPrice = 1299.00,
                    discountPrice = null,
                    stockQuantity = 2,
                    standId = stand3Id,
                    standSlot = 1,
                    warehouseContainer = null,
                    warehouseSlot = null,
                    isReserved = true,
                    reservedFor = "Marek Kowalski",
                    reservedPhone = "+48 601 234 567",
                    notes = "Klient odbiera w sobotę. Klient prosi o nienaruszanie rezerwacji."
                ),
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
                    warehouseContainer = null,
                    warehouseSlot = null,
                    isReserved = false,
                    notes = "Niezwykle miękkie runo imitujące futro królika, antypoślizgowy spód"
                ),
                Carpet(
                    name = "Dywany Vista Geometryczny Szary/Złoty",
                    ean = "5901234110043",
                    lmCode = "82345004",
                    widthCm = 120,
                    lengthCm = 170,
                    material = "100% Polipropylen Heatset Frise",
                    patternStyle = "Geometryczny",
                    regularPrice = 219.00,
                    discountPrice = 179.00,
                    stockQuantity = 6,
                    standId = stand2Id,
                    standSlot = 1,
                    warehouseContainer = null,
                    warehouseSlot = null,
                    isReserved = false,
                    notes = "Nowoczesne linie w stylu art deco ze złotą przecierką"
                ),
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
                    warehouseContainer = null,
                    warehouseSlot = null,
                    isReserved = false,
                    notes = "Płaskotkany, naturalny splot ekologiczny, łatwy do czyszczenia"
                ),
                Carpet(
                    name = "Dywany Scandinavia Szary Melanż",
                    ean = "5901234110104",
                    lmCode = "82345010",
                    widthCm = 80,
                    lengthCm = 150,
                    material = "Polipropylen płaskotkany",
                    patternStyle = "Nowoczesny",
                    regularPrice = 89.00,
                    discountPrice = 69.00,
                    stockQuantity = 15,
                    standId = null,
                    standSlot = null,
                    warehouseContainer = "Kontener 1",
                    warehouseSlot = "A",
                    isReserved = false,
                    notes = "Chodnik / mały dywanik do przedpokoju. Zapas w Kontenerze 1, Miejsce A."
                ),
                Carpet(
                    name = "Dywany Berber Kremowy Romb",
                    ean = "5901234110135",
                    lmCode = "82345013",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "100% Polipropylen Shaggy",
                    patternStyle = "Boho",
                    regularPrice = 379.00,
                    discountPrice = 299.00,
                    stockQuantity = 6,
                    standId = null,
                    standSlot = null,
                    warehouseContainer = "Kontener 1",
                    warehouseSlot = "B",
                    isReserved = false,
                    notes = "Styl marokański, miękkie grube frędzle. Składowany w Kontenerze 1, Miejsce B."
                ),
                Carpet(
                    name = "Dywany Atlas Tradycyjny Karmin",
                    ean = "5901234110142",
                    lmCode = "82345014",
                    widthCm = 200,
                    lengthCm = 300,
                    material = "Wełna + Akryl",
                    patternStyle = "Klasyczny",
                    regularPrice = 999.00,
                    discountPrice = 849.00,
                    stockQuantity = 3,
                    standId = null,
                    standSlot = null,
                    warehouseContainer = "Kontener 2",
                    warehouseSlot = "A",
                    isReserved = false,
                    notes = "Ciężki dywan tradycyjny, zrolowany w Kontenerze 2, Miejsce A."
                ),
                Carpet(
                    name = "Dywany Palermo Nowoczesny Marmur",
                    ean = "5901234110159",
                    lmCode = "82345015",
                    widthCm = 160,
                    lengthCm = 230,
                    material = "Polipropylen Heatset z lurexem",
                    patternStyle = "Nowoczesny",
                    regularPrice = 429.00,
                    discountPrice = 349.00,
                    stockQuantity = 5,
                    standId = null,
                    standSlot = null,
                    warehouseContainer = "Kontener 2",
                    warehouseSlot = "B",
                    isReserved = false,
                    notes = "Wzór marmuru z połyskującą nicią. Składowany w Kontenerze 2, Miejsce B."
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
                    standSlot = 2,
                    warehouseContainer = null,
                    warehouseSlot = null,
                    isReserved = false,
                    notes = "Certyfikat Oeko-Tex, bezpieczny dla alergików"
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
