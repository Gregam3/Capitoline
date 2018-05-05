var app = angular.module("capitoline", ['ui.bootstrap', 'smart-table', 'n3-line-chart', 'n3-pie-chart', 'toaster']);

app.run(function ($rootScope) {
    $rootScope.totalValue = 0;
    $rootScope.acquisitionCost = 0;

    $rootScope.user = {};
    $rootScope.isProfiting = null;
    $rootScope.userCurrency = {
        modifier: 1,
        acronym: "USD",
        symbol: '$'
    };

    $rootScope.historicalPortfolio = {
        total: [],
        crypto: [],
        stock: [],
        fiat: []
    };
});
app.value('AlphaVantageKey', 'QVJRID55FX6HALQH');

app.controller("loginCtrl", ['$scope', '$http', 'toaster', '$window', '$rootScope',
    function ($scope, $http, toaster, $window) {
        $scope.loginDetails = {
            email: null,
            password: null
        };
        $scope.registerDetails = {
            email: null,
            name: null,
            password: null,
            confirmedPassword: null
        };

        $scope.heading = "Log In";

        $scope.login = function () {
            $http({
                method: 'POST',
                url: "http://localhost:8080/security/login/",
                data: $scope.loginDetails
            }).then(function successCallback(response) {
                toaster.pop('success', "Logged in", response.data);
                //Cache details necessary to retrieve profile
                $window.localStorage.setItem('email', $scope.loginDetails.email);
                $window.localStorage.setItem('password', $scope.loginDetails.password);

                $window.location.reload();
            }, function errorCallback(response) {
                console.log(response);
                toaster.pop('error', "Failed to Login", response.data);
            });
        };

        $scope.register = function () {
            if ($scope.registerDetails.password !== $scope.registerDetails.confirmedPassword) {
                toaster.pop('error', "Failed to Register", "Password's did not match");
            } else {
                $http({
                    method: 'POST',
                    url: "http://localhost:8080/security/register/",
                    data: $scope.registerDetails
                }).then(function successCallback(response) {
                    toaster.pop('success', "You are now Registered", response.data);
                    $window.localStorage.setItem("email", $scope.registerDetails.email);
                    $window.localStorage.setItem("password", $scope.registerDetails.password);
                    $window.location.reload();
                }, function errorCallback(response) {
                    console.log(response);
                    toaster.pop('error', "Failed to Register", response.data);
                });
            }
        };

        $scope.changeHeading = function (newHeading) {
            console.log(newHeading);
            $scope.heading = newHeading;
        };
    }]);

app.controller("homeCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', 'toaster', '$timeout', '$q',
    function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey, toaster, $timeout, $q) {
        $scope.historicalPortfolioStatus = "loading";

        //Fetch and process graph data
        $rootScope.fetchHistoricalPortfolioData = function () {
            $rootScope.historicalPortfolioStatus = 'loading';

            $http.get(
                'http://localhost:8080/user/get/holding-graph-data/'
            ).then(function successCallback(response) {
                // console.log("Behind the Scenes: Based on the user's current holdings and the date(s) they were added, the back-end " +
                //     "fetches and parses this into a format able to be displayed on a graph. Some data takes far longer than " +
                //     "others based on whether or not the holding is a stock (more parsing necessary) and based on how much history " +
                //     "needs to be fetched", $rootScope.historicalPortfolio);

                $rootScope.historicalPortfolio.total = response.data.total;
                $rootScope.historicalPortfolio.crypto = response.data.crypto;
                $rootScope.historicalPortfolio.fiat = response.data.fiat;
                $rootScope.historicalPortfolio.stock = response.data.stock;

                $rootScope.historicalPortfolioStatus =
                    ($scope.historicalPortfolio.total.length > 10) ? "loaded" : "too recent";

            }, function errorCallback(response) {
                console.log(response);
                toaster.pop('error', "Error Fetching Stock History",
                    (response.data) ? response.data.data : 'Stock API may be down');
            });
        };

        $rootScope.formatValues = function (value) {
            if (value < 0)
                value = value * -1;

            if (value > 1000000000)
                return $rootScope.userCurrency.symbol + (value / 1000000000000).toFixed(3) + "TN";

            if (value > 1000000000)
                return $rootScope.userCurrency.symbol + (value / 1000000000).toFixed(3) + "BN";

            if (value > 1000000)
                return $rootScope.userCurrency.symbol + (value / 1000000).toFixed(3) + "MM";

            if (value > 1000)
                return $rootScope.userCurrency.symbol + (value / 1000).toFixed(3) + "k";

            return $rootScope.userCurrency.symbol + value.toFixed(2);
        };

        $rootScope.toPercentage = function (totalValue, acquisitionCost) {
            return ((totalValue / acquisitionCost) * 100 - 100).toFixed(2) + '%';
        };


        let stockShownWarning = false;
        let currencyShownWarning = false;

        $rootScope.updateUser = function () {
            $rootScope.loaded = false;

            stockShownWarning = false;
            currencyShownWarning = false;

            $rootScope.cryptoValue = 0;
            $rootScope.stockValue = 0;
            $rootScope.fiatValue = 0;

            $rootScope.totalValue = 0;

            $http.get('http://localhost:8080/user/get')
                .then(function (response) {
                    // console.log("Behind the Scenes: Capitoline has fetched user data based on the login, " +
                    //     "after a user logs in it sets the server-side variable \"currentUser\" " +
                    //     "to their user data. This is also retrieved when a change is made to the user " +
                    //     "on the client-side. Their user data looks like the following: ", response.data);
                    console.log(response.data);

                    $rootScope.user = response.data;

                    $rootScope.userCurrency.acronym = $rootScope.user.settings.userCurrency.acronym;

                    if ($rootScope.user.settings.userCurrency.acronym === "USD") {
                        $rootScope.userCurrency.modifier = 1.0;
                        $rootScope.userCurrency.symbol = '$';
                    } else {
                        $http.get('https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=' + $rootScope.user.settings.userCurrency.acronym)
                            .then(function (response) {
                                //a modifier used instead of changing requests as AlphaVantage does not provide currency support
                                $rootScope.userCurrency.modifier = response.data[$rootScope.user.settings.userCurrency.acronym];
                                $rootScope.userCurrency.symbol = $rootScope.user.settings.userCurrency.symbol;
                            });
                    }

                    $rootScope.acquisitionCost = 0;

                    $rootScope.holdings = {
                        cryptos: {},
                        stocks: {},
                        fiats: {}
                    };

                    for (let i = 0; i < $rootScope.user.holdings.length; i++) {
                        const currentHolding = $rootScope.user.holdings[i];

                        if (currentHolding.holdingType === "STOCK")
                            $rootScope.holdings.stocks[currentHolding.acronym] = currentHolding;
                        else if (currentHolding.holdingType === "CRYPTO")
                            $rootScope.holdings.cryptos[currentHolding.acronym] = currentHolding;
                        else if (currentHolding.holdingType === "FIAT")
                            $rootScope.holdings.fiats[currentHolding.acronym] = currentHolding;


                        $rootScope.acquisitionCost += currentHolding.acquisitionCost;
                    }


                    $rootScope.generatePerformance();
                    $rootScope.fetchHistoricalPortfolioData();
                    updateHoldingValue();
                });
        };

        // console.log("Behind the Scenes: Updated user prices and anything reliant on them, Capitoline updates its data every 10 seconds." +
        //     " the data retrieved from the APIs updates roughly ever 30 seconds.");

        //Arbitrary value over 10000


        const updateHoldingValue = function () {

            let tempHoldings = $rootScope.holdings;
            let tempTotalValue = 0;
            let tempCryptoValue = 0;
            let tempFiatValue = 0;
            let tempStockValue = 0;

            $scope.currencyCall = null;
            $scope.stockCall = null;

            if (!angular.equals(tempHoldings.cryptos, {}) || !angular.equals(tempHoldings.fiats, {})) {
                $scope.currencyCall = $http.get(
                    "https://min-api.cryptocompare.com/data/pricemulti?fsyms="
                    + $rootScope.convertHoldingsToPathVariables(tempHoldings.cryptos) + ","
                    + $rootScope.convertHoldingsToPathVariables(tempHoldings.fiats)
                    + "&tsyms=" + $rootScope.userCurrency.acronym
                ).then(function (response) {
                    let holding;
                    for (holding in tempHoldings.cryptos) {
                        tempHoldings.cryptos[holding].price =
                            (response.data[holding]) ? response.data[holding][$rootScope.userCurrency.acronym] : null;
                        const currentValue = tempHoldings.cryptos[holding].price * tempHoldings.cryptos[holding].totalQuantity;

                        tempHoldings.cryptos[holding].totalValue = currentValue;
                        tempTotalValue += currentValue;
                        tempCryptoValue += currentValue;

                    }

                    for (holding in tempHoldings.fiats) {
                        tempHoldings.fiats[holding].price =
                            (response.data[holding]) ? response.data[holding][$rootScope.userCurrency.acronym] : null;

                        if (!tempHoldings.fiats[holding].price && !currencyShownWarning) {
                            console.log(tempHoldings.fiats, holding);
                            console.log(tempHoldings.fiats[holding]);
                            toaster.pop('warning', "Issue retrieving a Fiat currency",
                                "The CryptoCompare API cannot fetch data for " + holding);
                            currencyShownWarning = true;
                        }

                        const currentValue =
                            tempHoldings.fiats[holding].price * tempHoldings.fiats[holding].totalQuantity;

                        tempHoldings.fiats[holding].totalValue = currentValue;
                        tempTotalValue += currentValue;
                        tempFiatValue += currentValue;
                    }
                })
            }


            //Get all User's stock prices
            if (!angular.equals(tempHoldings.stocks, {})) {
                $scope.stockCall = $http.get("https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols="
                    + $rootScope.convertHoldingsToPathVariables($scope.holdings.stocks)
                    + "&apikey=" + AlphaVantageKey).then(function successCallback(response) {
                    let i = 0;
                    for (const holding in tempHoldings.stocks) {
                        let currentResponseItem = response.data["Stock Quotes"];

                        tempHoldings.stocks[holding].price = 0;
                        tempHoldings.stocks[holding].totalValue = 0;

                        for (const stockQuoteIndex in currentResponseItem) {
                            if (currentResponseItem[stockQuoteIndex]["1. symbol"] === holding) {
                                tempHoldings.stocks[holding].price = currentResponseItem[stockQuoteIndex]["2. price"];

                                const currentValue = tempHoldings.stocks[holding].price * tempHoldings.stocks[holding].totalQuantity;

                                // two = are intentional for conversion

                                tempHoldings.stocks[holding].totalValue = currentValue;
                                tempTotalValue += currentValue * $rootScope.userCurrency.modifier;
                                tempStockValue += currentValue * $rootScope.userCurrency.modifier;

                                i++;
                            }
                        }

                        if ((tempHoldings.stocks[holding].price == 0 || !tempHoldings.stocks[holding].price) && !stockShownWarning) {
                            toaster.pop('warning', "Issue retrieving " + tempHoldings.stocks[holding].acronym,
                                "The AlphaVantage API used in Capitoline is currently experiencing problems, this may influence your portfolio's accuracy.");
                            shownWarning = true;
                        }
                    }


                }, function errorCallback() {
                    $scope.stockCall = null;

                    toaster.pop('warning', "Issue retrieving " + tempHoldings.stocks[holding].acronym,
                        "The AlphaVantage API used in Capitoline is currently experiencing problems, this may influence your portfolio's accuracy.");
                })


            }

            if ($scope.currencyCall && $scope.stockCall)
                $q.all([$scope.currencyCall, $scope.stockCall])
                    .then(function () {
                        callUpdateMethods(tempTotalValue, tempCryptoValue, tempFiatValue, tempStockValue);
                    });
            else if ($scope.currencyCall)
                $q.all([$scope.currencyCall])
                    .then(function () {
                        callUpdateMethods(tempTotalValue, tempCryptoValue, tempFiatValue, tempStockValue);
                    });
            else if ($scope.stockCall)
                $q.all([$scope.currencyCall])
                    .then(function () {
                        callUpdateMethods(tempTotalValue, tempCryptoValue, tempFiatValue, tempStockValue);
                    });
            else
                $rootScope.loaded = true;
        };

        const callUpdateMethods = function (totalValue, tempCryptoValue, tempFiatValue, tempStockValue) {
            $rootScope.totalValue = totalValue;
            $rootScope.cryptoValue = tempCryptoValue;
            $rootScope.fiatValue = tempFiatValue;
            $rootScope.stockValue = tempStockValue;

            generateDiversificationPieChart();
            generateIsProfiting();
            $rootScope.loaded = true;
            console.log("Behind the Scenes: Updated");
        };

        const autoUpdate = function () {
            $timeout(updateHoldingValue, 10000);
            $timeout(autoUpdate, 10000);
        };

        autoUpdate();

        const generateDiversificationPieChart = function () {
            $rootScope.portfolioDiversification = [
                {
                    label: "Cryptos",
                    value: (($scope.cryptoValue / $rootScope.totalValue) * 100).toFixed(2),
                    color: '#f7931a'
                },
                {
                    label: "Fiat",
                    value: (($scope.fiatValue / $rootScope.totalValue) * 100).toFixed(2),
                    color: '#c47472'
                },
                {
                    label: "Stocks",
                    value: (($scope.stockValue / $rootScope.totalValue) * 100).toFixed(2),
                    color: '#35b2c8'
                }
            ];
        };


        const generateIsProfiting = function () {
            $rootScope.isProfiting =
                $rootScope.totalValue > $rootScope.acquisitionCost;
        };

        $rootScope.convertHoldingsToPathVariables = function (currentHoldingsList) {
            let tickers = "";

            for (const holding in currentHoldingsList) {
                tickers += currentHoldingsList[holding].acronym + ",";
            }

            //Removing trailing comma for uri format
            return tickers.substr(0, tickers.length - 1);
        };
    }]);

app.controller("performanceCtrl", ['$scope', '$http', '$rootScope', 'toaster', 'AlphaVantageKey',
    function ($scope, $http, $rootScope, toaster, AlphaVantageKey) {

        //Cannot have expression in html due to SAX Parsing not accepting &
        $scope.portfolioNotOldEnough = function () {
            return $rootScope.historicalPortfolio.total.length > 0 && $rootScope.historicalPortfolio.total.length < 10;
        };

        $scope.lineOptions = {
            series: [
                {
                    axis: "y",
                    dataset: "total",
                    key: "value",
                    label: "Total Value",
                    color: "#636363",
                    type: ['line', 'area'],
                    id: 'mySeries0'
                },
                {
                    axis: "y",
                    dataset: "crypto",
                    key: "value",
                    label: "Crypto Value",
                    color: "#f7931a",
                    type: ['line', 'area'],
                    id: 'mySeries1'
                },
                {
                    axis: "y",
                    dataset: "stock",
                    key: "value",
                    label: "Stock Value",
                    color: "#35b2c8",
                    type: ['line', 'area'],
                    id: 'mySeries2'
                },
                {
                    axis: "y",
                    dataset: "fiat",
                    key: "value",
                    label: "Fiat Value",
                    color: "#c47472",
                    type: ['line', 'area'],
                    id: 'mySeries3'
                }

            ],
            axes: {
                x: {
                    key: "time",
                    type: 'date',
                    tickFormat: d3.time.format("%d %b %y")
                },
                y: {
                    type: ''
                }
            }
        };

        $scope.pieOptions = {
            thickness: 10
        };

        $rootScope.portfolioDiversification = [];


        //Crypto
        let btcValueNow = 0;
        let btcValueOneMonthAgo = 0;
        $scope.btcChange = 0;

        let userCryptoValueNow = 0;
        let userCryptoValueOneMonthAgo = 0;
        $scope.portfolioCryptoChange = 0;
        $scope.volatility = [];

        //Fiat
        let fiatIndexValueNow = 0;
        let fiatIndexValueOneMonthAgo = 0;
        $scope.fiatIndexChange = 0;

        let userFiatValueNow = 0;
        let userFiatValueOneMonthAgo = 0;
        $scope.portfolioFiatChange = 0;

        //Stock
        $scope.aggregateSectorChange = 0;
        $scope.portfolioStockChange = 0;

        $rootScope.generatePerformance = function () {
            let currencyPathVariables = "";

            const cryptosPathVariable = $rootScope.convertHoldingsToPathVariables($rootScope.holdings.cryptos);
            const fiatsPathVariable = $rootScope.convertHoldingsToPathVariables($rootScope.holdings.fiats);

            let userCryptoAcronyms = (cryptosPathVariable.length > 1) ? cryptosPathVariable.split(",") : [];
            let userFiatAcronyms = (fiatsPathVariable.length > 1) ? fiatsPathVariable.split(",") : [];

            currencyPathVariables = cryptosPathVariable + "," +
                fiatsPathVariable;

            if (currencyPathVariables !== "") {
                $http.get("https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=BTC,JPY,EUR,GBP,CNY,CHF")
                    .then(function (response) {
                        btcValueNow = response.data["BTC"];
                        fiatIndexValueNow =
                            response.data["JPY"] +
                            response.data["EUR"] +
                            response.data["GBP"] +
                            response.data["CNY"] +
                            response.data["CHF"];


                        $http.get('https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=' + currencyPathVariables)
                            .then(function (innerResponse) {
                                for (const index in userFiatAcronyms)
                                    userFiatValueNow += innerResponse.data[userFiatAcronyms[index]]
                                        * $rootScope.holdings.fiats[userFiatAcronyms[index]].totalQuantity;


                                for (const index in userCryptoAcronyms)
                                    userCryptoValueNow += innerResponse.data[userCryptoAcronyms[index]] *
                                        +$rootScope.holdings.cryptos[userCryptoAcronyms[index]].totalQuantity;

                            });

                        $http.get("https://min-api.cryptocompare.com/data/pricehistorical?fsym=USD&tsyms=BTC,JPY,EUR,GBP,CNY,CHF" +
                            "&ts=" + (new Date().getTime() - (7 * 24 * 60 * 60 * 1000)))
                            .then(function (response) {
                                const responseData = response.data["USD"];

                                btcValueOneMonthAgo = responseData["BTC"];
                                fiatIndexValueOneMonthAgo =
                                    responseData["JPY"] +
                                    responseData["EUR"] +
                                    responseData["GBP"] +
                                    responseData["CNY"] +
                                    responseData["CHF"];

                                $http.get("https://min-api.cryptocompare.com/data/pricehistorical?fsym=USD&tsyms=" + currencyPathVariables +
                                    "&ts=" + (new Date().getTime() - (7 * 24 * 60 * 60 * 1000)))
                                    .then(function (innerResponse) {
                                        const innerResponseData = innerResponse.data["USD"];

                                        for (const index in userFiatAcronyms)
                                            userFiatValueOneMonthAgo += innerResponseData[userFiatAcronyms[index]]
                                                * $rootScope.holdings.fiats[userFiatAcronyms[index]].totalQuantity;

                                        for (const index in userCryptoAcronyms)
                                            userCryptoValueOneMonthAgo += innerResponseData[userCryptoAcronyms[index]]
                                                * $rootScope.holdings.cryptos[userCryptoAcronyms[index]].totalQuantity;
                                    });

                                $scope.btcChange = ((btcValueNow / btcValueOneMonthAgo) * 100 - 100).toFixed(3);
                                $scope.fiatIndexChange = ((fiatIndexValueNow / fiatIndexValueOneMonthAgo) * 100 - 100).toFixed(2);
                            });
                    });

            }
        };


        $scope.calculateCryptoPerformance = function () {
            if (userCryptoValueNow === 0 || userCryptoValueOneMonthAgo === 0)
                $scope.performanceNotReadyPopUp("cryptocurrency");
            else
                $scope.portfolioCryptoChange =
                    ((userCryptoValueNow / userCryptoValueOneMonthAgo)
                        * 100 - 100).toFixed(3);

            $scope.generateCryptoVolatilityPieChart();
        };

        $scope.calculateFiatPerformance = function () {
            console.log(userFiatValueNow, userFiatValueOneMonthAgo);
            if (userFiatValueNow === 0 || userFiatValueOneMonthAgo === 0)
                $scope.performanceNotReadyPopUp("fiat currency");
            else

                console.log(userFiatValueNow, userFiatValueOneMonthAgo);
            $scope.portfolioFiatChange =
                ((userFiatValueNow / userFiatValueOneMonthAgo)
                    * 100 - 100).toFixed(2);
        };

        $scope.retrieveAndCalculateStockPerformance = function () {
            if ($rootScope.historicalPortfolio.stock.length === 0)
                $scope.performanceNotReadyPopUp("stock");
            else {
                $http.get('https://www.alphavantage.co/query?function=SECTOR&apikey=' + AlphaVantageKey)
                    .then(function (response) {
                        const sectorPerformance = response.data["Rank D: 1 Month Performance"];

                        console.log(sectorPerformance);

                        $http.get('http://localhost:8080/stock/portfolio-stock-change-over-month')
                            .then(function successCallback(responseTwo) {
                                console.log(responseTwo);
                                $scope.portfolioStockChange = responseTwo.data.toFixed(3);
                            }, function () {
                                toaster.pop('error', 'Issue with API',
                                    "The AlphaVantage API used by Capitoline is currently experiencing " +
                                    "issues returning  data needed for your stock performance.")
                            });


                        //AlphaVantage API sends data back with trailing '%', this needs to be removed in order for it to be treated as a number
                        $scope.aggregateSectorChange =
                            ((sectorPerformance["Utilities"].substr(0, sectorPerformance["Utilities"].length - 1) * 1 +
                                sectorPerformance["Energy"].substr(0, sectorPerformance["Energy"].length - 1) * 1 +
                                sectorPerformance["Information Technology"].substr(0, sectorPerformance["Information Technology"].length - 1) * 1 +
                                sectorPerformance["Consumer Discretionary"].substr(0, sectorPerformance["Consumer Discretionary"].length - 1) * 1 +
                                sectorPerformance["Telecommunication Services"].substr(0, sectorPerformance["Telecommunication Services"].length - 1) * 1 +
                                sectorPerformance["Health Care"].substr(0, sectorPerformance["Health Care"].length - 1) * 1 +
                                sectorPerformance["Industrials"].substr(0, sectorPerformance["Industrials"].length - 1) * 1 +
                                sectorPerformance["Financials"].substr(0, sectorPerformance["Financials"].length - 1) * 1) / 8).toFixed(3);
                    });
            }
        };

        $scope.performanceNotReadyPopUp = function (holdingType) {
            toaster.pop('info', "Cannot do that yet", "We might still be loading your " + holdingType + " data, " +
                "if not you can only view your " + holdingType + " analysis after adding some to your portfolio.");

            $scope.active = 0;
        };

        $scope.generateCryptoVolatilityPieChart = function () {
            for (const holding in $rootScope.holdings.cryptos) {
                $http.get("https://min-api.cryptocompare.com/data/histoday?fsym="
                    + $rootScope.userCurrency.acronym
                    + "&tsym=" + holding + "&limit=30")
                    .then(function (response) {
                        const currentHoldingHistory = response.data.Data;

                        let high = 0;
                        let low = 1000000;

                        for (const day in currentHoldingHistory) {
                            const currentPrice = currentHoldingHistory[day]["close"];
                            if (currentPrice > high) {
                                high = currentPrice
                            } else if (currentPrice < low) {
                                low = currentPrice;
                            }
                        }
                        const volatility = (high / low);

                        if (volatility < 1.25) $scope.volatility[0].value++;
                        else if (volatility > 1.25 && volatility < 1.5) $scope.volatility[1].value++;
                        else if (volatility > 1.5 && volatility < 2) $scope.volatility[2].value++;
                        else $scope.volatility[3].value++;
                    });
            }

            $scope.volatility = [
                {
                    label: "<25%",
                    value: 0,
                    color: '#0000d6'
                },
                {
                    label: "25%-50%",
                    value: 0,
                    color: '#5ab4c6'
                },
                {
                    label: "50%-100%",
                    value: 0,
                    color: '#d65e21'
                },
                {
                    label: ">100%",
                    value: 0,
                    color: '#d61700'
                }];
        };

        $rootScope.updateUser();
    }]);

app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', 'toaster',
    function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey, toaster) {
        $scope.isObjectEmpty = function (object) {
            return angular.equals(object, {});
        };

        $scope.lessThanOne = function (value) {
            return value < 1;
        };

        $rootScope.currentHoldingInfo = null;

        $scope.viewHoldingInfo = function (acronym, holdingType) {
            let currentHolding = null;

            if (holdingType === "CRYPTO")
                currentHolding = $rootScope.holdings.cryptos[acronym];
            else if (holdingType === "STOCK")
                currentHolding = $rootScope.holdings.stocks[acronym];
            else
                currentHolding = $rootScope.holdings.fiats[acronym];

            $rootScope.currentHoldingInfo = currentHolding;

            $rootScope.tempModifier = ($rootScope.currentHoldingInfo.holdingType === "STOCK") ?
                $rootScope.userCurrency.modifier : 1;

            $uibModal.open({
                templateUrl: 'templates/home/popups/holding-info-popup.html',
                controller: 'holdingInfoCtrl',
                size: 'lg'
            });


        };

        $scope.addHolding = function () {
            $uibModal.open({
                templateUrl: 'templates/home/popups/add-holding-popup.html',
                controller: 'addHoldingCtrl',
                size: 'md'
            });
        };

        $scope.removeHolding = function (acronym, holdingType, amountToRemove) {
            $http.delete('http://localhost:8080/user/delete/holding/' + acronym + '/' + holdingType + '/' + amountToRemove)
                .then(function successCallback(response) {
                    toaster.pop('success', "Successfully removed", "Removed " + amountToRemove + " from " + acronym);
                    $rootScope.updateUser();
                }, function errorFallback(response) {
                    toaster.pop('error', "Failed to Remove " + acronym, response.data);
                });
        };
    }]);

app.controller("holdingInfoCtrl", ['$scope', '$uibModalStack', '$rootScope', function ($scope, $uibModalStack, $rootScope) {
}]);

app.controller("addHoldingCtrl", ['$scope', '$http', '$uibModalStack', '$rootScope', 'toaster',
    function ($scope, $http, $uibModalStack, $rootScope, toaster) {
        $scope.holding = {};

        $scope.showHoldingToReduce = false;
        $scope.holdingToReduce = null;
        $scope.holdingsLoaded = false;

        $scope.setHoldingToReduceAsSelectedCurrency = function () {
            const selectedCurrencyAsUserHolding = getSelectedCurrencyAsUserHolding();

            if (selectedCurrencyAsUserHolding)
                $scope.holdingToReduce = $rootScope.user.settings.currency;
        };

        const getSelectedCurrencyAsUserHolding = function () {
            for (let i = 0; i < $rootScope.user.holdings; i++)
                if ($rootScope.user.holdings[i].acronym === $rootScope.user.settings.currency.acronym)
                    return $rootScope.user.holdings[i];

            return null;
        };

        let newHolding = {
            email: $rootScope.user.email,
            name: null,
            acronym: null,
            holdingType: null,
            quantity: -1,
            dateBought: null
        };

        $scope.options = {
            minDate: new Date(946684800000),
            maxDate: new Date()
        };

        $scope.changeDatesAvailable = function (holdingType) {
            if (holdingType === "STOCK")
                $scope.options = {
                    minDate: new Date(946684800000),
                    maxDate: new Date()
                };
            else if (holdingType === "CRYPTO")
                $scope.options = {
                    minDate: new Date(1230768000000),
                    maxDate: new Date()
                };
            else
                $scope.options = {
                    minDate: new Date(1420070400000),
                    maxDate: new Date()
                };
        };


        console.log(newHolding);

        $scope.holdingList = [];

        //save reloading every time
        if ($scope.holdingList.length === 0) {
            $scope.holdingsLoaded = false;
            console.log("Getting holdings");

            //Callbacks needed to avoid concurrency exceptions
            $http.get(
                "http://localhost:8080/crypto/list"
            ).then(function (response) {
                $scope.holdingList = $scope.holdingList.concat(response.data);
                $http.get(
                    "http://localhost:8080/fiat/list"
                ).then(function (response) {
                    $scope.holdingList = $scope.holdingList.concat(response.data);
                    $http.get(
                        "http://localhost:8080/stock/list"
                    ).then(function (response) {
                        $scope.holdingList = $scope.holdingList.concat(response.data);
                        $scope.holdingsLoaded = true;
                    });
                });
            });
        }

        $scope.add = function () {
            // console.log("Behind the Scenes: Adding a new holding to the current user's portfolio, " +
            //     "this will either be a completely new holding or it will append something called a transaction " +
            //     "to an existing holding, updating its quantity and keeping track of its history.");

            newHolding = {
                acronym: $scope.holding.acronym,
                name: $scope.holding.name,
                holdingType: $scope.holding.holdingType,
                quantity: $scope.quantity,
                //* 1 to get unix time
                dateBought: ($scope.holding.dateBought) ? $scope.holding.dateBought * 1 : new Date() * 1,
                holdingToReduce: $scope.holdingToReduce
            };

            $scope.addingHolding = true;

            $http({
                method: 'PUT',
                url: "http://localhost:8080/user/add-holding",
                data: newHolding
            }).then(function successCallback() {
                if (newHolding.quantity === 0) toaster.pop('success', "Successfully Watched", "Began watching " + newHolding.name);
                else toaster.pop('success', "Successfully Added", "Added " + newHolding.quantity + " instance of " + newHolding.name);
                $rootScope.updateUser();
                $scope.addingHolding = false;
                $uibModalStack.dismissAll();
            }, function errorCallback(response) {
                toaster.pop('error', "Failed to Add", response.data);
                $scope.addingHolding = false;
            });
        };
    }]);

app.controller("settingsCtrl", ['$scope', '$http', '$uibModalStack', 'toaster', '$rootScope', '$window',
    function ($scope, $http, $uibModalStack, toaster, $rootScope, $window) {
        $scope.userSettings = {
            email: null,
            settings: {
                currency: null
            }
        };

        $scope.currencies = null;

        $http.get(
            "http://localhost:8080/fiat/list"
        ).then(function (response) {
            $scope.currencies = response.data;
        });

        $scope.save = function () {
            $scope.userSettings.email = $rootScope.user.email;

            $http({
                method: 'PUT',
                url: "http://localhost:8080/user/update/settings",
                data: $scope.userSettings
            }).then(function successCallback() {
                toaster.pop('success', "Successfully changed currency",
                    "Currency changed to " + $scope.userSettings.settings.currency.name);
                $rootScope.updateUser();
                $uibModalStack.dismissAll();
            }, function failureCallback(response) {
                console.log(response);
                toaster.pop('error', "Failed to Change currency", response.data);
            });
        };

        $scope.logOut = function () {
            $http({
                method: 'POST',
                url: "http://localhost:8080/security/logout/",
                data: $scope.loginDetails
            }).then(function successCallback() {
                toaster.pop('success', "Logged Out", "");

                $window.location.reload();
            })
        };
    }]);