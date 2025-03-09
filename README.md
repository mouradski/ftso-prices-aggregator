# FTSO-PRICES-AGGREGATOR

A digital asset price aggregator, the app centralizes real-time prices retrieval from over 82 exchanges using Websocket APIs and distributes them via websocket in a single, simple format.

Collect the prices of the following assets in real time: aave,ada,algo,arb,atom,avax,bch,bnb,bonk,btc,dai,doge,dot,ena,etc,eth,ethfi,fet,fil,flr,ftm,hnt,icp,jup,leo,link,ltc,near,not,ondo,pepe,pol,pyth,qnt,render,rune,sgb,shib,sol,sui,tao,ton,trx,uni,usdc,usdt,wif,xdc,xlm,xrp,matic,trump,s,usds


You can add more assets by editing .env file.

The list of supported exchanges will evolve over time.

## Run with Docker

Edit the .env file and define the assets you want to collect prices in real time (in this example all supported
exchanges)

```sh
ASSETS=aave,ada,algo,arb,atom,avax,bch,bnb,bonk,btc,dai,doge,dot,ena,etc,eth,ethfi,fet,fil,flr,ftm,hnt,icp,jup,leo,link,ltc,near,not,ondo,pepe,pol,pyth,qnt,render,rune,sgb,shib,sol,sui,tao,ton,trx,uni,usdc,usdt,wif,xdc,xlm,xrp,matic
EXCHANGES=binance,binanceus,bitfinex,bitrue,bitstamp,bybit,coinbase,crypto,fmfw,gateio,hitbtc,huobi,kraken,lbank,mexc,okex,upbit,bitmart,bitget,coinex,xt,whitebit,toobit,pionex,btse,bingx,p2b,digifinex,kucoin,gemini,cexio,coinw,pointpay,orangex,biconomy,cointr,bitvenus,tapbit,hashkey,bequant,bigone,ascendex,exmo,cpatex,bydfi,emirex,delta,poloniex,latoken,bit2me,nonkyc,trubit,bluebit,citex,ace,bitso,blofin,bitpanda,coinsbit,coinstore,famex,batonex,websea,nami,indoex,bybitfuture,bitgetfuture,krakenfuture,lbankfuture,whitebitfuture,mexcfuture,cryptofuture,poloniexfuture,deepcoin,azbit,bitvavo,bitdelta,phemex,luno,probit,bibox,hotcoin,bitunix
```

Run container

```sh
docker-compose up
```

## Connect to WS

ws://localhost:8090/ticker



