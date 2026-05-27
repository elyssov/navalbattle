package com.elyssov.navalbattle.game

data class HelpSection(val title: String, val body: String)

object HelpTexts {

    val ru: List<HelpSection> = listOf(
        HelpSection(
            "Адмиральская наука. Слово старого моряка",
            "Садись, юноша. Налей себе чаю — крепкого, чёрного, без сахара. Так пьют те, кто стоял у орудий, когда ватерлиния уходила под воду. Я расскажу тебе про этот «морской бой» — не тот, в который ты с дедом по тетрадке играл, а настоящий. Тут флоты, реакторы, и однажды — да, придётся отвечать за то, что нажал ☢. Слушай."
        ),
        HelpSection(
            "Два поля, а не одно",
            "У тебя перед глазами — две карты. Слева карта твоего флота — там стоят твои корабли. Если враг попал — увидишь красный маркер на своей же палубе. Справа — радар, твоё представление о его флоте. Стреляешь ты по радару, а урон считает он на своей карте. Поля не связаны — нет «расстояния» между твоей эскадрой и его. Только два экрана. Двойная слепота."
        ),
        HelpSection(
            "Размер партии",
            "Стычка (30×30) — короткий пинок. Четыре корабля туда, четыре сюда. Без авианосцев, ракет и атомных штучек. Чистая тактика на пятнадцать минут. Это разминка, юноша. Я её рекомендую, если ты ещё не отличаешь нос от кормы.\n\n" +
            "Эскадра (50×50) — основной формат. Сорок-сорок пять минут хорошего боя. Десять кораблей, одна капитал-единица (выбираешь — ТАРКР или авианосец), одна подлодка. То, ради чего я учил тебя пить чай.\n\n" +
            "Адмирал (100×100) — полный размах. Семнадцать вымпелов, час с лишним. Это уже не бой, а кампания. Береги дыхание."
        ),
        HelpSection(
            "Кто там у тебя",
            "Ракетный катер. Одна клетка, двенадцать душ экипажа. Маленькая, дерзкая, шуршит как мышь. ПКР по ней не работают — слишком юркая. Зато и сам кусается слабо.\n\n" +
            "МРК — малый ракетный корабль. Две клетки. Хороший разменный — недорог, неплохо стреляет, не жалко потерять.\n\n" +
            "Эсминец УРО. Три клетки. Универсал — артиллерия, ПВО, гидролокатор, и одна ракетоторпеда АСРОК на подлодки врага. Спина флота.\n\n" +
            "Крейсер. Четыре клетки. Толстокожий. Хорошее ПВО — 40% сбить «Вулкан». Стреляет долго, тонет неохотно."
        ),
        HelpSection(
            "ТАРКР — убийца флотов",
            "Тяжёлый атомный ракетный крейсер. Пять клеток. Центральная — реактор. Четыре остальные — пусковые установки ПКР П-1000 «Вулкан». Это убийца флотов. Один «Вулкан», прошедший ПРО, топит цель целиком. Без бросков. Семь тонн железа, идущих на сверхзвуке.\n\n" +
            "Но! Реактор. Если враг попадёт в центральную клетку — пятьдесят на пятьдесят что у тебя ядерный гриб прямо в строю. И снесёт всех соседей в радиусе пяти клеток. Своих тоже. Не ставь ТАРКР ближе трёх клеток к остальным. И в попадание в заряженную пусковую — пятнадцать процентов на детонацию боевой части. То же самое — гриб."
        ),
        HelpSection(
            "Авианосец",
            "Тоже пять клеток. Реактор в центре. Несёт три самолёта-разведчика (Ка-31 / E-2 Hawkeye) и шесть штурмовиков (Су-33 / F/A-18). Без него — слеп. И без штурмовиков. Прячь его за островом. Береги."
        ),
        HelpSection(
            "Подводная лодка",
            "Три клетки. Не ставится в начале партии. Вводится потом, действием MOVE, с первого по десятый ход. Не ввёл до конца десятого — считается погибшей. Три обычные торпеды и одна ядерная Т-15 на борту. Невидима для обычной стрельбы — снаряд пройдёт мимо, лодка под водой. Видна только радару или авиаразведке. Запустишь торпеду — всплываешь на ход. И тогда тебя любой увидит."
        ),
        HelpSection(
            "Разведка",
            "РЛС. Только с эсминца и крупнее. Включил — нашёл что-то на 60% базовой вероятности, плюс пять процентов за каждую клетку размера цели. Но! РЛС выдаёт твою позицию. Враг сразу видит твой корабль на своём радаре. И перезарядка три хода. Думай, когда включать.\n\n" +
            "Авиаразведка. С авианосца. Не выдаёт его позицию (хитрая штука). Но шанс что самолёт собьют — есть. Каждый разведчик — один полёт.\n\n" +
            "Устаревание данных. То что увидел сегодня — через четыре хода исчезнет с радара. Корабли не стоят на месте."
        ),
        HelpSection(
            "Чем бить",
            "Артиллерия. Обычный выстрел. Назвал клетку — там оказался корабль или нет. Никаких бросков. Как в твоём детском морском бое. Тут и стратегия — куда стрелять.\n\n" +
            "ПКР «Вулкан». С ТАРКР. Указываешь клетку — если там корабль, противник бросает на перехват. Шанс перехвата зависит от его класса: катер — ноль (мелкий, не достанет), МРК — 10%, эсминец — 25%, крейсер — 40%, ТАРКР — 50% (С-300Ф плюс «Кинжал»), авианосец — 40%. Прошла ПРО — корабль потоплен целиком. Это не «попадание», это конец. Поэтому крейсера и ТАРКР держат подальше — их сложнее достать.\n\n" +
            "Штурмовик. С авианосца. Указал цель — оба игрока бросают D100. Кто больше — выиграл бой. Штурмовик жив — бьёт следующую цель, цепочкой. Сбит — конец. Каждый бой 50/50. Удача.\n\n" +
            "Торпеда. С подлодки. Указал цель — но торпеда не мгновенна. Один ход в воде, противник видит её на своей карте и может уйти. Уйдёт — торпеда мимо. Не уйдёт — корабль уничтожен гарантированно. И помни: если на пути остов корабля — торпеда взорвётся об него."
        ),
        HelpSection(
            "Зачем разворачивать лодку",
            "Подлодку никто не видит, пока не включишь радар или не пошлёшь разведчика. Это твой нож в темноте. Спрячь её там, где у врага плохая радарная сеть (например, у углов поля). Дай ей подобраться. Запусти торпеду — гарантированное потопление если враг не уйдёт.\n\n" +
            "И самое страшное: у каждой лодки на борту одна Т-15, ядерная торпеда. Полтора метра в диаметре. Полтора. Пятнадцать килотонн. Когда ты решишь её пустить — будет ритуал. Я к этому ещё вернусь.\n\n" +
            "Если до конца десятого хода лодку не ввёл — её списывают. Считай, тебе подарили три обычных торпеды и одну ядерную, а ты их в порту оставил. Не делай так."
        ),
        HelpSection(
            "☢ Ядерное оружие",
            "Не во всякой партии. В Стычке — нет. Считается, что там командуют адъютанты, а адъютантам атомного ключа не положено. В Эскадре и Адмирале — да, у каждого игрока один раз за партию. Только Т-15 с подлодки.\n\n" +
            "Когда жмёшь ☢ — начнётся ритуал. Четыре акта. СДВ-станция «ЗЕВС» передаст тебе кодограмму. Командир ПЛ и старпом вскрывают опечатанный пакет. Сверяют коды. И в конце — ты сам введёшь шестизначный пусковой код. Сорок пять секунд на ввод. Ошибёшься — система блокируется. Не успеешь — то же. Если ввёл правильно — выбираешь эпицентр на радаре, и всё что в одиннадцати клетках вокруг исчезает. На три хода зона остаётся радиоактивной. Не лезь туда сам.\n\n" +
            "Ты подумай хорошо, до того как нажать ☢. Это необратимо."
        ),
        HelpSection(
            "Стратегия. Несколько слов от себя",
            "Не ставь корабли вплотную. Я знаю, тянет — компактный строй, всё под рукой. Но если в твой ТАРКР попадёт «Вулкан» — он не упадёт один. И за ним пойдут три твоих эсминца, что стояли рядом. Береги интервалы.\n\n" +
            "Авианосец сразу прячь. За остров, в угол, куда угодно — лишь бы не на середину. Без него ты теряешь и разведку, и штурмовики. Это слепота плюс беззубость.\n\n" +
            "Подлодку вводи рано, но не туда где её сразу найдут радаром. Подумай где у врага «дыра» в обзоре. Запусти торпеду — даже мимо — это его заставит маневрировать, а маневрирующий корабль не стреляет.\n\n" +
            "Разведку береги. Каждый раз когда включаешь РЛС — это либо «я знаю где ты» либо «теперь ты знаешь где я». Иногда лучше не включать.\n\n" +
            "И последнее. Не топи всё ядеркой. Я знаю, соблазн. Один тап — и врага нет. Но если ты пустишь Т-15 в первый ход, ты не научишься играть. Ты научишься нажимать кнопку. А морской бой — это про разведку, маневр и нервы. Атом — это последний аргумент. Когда нечем другим. Я тебе серьёзно говорю."
        ),
        HelpSection(
            "Удачи, юноша",
            "Если что-то спутал — открой эту памятку снова, я тут навсегда. И помни: каждый бой — это история. Корабли, которые ты потеряешь, у каждого было имя. Шестьсот душ экипажа на ТАРКРе. Тысяча восемьсот на авианосце. Шестнадцать на катере. Все они там, на своих палубах, в момент когда ты называешь координаты.\n\n" +
            "Это не значит что не нужно стрелять. Это значит что нужно стрелять точно.\n\n" +
            "С богом, командир. Жду тебя в кают-компании после боя.\n\n— Адмирал"
        )
    )

    val en: List<HelpSection> = listOf(
        HelpSection(
            "An Admiral's Tale. The Old Salt's Lecture",
            "Sit down, son. Pour yourself a cup of tea — strong, black, no sugar. That's how those drink who stood at the guns when the waterline went under. Let me tell you about this \"battleship game\" — not the one you played with your grandfather in a school notebook, but the real one. Fleets, reactors, and one day — yes — you'll answer for pressing ☢. Listen."
        ),
        HelpSection(
            "Two grids, not one",
            "You see two maps in front of you. On the left is your fleet map — that's where your ships stand. If the enemy hits, you'll see a red marker on your own deck. On the right is the radar — your idea of his fleet. You fire at the radar, he tallies damage on his map. The grids aren't connected — there's no \"distance\" between your squadron and his. Just two screens. Double blindness."
        ),
        HelpSection(
            "Match size",
            "Skirmish (30×30) — a short jab. Four ships here, four there. No carriers, no missiles, no atomic surprises. Pure tactics, fifteen minutes. A warm-up, son. I recommend it if you can't yet tell the bow from the stern.\n\n" +
            "Squadron (50×50) — the main format. Forty-five minutes of good fighting. Ten ships, one capital unit (you pick — TARKR or carrier), one submarine. The reason I taught you to drink tea properly.\n\n" +
            "Admiral (100×100) — the full scope. Seventeen pennants, over an hour. This isn't a battle, it's a campaign. Pace your breathing."
        ),
        HelpSection(
            "Who you've got",
            "Missile boat. One cell, twelve souls aboard. Small, brazen, scurries like a mouse. Anti-ship missiles don't work on her — too nimble. But her bite is weak.\n\n" +
            "MRK — small missile corvette. Two cells. A good trade — cheap, reasonable fire, not too sad to lose.\n\n" +
            "Destroyer. Three cells. The universal one — guns, AA, sonar, and one ASROC anti-sub rocket. The spine of the fleet.\n\n" +
            "Cruiser. Four cells. Thick-skinned. Good AA — 40% chance to swat a Vulkan. Fires steadily, sinks reluctantly."
        ),
        HelpSection(
            "TARKR — the fleet killer",
            "Heavy nuclear missile cruiser. Five cells. Center cell is the reactor. The other four are launchers for P-1000 \"Vulkan\" anti-ship missiles. This is a fleet killer. One Vulkan that gets through air defense sinks the target whole. No rolls. Seven tons of armored steel at supersonic.\n\n" +
            "But! The reactor. If the enemy hits the center cell — there's a fifty-fifty chance you get a mushroom cloud in your own formation. It wipes everything within five cells. Yours too. Don't park the TARKR closer than three cells to the others. And a hit on a loaded launcher — fifteen percent for the warhead to cook off. Same result. Mushroom."
        ),
        HelpSection(
            "Aircraft carrier",
            "Also five cells. Reactor in the center. Carries three reconnaissance planes (E-2 Hawkeye / Ka-31) and six strike aircraft (F/A-18 / Su-33). Without her you're blind. And toothless. Hide her behind an island. Mind her."
        ),
        HelpSection(
            "Submarine",
            "Three cells. Not placed at the start. You deploy her later, as a MOVE action, between turn one and ten. If you don't deploy by the end of turn ten — she's lost. Three regular torpedoes and one T-15 nuclear torpedo aboard. Invisible to regular gunfire — the shell goes overhead, she's under the water. Visible only to radar or air recon. Fire a torpedo — she surfaces for a turn. And then anyone can see her."
        ),
        HelpSection(
            "Reconnaissance",
            "Radar. Destroyers and larger only. Switch it on — find something with 60% base, plus five for every cell of target size. But! Radar gives your position away. The enemy instantly sees your ship on his radar. And three-turn cooldown. Think before you switch it on.\n\n" +
            "Air recon. From the carrier. Doesn't give her position away (sneaky). But there's a chance the plane gets shot down. Each scout — one sortie.\n\n" +
            "Aging data. What you saw today fades from radar in four turns. Ships don't stand still."
        ),
        HelpSection(
            "What to hit with",
            "Guns. Regular shot. Name a cell — there's a ship there or not. No rolls. Like your childhood battleship game. The strategy is where to shoot.\n\n" +
            "Vulkan ASM. From the TARKR. Pick a cell — if there's a ship, the opponent rolls for intercept. Intercept chance depends on class: missile boat — zero (too small, can't reach), MRK — 10%, destroyer — 25%, cruiser — 40%, TARKR — 50% (S-300F plus Kinzhal), carrier — 40%. Defense penetrated — the ship is sunk entirely. Not \"hit\". Done. That's why cruisers and TARKRs are kept at distance — harder to reach.\n\n" +
            "Strike aircraft. From the carrier. Pick a target — both players roll D100. Higher wins. Plane survives — strikes the next target, chained. Shot down — over. Each duel 50/50. Luck.\n\n" +
            "Torpedo. From the submarine. Pick a target — but the torpedo isn't instant. One turn in the water, the enemy sees it on his map and can maneuver away. Away — torpedo misses. Stays — guaranteed kill. And remember: a shipwreck in the path stops the torpedo cold."
        ),
        HelpSection(
            "Why you deploy the sub",
            "Nobody sees the submarine until you activate radar or send a plane. She's your knife in the dark. Hide her where his radar grid is thin (corners of the map). Let her crawl in. Launch a torpedo — guaranteed sink if he doesn't maneuver away.\n\n" +
            "And the scariest part: each sub carries one T-15, a nuclear torpedo. Five feet across. Five. Fifteen kilotons. When you decide to launch it — there's a ritual. I'll come back to that.\n\n" +
            "If you don't deploy the sub by the end of turn ten — written off. As if you were gifted three regular torpedoes and one nuclear, and left them in port. Don't."
        ),
        HelpSection(
            "☢ Nuclear weapons",
            "Not every match. Skirmish — no. Adjutants run that one, and adjutants don't get the atomic key. Squadron and Admiral — yes, each player once per match. T-15 from the sub only.\n\n" +
            "When you press ☢ — the ritual begins. Four acts. The VLF \"ZEUS\" station (or TACAMO SKYKING) transmits the order. The sub's captain and XO open the sealed packet. Verify codes. And at the end — you yourself enter the six-digit launch code. Forty-five seconds. Wrong — system locks. Late — same. Right — pick the epicenter on radar, and everything within eleven cells vanishes. For three turns the zone stays irradiated. Don't go there yourself.\n\n" +
            "Think hard before pressing ☢. There's no taking it back."
        ),
        HelpSection(
            "Strategy. A few thoughts",
            "Don't park ships side by side. I know, tempting — tight formation, everything close. But if a Vulkan hits your TARKR — it won't go down alone. Three destroyers next to it will follow. Mind the spacing.\n\n" +
            "Hide the carrier immediately. Behind an island, in a corner, anywhere — just not the middle. Without her you lose both reconnaissance and strike. Blind and toothless together.\n\n" +
            "Deploy the sub early, but not where his radar finds her at once. Think where his coverage has a hole. Launch a torpedo — even one that misses — it forces him to maneuver, and a maneuvering ship doesn't fire.\n\n" +
            "Mind the reconnaissance. Every time you switch on radar it's either \"I know where you are\" or \"now you know where I am\". Sometimes better not to switch.\n\n" +
            "And finally. Don't nuke everything. I know, tempting. One tap — and the enemy's gone. But if you launch the T-15 on turn one, you won't learn to play. You'll learn to press a button. And naval combat is about reconnaissance, maneuver, and nerves. The atom is the last argument. When nothing else works. I'm telling you straight."
        ),
        HelpSection(
            "Good luck, son",
            "If you forget something — open this note again, I'm here for good. And remember: every battle is a story. The ships you'll lose — each had a name. Six hundred souls on a TARKR. Eighteen hundred on a carrier. Sixteen on a missile boat. They're all there, on their decks, the moment you call the coordinates.\n\n" +
            "This doesn't mean don't shoot. It means shoot accurately.\n\n" +
            "Godspeed, commander. I'll be in the wardroom after the battle.\n\n— The Admiral"
        )
    )
}
