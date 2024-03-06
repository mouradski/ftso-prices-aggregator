# FTSO-PRICES-AGGREGATOR

A digital asset price aggregator, the app centralizes real-time prices retrieval from over 55 exchanges using Websocket APIs and distributes them via websocket in a single, simple format.

Collect the prices of the following assets in real time: "xrp", "btc", "eth", "ltc", "algo", "xlm", "ada", "matic", "
sol", "fil", "flr", "sgb", "doge", "xdc", "arb", "avax", "bnb", "usdc", "busd", "usdt", "dgb", "bch"

You can add more assets by editing .env file.

The list of supported exchanges will evolve over time.

## Run with Docker

Edit the .env file and define the assets you want to collect prices in real time (in this example all supported
exchanges)

```sh
ASSETS=xrp,btc,eth,algo,xlm,ada,matic,sol,fil,ltc,flr,sgb,doge,xdc,arb,avax,bnb,usdc,busd,usdt,dgb,bch
EXCHANGES=binance,binanceus,bitfinex,bitrue,bitstamp,bybit,coinbase,crypto,digifinex,fmfw,gateio,hitbtc,huobi,kraken,kucoin,lbank,mexc,okex,upbit,btcex,bitmart,bitget,coinex,xt,whitebit,toobit,pionex,btse,gemini,bitforex,bingx,p2b,digifinex,kucoin,gemini,cexio,bitmake,hotcoin,coinw,deepcoin,pointpay,orangex,biconomy,cointr,bitvenus,tapbit,hashkey,bequant,bigone,ascendex,exmo,cpatex,bydfi,emirex,delta,poloniex,latoken,bit2me,nonkyc
```

Run container

```sh
docker-compose up
```

## Connect to WS

ws://localhost:8985/ticker



