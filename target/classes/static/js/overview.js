var app = angular.module("capitoline", ['ui.bootstrap', 'smart-table', 'n3-line-chart', 'n3-pie-chart', 'toaster']);

app.run(function ($rootScope) {
    $rootScope.totalValue = 0.0;
    $rootScope.acquisitionCost = 0.0;

    $rootScope.user = {};
    $rootScope.profitting = null;
    $rootScope.userCurrency = {
        modifier: 1,
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

app.controller("homeCtrl", ['$scope', '$http', '$uibModal', '$rootScope', function ($scope, $http, $uibModal, $rootScope) {
    $scope.openSettings = function () {
        $uibModal.open({
            templateUrl: 'templates/home/popups/settings-popup.html',
            controller: 'settingsCtrl'
        })
    };

    $rootScope.setCurrencyModifier = function () {
        if ($rootScope.user.settings.userCurrency.acronym === "USD") {
            $rootScope.userCurrency.modifier = 1;
            $rootScope.userCurrency.symbol = '$';
        } else {
            $http.get('https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=' + $rootScope.user.settings.userCurrency.acronym)
                .then(function (response) {
                    //a modifier used instead of changing requests as AlphaVantage does not provide currency support
                    if ($rootScope.user.settings.userCurrency.acronym !== "USD") {
                        $rootScope.userCurrency.modifier = response.data[$rootScope.user.settings.userCurrency.acronym];
                        $rootScope.userCurrency.symbol = $rootScope.user.settings.userCurrency.symbol;
                    }
                });
        }
    };
}]);


app.controller("totalValueLineChartCtrl", ['$scope', '$http', '$rootScope', '$window',
    function ($scope, $http, $rootScope, $window) {
        $scope.showHistory = false;

        $scope.lineOptions = {
            series: [
                {
                    axis: "y",
                    dataset: "total",
                    key: "value",
                    label: "Total Value ($)",
                    color: "#7d7d7d",
                    type: ['line', 'area'],
                    id: 'mySeries0'
                },
                {
                    axis: "y",
                    dataset: "crypto",
                    key: "value",
                    label: "Crypto Value ($)",
                    color: "#008c00",
                    type: ['line', 'area'],
                    id: 'mySeries1'
                },
                {
                    axis: "y",
                    dataset: "stock",
                    key: "value",
                    label: "Stock Value ($)",
                    color: "#0000c8",
                    type: ['line', 'area'],
                    id: 'mySeries2'
                },
                {
                    axis: "y",
                    dataset: "fiat",
                    key: "value",
                    label: "Fiat Value ($)",
                    color: "#b40e00",
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
                y: {}
            }
        };

        //Fetch and process graph data
        $rootScope.generateGraph = function () {
            $http.get(
                'http://localhost:8080/user/get/holding-graph-data/' + $rootScope.user.email
            ).then(function (response) {
                console.log("generated graph");

                $rootScope.historicalPortfolio.total = response.data.total;
                $rootScope.historicalPortfolio.crypto = response.data.crypto;
                $rootScope.historicalPortfolio.fiat = response.data.fiat;
                $rootScope.historicalPortfolio.stock = response.data.stock;

                $scope.showHistory = true;
            });

            console.log($rootScope.historicalPortfolio);
        }
    }]);

app.controller("performanceCtrl", ['$scope', '$http', '$rootScope', 'toaster', 'AlphaVantageKey',
    function ($scope, $http, $rootScope, toaster, AlphaVantageKey) {
        //Diversification Tab
        $scope.pieOptions = {
            thickness: 10
        };

        $rootScope.portfolioDiversification = [];

        $scope.btcChange = 0;
        $scope.currencyChange = 0;
        $scope.aggregateSectorChange = 0;

        $scope.portfolioCryptoChange = 0;
        $scope.portfolioStockChange = 0;
        $scope.portfolioFiatChange = 0;

        $scope.volatility = [];

        let btcValueOneMonthAgo = 0;
        let btcValueNow = 0;

        let totalFiatValueOneMonthAgo = 0;
        let totalFiatValueNow = 0;

        $http.get("https://min-api.cryptocompare.com/data/price?fsym=USD&tsyms=BTC,JPY,EUR,GBP,CNY,CHF")
            .then(function (response) {
                btcValueNow = response.data["BTC"];
                totalFiatValueNow =
                    response.data["JPY"] +
                    response.data["EUR"] +
                    response.data["GBP"] +
                    response.data["CNY"] +
                    response.data["CHF"];


                $http.get("https://min-api.cryptocompare.com/data/pricehistorical?fsym=USD&tsyms=BTC,JPY,EUR,GBP,CNY,CHF&ts=" + (new Date().getTime() - (7 * 24 * 60 * 60 * 1000)))
                    .then(function (response) {
                        btcValueOneMonthAgo = response.data["USD"]["BTC"];
                        totalFiatValueOneMonthAgo =
                            response.data["USD"]["JPY"] +
                            response.data["USD"]["EUR"] +
                            response.data["USD"]["GBP"] +
                            response.data["USD"]["CNY"] +
                            response.data["USD"]["CHF"];

                        $scope.btcChange = ((btcValueNow / btcValueOneMonthAgo) * 100 - 100).toFixed(3);
                        $scope.currencyChange = ((totalFiatValueNow / totalFiatValueOneMonthAgo) * 100 - 100).toFixed(3);
                    });
            });

        $scope.calculateCryptoPerformance = function () {
            if ($rootScope.historicalPortfolio.crypto.length < 30)
                $scope.performanceNotReadyPopUp("crypto");
            else
                $scope.portfolioCryptoChange =
                    ($rootScope.historicalPortfolio.crypto[$rootScope.historicalPortfolio.crypto.length - 1].value /
                        ($rootScope.historicalPortfolio.crypto[$rootScope.historicalPortfolio.crypto.length - 31].value)
                        * 100 - 100).toFixed(3);

            $scope.generateCryptoVolatilityPieChart();
        };

        $scope.calculateFiatPerformance = function () {
            if ($rootScope.historicalPortfolio.fiat.length < 30)
                $scope.performanceNotReadyPopUp("fiat");
            else
                $scope.portfolioFiatChange =
                    ($rootScope.historicalPortfolio.fiat[$rootScope.historicalPortfolio.fiat.length - 1].value /
                        ($rootScope.historicalPortfolio.fiat[$rootScope.historicalPortfolio.fiat.length - 31].value)
                        * 100 - 100).toFixed(3);

            console.log($scope.portfolioFiatChange);
        };

        $scope.retrieveAndCalculateStockPerformance = function () {
            if ($rootScope.historicalPortfolio.stock.length < 30)
                $scope.performanceNotReadyPopUp("stock");
            else
                $scope.portfolioStockChange =
                    ($rootScope.historicalPortfolio.stock[$rootScope.historicalPortfolio.stock.length - 1].value /
                        ($rootScope.historicalPortfolio.stock[$rootScope.historicalPortfolio.stock.length - 31].value)
                        * 100 - 100).toFixed(3);

            $http.get('https://www.alphavantage.co/query?function=SECTOR&apikey=' + AlphaVantageKey)
                .then(function (response) {
                    const sectorPerformance = response.data["Rank D: 1 Month Performance"];

                    console.log(sectorPerformance);


                    //AlphaVantage API sends data back with trailing '%', this needs to be removed in order for it to be treated as a number
                    $scope.aggregateSectorChange =
                        (sectorPerformance["Utilities"].substr(0, sectorPerformance["Utilities"].length - 1) * 1 +
                            sectorPerformance["Energy"].substr(0, sectorPerformance["Energy"].length - 1) * 1 +
                            sectorPerformance["Information Technology"].substr(0, sectorPerformance["Information Technology"].length - 1) * 1 +
                            sectorPerformance["Consumer Discretionary"].substr(0, sectorPerformance["Consumer Discretionary"].length - 1) * 1 +
                            sectorPerformance["Telecommunication Services"].substr(0, sectorPerformance["Telecommunication Services"].length - 1) * 1 +
                            sectorPerformance["Health Care"].substr(0, sectorPerformance["Health Care"].length - 1) * 1 +
                            sectorPerformance["Industrials"].substr(0, sectorPerformance["Industrials"].length - 1) * 1 +
                            sectorPerformance["Financials"].substr(0, sectorPerformance["Financials"].length - 1) * 1) / 8;
                });
        };

        $scope.performanceNotReadyPopUp = function (holdingType) {
            toaster.pop('info', "Cannot do that yet", "We might still be loading your data, if not your oldest " + holdingType + " must be older than a month for this functionality.");
            $scope.active = 0;
        };

        $scope.generateCryptoVolatilityPieChart = function () {
            for (const holding in $rootScope.holdings.cryptos) {
                $http.get("https://min-api.cryptocompare.com/data/histoday?fsym=USD&tsym=" + holding + "&limit=30")
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
    }]);


app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', 'toaster', '$route',
    function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey, toaster, $route) {
        $rootScope.updateUser = function () {
            $rootScope.cryptoValue = 0;
            $rootScope.stockValue = 0;
            $rootScope.fiatValue = 0;

            $rootScope.totalValue = 0;

            console.log("updating user");

            if (!$rootScope.user.email) {
                $rootScope.user.email = localStorage.getItem('email');
            }

            $scope.isObjectEmpty = function (object) {
                return angular.equals(object, {});
            };

            // $scope.userHasNoHoldings = function () {
            //     if($scope.isObjectEmpty($rootScope.holdings))
            //         return false;
            //
            //     console.log($rootScope.holdings);
            //
            //     return $scope.isObjectEmpty($rootScope.holdings.stocks) &&
            //         $scope.isObjectEmpty($rootScope.holdings.cryptos) &&
            //         $scope.isObjectEmpty($rootScope.holdings.fiat)
            // };


            $http.get('http://localhost:8080/user/get/' + $rootScope.user.email)
                .then(function (response) {
                    console.log(response.data);
                    $rootScope.user = response.data;

                    if ($rootScope.user.settings
                        || $rootScope.user.settings.currency.acronym !== "USD") {
                        $rootScope.userCurrencyModifier =
                            $rootScope.setCurrencyModifier();
                    }

                    $rootScope.acquisitionCost = 0;
                    console.log(response.data);

                    $rootScope.holdings = {
                        cryptos: {},
                        stocks: {},
                        fiats: {}
                    };

                    for (let i = 0; i < $rootScope.user.holdings.length; i++) {
                        const currentHolding = $rootScope.user.holdings[i];

                        if (currentHolding.holdingType === "STOCK") {
                            $rootScope.holdings.stocks[currentHolding.acronym] = currentHolding;
                        } else if (currentHolding.holdingType === "CRYPTO") {
                            $rootScope.holdings.cryptos[currentHolding.acronym] = currentHolding;
                        } else if (currentHolding.holdingType === "FIAT") {
                            $rootScope.holdings.fiats[currentHolding.acronym] = currentHolding;
                        }

                        $rootScope.acquisitionCost += currentHolding.acquisitionCost;
                    }
                }).then(function () {
                    $http.get(
                        "https://min-api.cryptocompare.com/data/pricemulti?fsyms="
                        + $scope.convertHoldingsToUriVariables($rootScope.holdings.cryptos) + ","
                        + $scope.convertHoldingsToUriVariables($rootScope.holdings.fiats)
                        + "&tsyms=USD"
                    ).then(function (response) {

                        let holding;
                        for (holding in $rootScope.holdings.cryptos) {
                            console.log("converting cryptos");
                            $rootScope.holdings.cryptos[holding].price =
                                (response.data[holding]) ? response.data[holding]["USD"] : null;
                            const currentValue = $rootScope.holdings.cryptos[holding].price * $rootScope.holdings.cryptos[holding].totalQuantity;

                            $rootScope.holdings.cryptos[holding].totalValue = currentValue;
                            $rootScope.totalValue += currentValue;
                            $rootScope.cryptoValue += currentValue;

                        }

                        for (holding in $rootScope.holdings.fiats) {
                            console.log("converting fiats");
                            $rootScope.holdings.fiats[holding].price =
                                (response.data[holding]) ? response.data[holding]["USD"] : null;

                            const currentValue =
                                $rootScope.holdings.fiats[holding].price * $rootScope.holdings.fiats[holding].totalQuantity;

                            $rootScope.holdings.fiats[holding].totalValue = currentValue;
                            $rootScope.totalValue += currentValue;
                            $rootScope.fiatValue += currentValue;
                        }
                    });

                    console.log($rootScope.holdings);

                    //Get all User's stock prices
                    $http.get("https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=" + $scope.convertHoldingsToUriVariables($scope.holdings.stocks)
                        + "&apikey=" + AlphaVantageKey).then(function (response) {

                        let i = 0;

                        for (const holding in $rootScope.holdings.stocks) {
                            console.log("converting stocks");
                            $rootScope.holdings.stocks[holding].price =
                                (response.data["Stock Quotes"][i]["2. price"]) ?
                                    response.data["Stock Quotes"][i]["2. price"] : null;
                            const currentValue = $rootScope.holdings.stocks[holding].price * $rootScope.holdings.stocks[holding].totalQuantity;

                            if (currentValue === 0) {
                                toaster.pop('warning', "Issue retrieving " + $rootScope.holdings.stocks[holding].acronym,
                                    "One of our data providers is currently not functioning properly, this may influence your portfolio's accuracy.");
                            }

                            $rootScope.holdings.stocks[holding].totalValue = currentValue;
                            $rootScope.totalValue += currentValue;
                            $rootScope.stockValue += currentValue;

                            i++;
                        }

                        $rootScope.generateGraph();
                        $rootScope.profitting =
                            $rootScope.totalValue > $rootScope.acquisitionCost;

                        $rootScope.portfolioDiversification = [
                            {
                                label: "Cryptos",
                                value: (($scope.cryptoValue / $rootScope.totalValue) * 100).toFixed(2),
                                color: '#139000'
                            },
                            {
                                label: "Fiat",
                                value: (($scope.fiatValue / $rootScope.totalValue) * 100).toFixed(2),
                                color: '#ad0e00'
                            },
                            {
                                label: "Stocks",
                                value: (($scope.stockValue / $rootScope.totalValue) * 100).toFixed(2),
                                color: '#0000d6'
                            }
                        ];


                        $route.reload();
                    });
                }
            );
        };

        $rootScope.updateUser();

        $scope.convertHoldingsToUriVariables = function (currentHoldingsList) {
            let tickers = "";

            for (const holding in currentHoldingsList) {
                tickers += currentHoldingsList[holding].acronym + ",";
            }

            //Removing trailing comma for uri format
            return tickers.substr(0, tickers.length - 1);
        };

        $scope.addHolding = function () {
            $rootScope.addHoldingModal = $uibModal.open({
                templateUrl: 'templates/home/popups/add-holding-popup.html',
                controller: 'addHoldingCtrl'
            })
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

app.controller("settingsCtrl", ['$scope', '$http', '$uibModalStack', 'Email', 'toaster', '$rootScope',
    function ($scope, $http, $uibModalStack, Email, toaster, $rootScope) {
        $scope.userSettings = {
            email: Email,
            settings: {
                currency: null
            }
        };

        $scope.currencies = null;

        $http.get(
            "http://localhost:8080/fiat/list"
        ).then(function (response) {
            console.log(response);
            $scope.currencies = response.data;
        });

        $scope.save = function () {
            $http({
                method: 'PUT',
                url: "http://localhost:8080/user/update/settings",
                data: $scope.userSettings
            }).then(function successCallback(response) {
                toaster.pop('success', "Successfully changed currency",
                    "Currency changed to " + $scope.userSettings.settings.currency.name);
                $rootScope.updateUser();
                $uibModalStack.dismissAll();
            }, function failureCallback(response) {
                console.log(response);
                toaster.pop('error', "Failed to Change currency", response.data);
            });
        }
    }]);

app.controller("addHoldingCtrl", ['$scope', '$http', '$uibModalStack', '$rootScope', 'toaster',
    function ($scope, $http, $uibModalStack, $rootScope, toaster) {
        $scope.holding = {};

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
            if (holdingType === "STOCK") {
                $scope.options = {
                    minDate: new Date(946684800000),
                    maxDate: new Date()
                };
            } else if (holdingType === "CRYPTO") {
                $scope.options = {
                    minDate: new Date(1230768000000),
                    maxDate: new Date()
                };
            } else {
                $scope.options = {
                    minDate: new Date(1420070400000),
                    maxDate: new Date()
                };
            }
        };


        console.log(newHolding);

        $scope.holdingList = [];

        //save reloading every time
        if ($scope.holdingList.length === 0) {
            $http.get(
                "http://localhost:8080/crypto/list"
            ).then(function (response) {
                $scope.holdingList = $scope.holdingList.concat(response.data);
            });

            $http.get(
                "http://localhost:8080/fiat/list"
            ).then(function (response) {
                $scope.holdingList = $scope.holdingList.concat(response.data);
            });

            $http.get(
                "http://localhost:8080/stock/list"
            ).then(function (response) {
                $scope.holdingList = $scope.holdingList.concat(response.data);
            });
        }

        $scope.add = function () {
            newHolding = {
                acronym: $scope.holding.acronym,
                name: $scope.holding.name,
                holdingType: $scope.holding.holdingType,
                quantity: $scope.quantity,
                dateBought: $scope.holding.dateBought * 1
            };


            console.log(newHolding);

            $http({
                method: 'PUT',
                url: "http://localhost:8080/user/add-holding",
                data: newHolding
            }).then(function (response) {
                toaster.pop('success', "Successfully Added", "Added " + newHolding.quantity + " instance of " + newHolding.name);
                $rootScope.updateUser();
                $uibModalStack.dismissAll();
            }, function (response) {
                toaster.pop('error', "Failed to Add", response.data);
            });
        };
    }]);

app.controller("loginCtrl", ['$scope', '$http', 'toaster', '$window', '$rootScope',
    function ($scope, $http, toaster, $window, $rootScope) {
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
                $window.localStorage.setItem('email', $scope.loginDetails.email);
                console.log($window.localStorage.getItem('email'));
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
        }
    }]);
