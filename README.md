# FTSO-WS-PRICES

A digital asset price aggregator, the app centralizes real-time trade retrieval from over 31 exchanges using Websocket APIs and distributes them via websocket in a single, simple format.

Collect the prices of the following assets in real time: "xrp", "btc", "eth", "ltc", "algo", "xlm", "ada", "matic", "
sol", "fil", "flr", "sgb", "doge", "xdc", "arb", "avax", "bnb", "usdc", "busd", "usdt", "dgb", "bch"

You can add more assets by editing .env file.

The list of supported exchanges will evolve over time.

## Run with Docker

Edit the .env file and define the assets you want to collect prices in real time (in this example all supported
exchanges)

```sh
ASSETS=xrp,btc,eth,algo,xlm,ada,matic,sol,fil,ltc,flr,sgb,doge,xdc,arb,avax,bnb,usdc,busd,usdt,dgb,bch
EXCHANGES=binance,binanceus,bitfinex,bitrue,bitstamp,bybit,coinbase,crypto,digifinex,fmfw,gateio,hitbtc,huobi,kraken,kucoin,lbank,mexc,okex,upbit,btcex,bitmart,bitget,coinex,xt,whitebit,toobit,pionex,btse,gemini,bitforex,bingx,p2b,bittrex,digifinex,kucoin,gemini,cexio,bitmake
```

Run container

```sh
docker-compose up
```

## Connect to WS

ws://localhost:8985/trade
ws://localhost:8985/ticker
## Demo

wscat --connect ws://51.15.221.166:8986/trade

wscat --connect ws://51.15.221.166:8986/ticker


