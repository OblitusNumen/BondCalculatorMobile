package oblitusnumen.bondcalculator.data.database

import BondDetailsEntity
import oblitusnumen.bondcalculator.data.database.entity.CandleEntity
import oblitusnumen.bondcalculator.data.database.entity.CurrencyRateEntity
import oblitusnumen.bondcalculator.data.database.entity.SecurityEntity
import oblitusnumen.bondcalculator.data.schema.Bond
import oblitusnumen.bondcalculator.data.schema.BondDetails
import oblitusnumen.bondcalculator.data.schema.Candle
import oblitusnumen.bondcalculator.data.schema.CurrencyRate
import oblitusnumen.bondcalculator.data.schema.DataVersion
import oblitusnumen.bondcalculator.data.schema.MarketData
import oblitusnumen.bondcalculator.data.schema.Security
import java.time.LocalDate

fun BondDetailsEntity.toDomain(): BondDetails =
    BondDetails(
        bond = Bond(
            secid = secid,
            boardid = boardId,
            shortname = shortName,
            couponValue = couponValue,
            accruedInt = accruedInt,
            prevPrice = prevPrice,
            lotSize = lotSize,
            faceValue = faceValue,
            boardName = boardName,
            matDate = matDate,
            decimals = decimals,
            couponPeriod = couponPeriod,
            issueSize = issueSize,
            secName = secName,
            faceUnit = faceUnit,
            isin = isin,
            latName = latName,
            regNumber = regNumber,
            currencyId = currencyId,
            issueSizePlaced = issueSizePlaced,
            couponPercent = couponPercent,
            lotValue = lotValue,
            callOptionDate = callOptionDate,
            putOptionDate = putOptionDate,
            bondType = bondType,
            bondSubType = bondSubType
        ),

        marketData = MarketData(
            secid = secid,
            bid = bid,
            offer = offer,
            spread = spread,
            bidDepthT = bidDepthT,
            offerDepthT = offerDepthT,
            open = open,
            low = low,
            high = high,
            last = last,
            lastChange = lastChange,
            lastChangePrcnt = lastChangePrcnt,
            value = value,
            yield = yield,
            valueUsd = valueUsd,
            marketPriceToday = marketPriceToday,
            marketPrice = marketPrice,
            lastToPrevPrice = lastToPrevPrice,
            numTrades = numTrades,
            volToday = volToday,
            valToday = valToday,
            valTodayUsd = valTodayUsd,
            boardid = marketBoardId,
            duration = duration,
            change = change,
            seqNum = seqNum,
            valTodayRur = valTodayRur,
            yieldToOffer = yieldToOffer,
            callOptionYield = callOptionYield,
            callOptionDuration = callOptionDuration
        ),

        dataVersion = DataVersion(
            dataVersion = dataVersion,
            seqnum = dataSeqNum,
            tradeDate = tradeDate,
            tradeSessionDate = tradeSessionDate
        )
    )

fun BondDetails.toEntity(
    cachedAt: Long
): BondDetailsEntity {
    val b = bond
    val m = marketData
    val v = dataVersion

    return BondDetailsEntity(
        secid = b.secid,

        boardId = b.boardid,
        shortName = b.shortname,
        couponValue = b.couponValue,
        accruedInt = b.accruedInt,
        prevPrice = b.prevPrice,
        lotSize = b.lotSize,
        faceValue = b.faceValue,
        boardName = b.boardName,
        matDate = b.matDate,
        decimals = b.decimals,
        couponPeriod = b.couponPeriod,
        issueSize = b.issueSize,
        secName = b.secName,
        faceUnit = b.faceUnit,
        isin = b.isin,
        latName = b.latName,
        regNumber = b.regNumber,
        currencyId = b.currencyId,
        issueSizePlaced = b.issueSizePlaced,
        couponPercent = b.couponPercent,
        lotValue = b.lotValue,
        callOptionDate = b.callOptionDate,
        putOptionDate = b.putOptionDate,
        bondType = b.bondType,
        bondSubType = b.bondSubType,

        bid = m.bid,
        offer = m.offer,
        spread = m.spread,
        bidDepthT = m.bidDepthT,
        offerDepthT = m.offerDepthT,
        open = m.open,
        low = m.low,
        high = m.high,
        last = m.last,
        lastChange = m.lastChange,
        lastChangePrcnt = m.lastChangePrcnt,
        value = m.value,
        yield = m.yield,
        valueUsd = m.valueUsd,
        marketPriceToday = m.marketPriceToday,
        marketPrice = m.marketPrice,
        lastToPrevPrice = m.lastToPrevPrice,
        numTrades = m.numTrades,
        volToday = m.volToday,
        valToday = m.valToday,
        valTodayUsd = m.valTodayUsd,
        marketBoardId = m.boardid,
        duration = m.duration,
        change = m.change,
        seqNum = m.seqNum,
        valTodayRur = m.valTodayRur,
        yieldToOffer = m.yieldToOffer,
        callOptionYield = m.callOptionYield,
        callOptionDuration = m.callOptionDuration,

        dataVersion = v.dataVersion,
        dataSeqNum = v.seqnum,
        tradeDate = v.tradeDate,
        tradeSessionDate = v.tradeSessionDate,

        cachedAt = cachedAt
    )
}

fun CandleEntity.toDomain(): Candle =
    Candle(
        boardId = boardId,
        secId = secId,
        tradeDate = LocalDate.ofEpochDay(tradeDateEpochDay),
        tradeSessionDate = tradeSessionDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        shortname = shortname,
        name = name,
        open = open,
        close = close,
        high = high,
        low = low,
        volume = volume,
        value = value,
        numTrades = numTrades,
        marketCap = marketCap,
        currencyId = currencyId
    )

fun Candle.toEntity(): CandleEntity? {
    val date = tradeDate ?: return null

    return CandleEntity(
        boardId = boardId,
        secId = secId,
        tradeDateEpochDay = date.toEpochDay(),
        tradeSessionDateEpochDay = tradeSessionDate?.toEpochDay(),
        shortname = shortname,
        name = name,
        open = open,
        close = close,
        high = high,
        low = low,
        volume = volume,
        value = value,
        numTrades = numTrades,
        marketCap = marketCap,
        currencyId = currencyId
    )
}

fun CurrencyRateEntity.toDomain(): CurrencyRate =
    CurrencyRate(
        id = id,
        charCode = charCode,
        numCode = numCode,
        nominal = nominal,
        name = name,
        value = value,
        vUnitRate = vUnitRate,
        dateEpochDay = date.toEpochDay()
    )

fun CurrencyRate.toEntity(
    cacheDate: LocalDate
): CurrencyRateEntity =
    CurrencyRateEntity(
        id = id,
        charCode = charCode,
        numCode = numCode,
        nominal = nominal,
        name = name,
        value = value,
        vUnitRate = vUnitRate,
        date = date,
        cacheDate = cacheDate
    )

fun SecurityEntity.toDomain(): Security =
    Security(
        secId = secId,
        boardId = boardId,
        shortName = shortName,
        secName = secName,
        latName = latName,
        assetCode = assetCode,
        lotVolume = lotVolume,
        prevPrice = prevPrice,
        bid = bid,
        bidDepth = bidDepth,
        bidDepthT = bidDepthT,
        offer = offer,
        offerDepth = offerDepth,
        offerDepthT = offerDepthT,
        open = open,
        high = high,
        low = low,
        lastPrice = lastPrice,
        qty = qty,
        lastChange = lastChange,
        lastChangePrcnt = lastChangePrcnt,
        numtrades = numtrades,
        volumeToday = volumeToday,
        valueToday = valueToday,
        tradeDate = tradeDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        tradeSessionDate = tradeSessionDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        lastToPrevPrcnt = lastToPrevPrcnt,
        sysTimeEpochSecond = sysTimeEpochSecond
    )

fun Security.toEntity(): SecurityEntity =
    SecurityEntity(
        secId = secId,
        boardId = boardId,
        shortName = shortName,
        secName = secName,
        latName = latName,
        assetCode = assetCode,
        lotVolume = lotVolume,
        prevPrice = prevPrice,
        bid = bid,
        bidDepth = bidDepth,
        bidDepthT = bidDepthT,
        offer = offer,
        offerDepth = offerDepth,
        offerDepthT = offerDepthT,
        open = open,
        high = high,
        low = low,
        lastPrice = lastPrice,
        qty = qty,
        lastChange = lastChange,
        lastChangePrcnt = lastChangePrcnt,
        numtrades = numtrades,
        volumeToday = volumeToday,
        valueToday = valueToday,
        tradeDateEpochDay = tradeDate?.toEpochDay(),
        tradeSessionDateEpochDay = tradeSessionDate?.toEpochDay(),
        lastToPrevPrcnt = lastToPrevPrcnt,
        sysTimeEpochSecond = sysTimeEpochSecond
    )