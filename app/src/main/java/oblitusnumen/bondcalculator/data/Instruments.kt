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