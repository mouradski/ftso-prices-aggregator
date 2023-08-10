# FTSO-WS-PRICES

Collect the prices of the following assets in real time: "xrp", "btc", "eth", "ltc", "algo", "xlm", "ada", "matic", "
sol", "fil", "flr", "sgb", "doge", "xdc", "arb", "avax", "bnb", "usdc", "busd", "usdt", "dgb", "bch"

The list of supported exchanges will evolve over time.

## Run with Docker

Edit the .env file and define the assets you want to collect prices in real time (in this example all supported
exchanges)

```sh
ASSETS=xrp,btc,eth,algo,xlm,ada,matic,sol,fil,ltc,flr,sgb,doge,xdc,arb,avax,bnb,usdc,busd,usdt,dgb,bch
EXCHANGES=binance,binanceus,bitfinex,bitrue,bitstamp,bybit,coinbase,crypto,digifinex,fmfw,gateio,hitbtc,huobi,kraken,kucoin,lbank,mexc,okex,upbit,btcex,bitmart,bitget,coinex,xt,whitebit,toobit,pionex,btse,gemini,bitforex,bingx,p2b
```

Run container

```sh
docker-compose up
```

## Connect to WS

ws://localhost:8985/trade

## Demo

wscat --connect ws://51.15.221.166:8985/trade


