package oblitusnumen.bondcalc.moexapi

import oblitusnumen.bondcalc.MoexInstrument

val IMOEX = MoexInstrument(
    name = "Индекс Мосбиржи",
    engine = "stock",
    market = "index",
    board = "SNDX",
    security = "IMOEX",
)

val USD_RUB = MoexInstrument(
    name = "USDRUBF",
    engine = "futures",
    market = "forts",
    board = "RFUD",
    security = "USDRUBF",
)

val CNY_RUB = MoexInstrument(
    name = "CNYRUB_TOM",
    engine = "currency",
    market = "selt",
    board = "CETS",
    security = "CNYRUB_TOM",
)

val GLD = MoexInstrument(
    name = "Золото GDU6",
    engine = "futures",
    market = "forts",
    board = "RFUD",
    security = "GDU6"
)

val GLDRUB_TOM = MoexInstrument(
    name = "GLDRUB_TOM",
    engine = "currency",
    market = "selt",
    board = "CETS",
    security = "GLDRUB_TOM"
)

val OIL = MoexInstrument(
    name = "Brent oil BR-10.26",
    engine = "futures",
    market = "forts",
    board = "RFUD",
    security = "BRV6"
)

val SBER = MoexInstrument(
    name = "Сбер",
    engine = "stock",
    market = "shares",
    board = "TQBR",
    security = "SBER"
)

val RZD1P51R = MoexInstrument(
    name = "РЖД 1Р-51R",
    engine = "stock",
    market = "bonds",
    board = "TQCB",
    security = "RU000A10E8P0"
)

val allInstruments: List<MoexInstrument> = listOf(
    IMOEX, OIL, USD_RUB, CNY_RUB, GLDRUB_TOM, GLD, SBER, RZD1P51R
)