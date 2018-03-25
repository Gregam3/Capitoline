var app = angular.module("capitoline", ['ui.bootstrap', 'smart-table', 'n3-line-chart', 'n3-pie-chart', 'toaster']);

angular.module("capitoline").factory('user', function ($http) {
    return null;
});

app.run(function ($rootScope) {
    $rootScope.totalValue = 0.0;
    $rootScope.acquisitionCost = 0.0;
    $rootScope.user = null;
    $rootScope.profitting = null;

    $rootScope.historicalPortfolio = {
        total: [],
        crypto: [],
        stock: [],
        fiat: []
    };
});

app.value('Email', 'gregoryamitten@gmail.com');
app.value('AlphaVantageKey', 'QVJRID55FX6HALQH');

app.controller("overview", ['$scope', '$http', '$uibModal', '$rootScope', function ($scope, $http, $uibModal, $rootScope) {
}]);


app.controller("totalValueLineChartCtrl", ['$scope', '$http', '$rootScope', function ($scope, $http, $rootScope) {


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
            'http://localhost:8080/user/get/holding-graph-data/gregoryamitten@gmail.com'
        ).then(function (response) {
            $rootScope.historicalPortfolio.total = response.data.total;
            $rootScope.historicalPortfolio.crypto = response.data.crypto;
            $rootScope.historicalPortfolio.fiat = response.data.fiat;
            $rootScope.historicalPortfolio.stock = response.data.stock;
        });

        console.log($rootScope.historicalPortfolio);
    }
}]);

app.controller("performanceCtrl", ['$scope', '$http', '$rootScope', 'toaster',
    function ($scope, $http, $rootScope, toaster) {
        //Diversification Tab
        $scope.pieOptions = {
            thickness: 10
        };

        $rootScope.portfolioDiversification = [];

        //Crypto Performance
        $scope.btcChange = 0;
        $scope.portfolioCryptoChange = 0;
        $scope.volatility = [];


        $scope.calculateCryptoPerformance = function () {
            if ($rootScope.historicalPortfolio.crypto.length === 0) {
                toaster.pop('info', "Cannot do that yet", "We need to load a few more things first.");
                $scope.active = 0;
            } else if ($rootScope.historicalPortfolio.crypto.length < 5) {
                toaster.pop('error', "Not Applicable for your portfolio", "You must have owned Cryptocurrencies for at least 30 days before this is accessible");
                $scope.active = 0;
            } else {
                let btcValueOneWeekAgo = 0;
                let btcValueNow = 0;

                $http.get("https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD")
                    .then(function (response) {
                        btcValueNow = response.data["USD"];

                        $http.get("https://min-api.cryptocompare.com/data/pricehistorical?fsym=BTC&tsyms=USD&ts=" + (new Date().getTime() - (7 * 24 * 60 * 60 * 1000)))
                            .then(function (response) {
                                btcValueOneWeekAgo = response.data["BTC"]["USD"];
                                $scope.btcChange = ((btcValueNow / btcValueOneWeekAgo) * 100 - 100).toFixed(3);

                                $scope.portfolioCryptoChange =
                                    ($rootScope.historicalPortfolio.crypto[$rootScope.historicalPortfolio.crypto.length - 1].value /
                                    ($rootScope.historicalPortfolio.crypto[$rootScope.historicalPortfolio.crypto.length - 31].value)
                                        * 100 - 100).toFixed(3)

                            });
                    });

                for (const holding in $rootScope.holdings.cryptos) {
                    $http.get("https://min-api.cryptocompare.com/data/histoday?fsym=USD&tsym=" + holding + "&limit=30")
                        .then(function (response) {
                            const currentHoldingHistory = response.data.Data;

                            let high = 0;
                            let low = 1000000;

                            console.log(response);

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
                            else if (volatility > 1.5 && volatility < 2)$scope.volatility[2].value++;
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
            }

        }
    }]);

app.controller("holdingManagementCtrl", ['$scope', '$http', '$uibModal', '$rootScope', 'AlphaVantageKey', 'toaster',
    function ($scope, $http, $uibModal, $rootScope, AlphaVantageKey, toaster) {
        $scope.user = null;

        $rootScope.updateUser = function () {
            $rootScope.cryptoValue = 0;
            $rootScope.stockValue = 0;
            $rootScope.fiatValue = 0;

            $rootScope.totalValue = 0;

            console.log("updating user");
            $rootScope.portfolioDiversification = [];
            $http.get('http://localhost:8080/user/get/gregoryamitten@gmail.com')
                .then(function (response) {
                    $rootScope.user = response.data;
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
                                    $rootScope.holdings.stocks[holding].name + "'s value was retrieved as 0, this may influence your portfolio's accuracy.");
                            }

                            $rootScope.holdings.stocks[holding].totalValue = currentValue;
                            $rootScope.totalValue += currentValue;
                            $rootScope.stockValue += currentValue;

                            i++;
                        }
                        $rootScope.generateGraph();
                        $rootScope.profitting = $rootScope.totalValue > $rootScope.acquisitionCost;

                        $rootScope.portfolioDiversification.push(
                            {
                                label: "Cryptos - " + (($scope.cryptoValue / $scope.totalValue) * 100).toFixed(2) + "%",
                                value: $scope.cryptoValue.toFixed(2),
                                color: '#139000'
                            },
                            {
                                label: "Fiat - " + (($scope.fiatValue / $scope.totalValue) * 100).toFixed(2) + "%",
                                value: $scope.fiatValue,
                                color: '#ad0e00'
                            },
                            {
                                label: "Stocks - " + (($scope.stockValue / $scope.totalValue) * 100).toFixed(2) + "%",
                                value: $scope.stockValue,
                                color: '#0000d6'
                            }
                        );
                    });


                }
            );
            // $scope.$apply();
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
            }).result.then(function (user) {
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

app.controller("settingsCtrl", ['$scope', '$http', '$uibModalStack', 'Email', function ($scope, $http, $uibModalStack, Email) {
    $scope.user = {
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

    $scope.fetchCurrency = function () {

    };

    $scope.save = function () {
        $http.put(
            "http://localhost:8080/user/update",
            JSON.stringify($scope.user),
            {"Content-Type": "application/json"}
        ).then(function (response) {
            console.log(response);
            $uibModalStack.dismissAll();
        });

    }
}]);

app.controller("addHoldingCtrl", ['$scope', '$http', '$uibModalStack', 'user', '$rootScope', 'toaster',
    function ($scope, $http, $uibModalStack, user, $rootScope, toaster) {
        $scope.holding = {};

        let newHolding = {
            email: "gregoryamitten@gmail.com",
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
        }
    }]);