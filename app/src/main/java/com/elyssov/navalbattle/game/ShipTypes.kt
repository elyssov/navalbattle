package com.elyssov.navalbattle.game

enum class ShipType(
    val size: Int,
    val abbrRu: String,
    val abbrEn: String,
    val crewMin: Int,
    val crewMax: Int,
    val pcrIntactChance: Int,
    val pcrDamagedChance: Int
) {
    MissileBoat(1, "МК", "MB", 12, 18, 0, 0),
    Mrk(2, "МРК", "MRK", 45, 60, 10, 5),
    Destroyer(3, "ЭМ", "DD", 250, 320, 25, 12),
    Cruiser(4, "КР", "CG", 450, 520, 40, 20),
    Tarkr(5, "ТАРКР", "TARKR", 650, 750, 50, 25),
    Carrier(5, "АВ", "CV", 1800, 2200, 40, 20),
    Submarine(3, "ПЛ", "SS", 80, 110, -1, 0);

    fun abbr(lang: String) = if (lang == "ru") abbrRu else abbrEn
}

object FleetComposition {
    fun shipsFor(size: FieldSize, capital: CapitalChoice): List<Pair<ShipType, Int>> = when (size) {
        FieldSize.Skirmish -> listOf(
            ShipType.MissileBoat to 2, ShipType.Mrk to 2,
            ShipType.Destroyer to 1, ShipType.Cruiser to 1
        )
        FieldSize.Squadron -> listOf(
            ShipType.MissileBoat to 3, ShipType.Mrk to 3,
            ShipType.Destroyer to 2, ShipType.Cruiser to 1,
            (if (capital == CapitalChoice.Tarkr) ShipType.Tarkr else ShipType.Carrier) to 1,
            ShipType.Submarine to 1
        )
        FieldSize.Admiral -> listOf(
            ShipType.MissileBoat to 5, ShipType.Mrk to 4,
            ShipType.Destroyer to 3, ShipType.Cruiser to 3,
            ShipType.Tarkr to 1, ShipType.Carrier to 1, ShipType.Submarine to 1
        )
    }
}

object ShipNames {
    val ru: Map<ShipType, List<String>> = mapOf(
        ShipType.MissileBoat to listOf("Гном","Волна","Таран","Шершень","Оса","Вихрь","Бриз","Шквал"),
        ShipType.Mrk to listOf("Молния","Буря","Гроза","Смерч","Мираж","Ураган","Тайфун","Шторм"),
        ShipType.Destroyer to listOf("Быстрый","Сметливый","Находчивый","Смелый","Умелый","Грозный","Стойкий","Бдительный","Отважный","Решительный"),
        ShipType.Cruiser to listOf("Москва","Ленинград","Новосибирск","Севастополь","Мурманск","Владивосток","Одесса","Киев"),
        ShipType.Tarkr to listOf("Нахимов","Кузнецов","Лазарев","Ушаков","Макаров","Горшков"),
        ShipType.Carrier to listOf("Орлан","Кондор","Сапсан","Беркут","Ястреб","Буревестник"),
        ShipType.Submarine to listOf("Альфа","Лира","Анчар","Барс","Щука","Гепард")
    )
    val en: Map<ShipType, List<String>> = mapOf(
        ShipType.MissileBoat to listOf("Wasp","Hornet","Stinger","Viper","Dart","Storm","Gale","Surge"),
        ShipType.Mrk to listOf("Tempest","Cyclone","Thunder","Monsoon","Squall","Blizzard","Typhoon","Mistral"),
        ShipType.Destroyer to listOf("USS Arleigh Burke","USS Halsey","USS Benfold","USS Milius","USS Stethem","USS Chafee","USS Decatur","USS Momsen","USS Gridley","USS Pinckney"),
        ShipType.Cruiser to listOf("USS Ticonderoga","USS Bunker Hill","USS Monterey","USS Chancellorsville","USS Cowpens","USS Gettysburg","USS Chosin","USS Hue City"),
        ShipType.Tarkr to listOf("USS Alaska","USS Guam","USS Hawaii","USS Samoa","USS Philippines","USS Puerto Rico"),
        ShipType.Carrier to listOf("USS Enterprise","USS Nimitz","USS Roosevelt","USS Lincoln","USS Washington","USS Stennis"),
        ShipType.Submarine to listOf("USS Seawolf","USS Connecticut","USS Jimmy Carter","USS Virginia","USS Texas","USS Hawaii")
    )

    fun pick(type: ShipType, lang: String, used: Set<String>): String {
        val pool = (if (lang == "ru") ru else en)[type] ?: listOf("?")
        val avail = pool.filterNot { it in used }
        return (if (avail.isNotEmpty()) avail else pool).random()
    }
}
