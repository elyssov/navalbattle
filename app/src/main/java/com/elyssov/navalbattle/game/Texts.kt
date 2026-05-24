package com.elyssov.navalbattle.game

import kotlin.random.Random

object Texts {

    private val captainWordsRu = listOf(
        "«{NAME}» — последний сигнал: \"Прощайте, товарищи... Служу Отечеству.\" Тишина.",
        "«{NAME}»: \"Машина, полный назад—\" ...связь потеряна. Шлюпки в воде. Спасение невозможно — бой продолжается.",
        "«{NAME}», командир: \"Экипажу — покинуть корабль! Оставить раненых не могу. Остаюсь.\" Конец связи.",
        "«{NAME}» — обрыв связи. Тишина в эфире. На месте корабля — масляное пятно и обломки.",
        "«{NAME}»: \"Не сдаёмся! Орудия к б—\" ...помехи... конец передачи. Навсегда.",
        "«{NAME}», радист: \"Крен 40 градусов... уходим под воду... машинное затоплено... не выб—\" Молчание.",
        "«{NAME}»: \"Флаг не спускать.\" Корабль скрылся под водой кормой вперёд. С поднятым флагом.",
        "«{NAME}» — последний доклад: \"Реактор — аварийное... температура активной зоны критическая... всем поки—\" Взрыв.",
        "«{NAME}»: \"Это конец. Слава флоту. Слава экипажу.\" Молчание. Корабля больше нет.",
        "«{NAME}», штурман: \"Координаты переданы. Боезапас детонирует. Отомстите за нас.\" Связь оборвалась.",
        "«{NAME}»: \"Я первый помощник, Семёнов. Капитан погиб. Принимаю командование на с—\" ...белый шум. Тишина.",
        "«{NAME}», мичман из БЧ-5: \"Вода в третьем... четвёртом... переборка между ними сложилась как бумага... ребята не успели выйти...\" Конец связи.",
        "«{NAME}»: \"Пожарным расчётам — отставить. Боезапас не потушить. Всем наверх. Все наверх, мать вашу!\" ...хрип... молчание.",
        "«{NAME}», последний перехват: \"...ватерлиния разрушена по левому борту на протяжении тридцати метров... крен нарастает... контрзатопление невозможно — насосы обесточены...\" Обрыв.",
        "«{NAME}» — из радиорубки, открытым текстом, без шифра: \"Корабль гибнет. Координаты 57-14. Флаг на месте. Экипаж... экипаж выполнил долг.\""
    )

    private val captainWordsEn = listOf(
        "\"{NAME}\" — last signal: \"Farewell, shipmates... I served my country.\" Silence.",
        "\"{NAME}\": \"Engine room, full reverse—\" ...signal lost. Lifeboats in the water. Rescue impossible — battle continues.",
        "\"{NAME}\", captain: \"All hands, abandon ship! I won't leave the wounded. I'm staying.\" End of transmission.",
        "\"{NAME}\" — signal lost. Radio silence. Where the ship was — an oil slick and debris.",
        "\"{NAME}\": \"We don't surrender! Guns to batt—\" ...static... end of transmission. Forever.",
        "\"{NAME}\", radioman: \"List 40 degrees... going under... engine room flooded... can't ge—\" Silence.",
        "\"{NAME}\": \"Don't strike the colors.\" The ship went down stern-first. Colors still flying.",
        "\"{NAME}\" — final report: \"Reactor — emergency... core temperature critical... all hands aband—\" Explosion.",
        "\"{NAME}\": \"This is the end. Glory to the fleet. Glory to the crew.\" Silence. The ship is gone.",
        "\"{NAME}\", navigator: \"Coordinates transmitted. Magazine will detonate. Avenge us.\" Signal lost.",
        "\"{NAME}\": \"This is the XO, Simmons. Captain is dead. I'm assuming command of th—\" ...white noise. Silence.",
        "\"{NAME}\", damage control petty officer: \"Water in compartment three... four... the bulkhead between them folded like paper... the boys didn't make it out...\" End of signal.",
        "\"{NAME}\": \"Fire parties — stand down. Magazine fire can't be contained. All hands topside. Everyone topside, NOW!\" ...rasping... silence.",
        "\"{NAME}\", last intercept: \"...hull breached below waterline along thirty meters on port side... list increasing... counter-flooding impossible — pumps without power...\" Signal lost.",
        "\"{NAME}\" — from radio room, in the clear, no cipher: \"Ship is going down. Grid 57-14. Colors are flying. The crew... the crew did their duty.\""
    )

    private val sunkDescRu = listOf(
        "Попадание пришлось в район мидельшпангоута. Бронепояс не выдержал — трещина прошла ниже ватерлинии. Вода хлынула в машинное отделение, турбины захлебнулись, генераторы встали. Корабль потерял ход, лёг на борт и опрокинулся, обнажив днище, заросшее ракушками. В воздух ударил пузырь мазута и пара. Через сорок секунд на поверхности остались только спасательные плотики и крики.",
        "Ракета вошла в кормовую надстройку и прошила корпус навылет, разорвавшись в погребе. Детонация боезапаса выбросила кормовую башню вместе с барбетом на высоту пятиэтажного дома. Верхнюю палубу вспучило, как консервную банку. Корабль переломился — нос и корма разошлись под углом, между ними — столб чёрного дыма и оранжевого пламени. Носовая часть задрала винты к небу и ушла за двадцать секунд. Кормовая горела ещё час.",
        "Пожар перекинулся на топливные цистерны. Горящий мазут хлынул по внутренним отсекам — переборки раскалились докрасна, а затем лопнули от температурного напряжения. Корабль превратился в плавучий крематорий. Краска пузырилась и стекала с бортов. Боеприпасы рвались внутри, как попкорн. Когда взорвался погреб главного калибра, от корабля осталось облако пара и разлетающиеся во все стороны куски стали.",
        "Торпедная пробоина ниже ватерлинии — восемь на двенадцать метров. Вода заполнила три смежных отсека за полторы минуты. Контрзатопление не помогло — крен перевалил за критические тридцать пять градусов. Люди срывались с палубы в воду. Корабль медленно, словно нехотя, лёг на борт. Винты ещё вращались вхолостую, когда волна накрыла мостик. Ушёл под воду с работающими машинами.",
        "Попадание в реакторный отсек. Аварийная защита не сработала — контрольные стержни заклинило от деформации корпуса. Температура активной зоны за двадцать секунд превысила три тысячи градусов. Расплав прожёг днище. Паровой взрыв поднял корабль из воды на два метра и разломил пополам. Радиоактивное облако поднялось на триста метров. На месте корабля — пятно кипящей воды и фонящие обломки.",
        "Три попадания: два в борт, одно — прямо в ходовую рубку. Командование уничтожено мгновенно. Рулевое управление потеряно. Корабль описал циркуляцию, подставив неповреждённый борт, и получил ещё два попадания. Бак затоплен, палуба просела по шпангоут. Носовая часть зарылась в волну — корма задралась, обнажив вращающиеся винты. Ушёл носом вперёд, вертикально, как будто нырнул.",
        "Ракета попала в пусковую установку. Детонация маршевых двигателей хранящихся ракет вызвала цепную реакцию — взрывы шли один за другим, как фейерверк. Корпус разрывало изнутри. Каждый взрыв выбрасывал через пробоины снопы пламени и обломки. Надстройка сложилась, мачты рухнули. Через три минуты — обугленный, покорёженный остов, слепо дрейфующий по течению. Через семь — остов ушёл под воду.",
        "Ракета вошла в машинное отделение. Маслопровод турбины перебит — горящее масло под давлением хлестало из магистрали, превращая отсек в ад. Аварийная партия не смогла пробиться — жар плавил алюминиевые переборки. Пожар дошёл до топливных танков. Корабль вздрогнул всем корпусом, тяжело осел на корму и начал тонуть — медленно, ровно, без крена. Как будто сам решил уйти с достоинством."
    )

    private val sunkDescEn = listOf(
        "The hit struck amidships. The armor belt failed — a crack ran below the waterline. Water surged into the engine room, turbines choked, generators died. The ship lost way, rolled onto her beam ends and capsized, exposing a barnacle-crusted hull. A bubble of fuel oil and steam erupted skyward. Forty seconds later, only life rafts and screaming remained on the surface.",
        "The missile entered the aft superstructure and punched clean through, detonating in the magazine. The ammunition explosion hurled the aft turret and barbette five stories high. The upper deck buckled like a tin can. The ship broke in two — bow and stern separating at an angle, a column of black smoke and orange flame between them. The bow raised its screws skyward and went under in twenty seconds. The stern burned for another hour.",
        "Fire reached the fuel bunkers. Burning oil flooded the internal compartments — bulkheads glowed red-hot, then burst from thermal stress. The ship became a floating crematorium. Paint bubbled and ran down the sides. Ammunition cooked off inside like popcorn. When the main magazine blew, all that remained was a cloud of steam and steel fragments flying in every direction.",
        "Torpedo breach below the waterline — eight by twelve meters. Water filled three adjacent compartments in ninety seconds. Counter-flooding failed — the list exceeded the critical thirty-five degrees. Men slid off the deck into the sea. The ship slowly, reluctantly, rolled onto her beam. The screws were still turning in the air when the wave covered the bridge. She went under with engines still running.",
        "Hit in the reactor compartment. Emergency protection failed — control rods jammed from hull deformation. Core temperature exceeded three thousand degrees in twenty seconds. The melt burned through the hull. A steam explosion lifted the ship two meters out of the water and broke her in half. A radioactive cloud rose to three hundred meters. Where the ship had been — boiling water and glowing debris.",
        "Three hits: two to the hull, one straight into the bridge. Command killed instantly. Steering lost. The ship swung in a circle, presenting her undamaged side, and took two more hits. The forecastle flooded, the deck sagged to the frames. The bow dug into a wave — the stern rose, exposing spinning propellers. She went down bow-first, vertically, as if diving.",
        "The missile hit a launcher. Detonation of stored missile boosters triggered a chain reaction — explosions rippled one after another like fireworks. The hull tore apart from within. Each blast threw gouts of flame and debris through the breaches. The superstructure collapsed, masts toppled. Three minutes later — a charred, twisted hulk drifting blindly. Seven minutes — the hulk went under.",
        "The missile entered the engine room. A turbine oil line severed — burning oil under pressure sprayed from the main, turning the compartment into an inferno. The damage control party couldn't break through — the heat melted aluminum bulkheads. Fire reached the fuel tanks. The ship shuddered along her entire length, settled heavily by the stern and began to sink — slowly, evenly, without list. As if she chose to go with dignity."
    )

    fun captainLastWords(name: String, lang: String): String {
        val pool = if (lang == "ru") captainWordsRu else captainWordsEn
        return pool.random().replace("{NAME}", name)
    }

    fun sunkDescription(lang: String): String =
        (if (lang == "ru") sunkDescRu else sunkDescEn).random()

    // ── Nuclear ceremony (4 acts + launch confirmation) ──
    val nukeCeremonyRu = listOf(
        "СИГНАЛ ОТ ГЕНЕРАЛЬНОГО ШТАБА ВМФ через СДВ-станцию «ЗЕВС» (82 Гц): Получена кодограмма литер «Ч». Дешифровка... Приказ Верховного Главнокомандующего: санкционировано применение специального боеприпаса. Подтверждение подлинности — сверить с опечатанным пакетом.",
        "Командир ПЛ: «Товарищи офицеры. Получен боевой приказ. Старпом — вскрыть сейф номер один. Достать опечатанный пакет. Сверяем коды...» Старпом: «Коды совпадают. Приказ подлинный. Это не учебная тревога.»",
        "Командир: «Боевая тревога. Ракетная атака. БЧ-2 — расчехлить изделие. Штурман — ввести координаты цели. Старпом — ключ на разблокировку на мой счёт. Предстартовая подготовка — НАЧАТЬ.»",
        "ВВЕДИТЕ ПУСКОВОЙ КОД"
    )
    val nukeCeremonyEn = listOf(
        "EMERGENCY ACTION MESSAGE VIA TACAMO (E-6B MERCURY): «SKYKING, SKYKING, DO NOT ANSWER. Message follows. Authenticate: TANGO-BRAVO. Nuclear weapons release authorized per National Command Authority. Proceed to sealed authentication system. This is not a drill.»",
        "Captain: «XO, authenticate. Open the safe.» XO: «Aye sir. Breaking out the CIP... Comparing against sealed authenticators... Codes match. Message is valid. I concur — this is a valid nuclear launch order.»",
        "Captain: «Battle stations missile. BMC — bring weapon to launch readiness. Navigator — input target coordinates. XO — arming key on my mark. Pre-launch checklist — BEGIN.»",
        "ENTER LAUNCH CODE"
    )
    val nukeLaunchLineRu = "«Ключ на старт — ЕСТЬ. Пуск разрешаю. Три... два... один... ПУСК!»"
    val nukeLaunchLineEn = "«Key turned. Launch authorized. Three... two... one... LAUNCH!»"

    fun generateLaunchCode(): String =
        (1..6).map { Random.nextInt(10) }.joinToString("")
}
